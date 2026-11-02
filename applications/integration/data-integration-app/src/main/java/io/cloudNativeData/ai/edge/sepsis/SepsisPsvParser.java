package io.cloudNativeData.ai.edge.sepsis;

import io.cloudNativeData.ai.edge.healthcare.domain.sepsis.SepsisVitalHistory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses a PhysioNet/Computing in Cardiology Challenge 2019 patient file (pipe separated, one row per hour)
 * into {@link SepsisVitalHistory} records.
 */
public class SepsisPsvParser {

    /**
     * @param patientId the patient identifier (file name without extension)
     * @param psv       the file content, including the header row
     * @return one record per hourly row that has at least one vital sign or lab reading
     */
    public List<SepsisVitalHistory> parse(String patientId, String psv) {

        var lines = psv.lines().filter(l -> !l.isBlank()).toList();
        var results = new ArrayList<SepsisVitalHistory>();
        if (lines.isEmpty())
            return results;

        var columns = new HashMap <String, Integer>();
        var header = lines.get(0).split("\\|", -1);
        for (int i = 0; i < header.length; i++)
            columns.put(header[i].trim(), i);

        for (String line : lines.subList(1, lines.size())) {
            var row = line.split("\\|", -1);

            var heartRate = decimal(row, columns, "HR");
            var systolic = decimal(row, columns, "SBP");
            var diastolic = decimal(row, columns, "DBP");
            var map = decimal(row, columns, "MAP");
            var resp = decimal(row, columns, "Resp");
            var spo2 = decimal(row, columns, "O2Sat");
            var temp = decimal(row, columns, "Temp");
            var wbc = decimal(row, columns, "WBC");
            var lactate = decimal(row, columns, "Lactate");

            if (Arrays.stream(new BigDecimal[]{heartRate, systolic, diastolic, map, resp, spo2, temp, wbc, lactate})
                    .allMatch(java.util.Objects::isNull))
                continue; // nothing measured in this hour

            var hour = decimal(row, columns, "ICULOS");
            var label = decimal(row, columns, "SepsisLabel");

            results.add(SepsisVitalHistory.builder()
                    .id(patientId + "-" + (hour == null ? results.size() + 1 : hour.intValue()))
                    .heartRate(heartRate)
                    .systolicBp(systolic)
                    .diastolicBp(diastolic)
                    .meanArterialPressure(map)
                    .respiratoryRate(resp)
                    .spo2(spo2)
                    .temperature(temp)
                    .wbcCount(wbc)
                    .lactate(lactate)
                    .hasSepsis(label != null && label.signum() > 0)
                    .build());
        }
        return results;
    }

    private static BigDecimal decimal(String[] row, Map<String, Integer> columns, String name) {
        var index = columns.get(name);
        if (index == null || index >= row.length)
            return null;
        var value = row[index].trim();
        if (value.isEmpty() || value.equalsIgnoreCase("NaN"))
            return null;
        return new BigDecimal(value);
    }
}

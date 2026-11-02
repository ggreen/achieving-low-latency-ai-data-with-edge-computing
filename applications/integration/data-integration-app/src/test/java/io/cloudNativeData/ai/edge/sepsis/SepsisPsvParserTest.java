package io.cloudNativeData.ai.edge.sepsis;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SepsisPsvParserTest {

    private static final String PSV = """
            HR|O2Sat|Temp|SBP|MAP|DBP|Resp|WBC|Lactate|Age|ICULOS|SepsisLabel
            NaN|NaN|NaN|NaN|NaN|NaN|NaN|NaN|NaN|83.14|1|0
            97|95|NaN|98|75.33|NaN|19|NaN|NaN|83.14|2|0
            110|91|38.6|88|65.3|54|24|14.8|3.2|83.14|3|1
            """;

    @Test
    void parse_skipsEmptyRowsAndMapsColumns() {
        var results = new SepsisPsvParser().parse("p000001", PSV);

        assertEquals(2, results.size());

        var first = results.get(0);
        assertEquals("p000001-2", first.getId());
        assertEquals(new BigDecimal("97"), first.getHeartRate());
        assertEquals(new BigDecimal("95"), first.getSpo2());
        assertNull(first.getTemperature());
        assertFalse(first.getHasSepsis());

        var second = results.get(1);
        assertEquals(new BigDecimal("3.2"), second.getLactate());
        assertEquals(new BigDecimal("14.8"), second.getWbcCount());
        assertTrue(second.getHasSepsis());
    }
}

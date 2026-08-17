package io.cloudnativedata.ai.edge;

import io.cloudNativeData.ai.edge.healthcare.domain.sepsis.SepsisVitalHistory;
import io.cloudnativedata.ai.edge.sepsis.entity.SepsisVitalHistoryEntity;
import nyla.solutions.core.patterns.conversion.JavaBeanConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

@Configuration
public class MappingConfig {

    /**
     * Convert history to an entity
     * @return SepsisVitalHistoryEntity
     */
    @Bean
    Converter<SepsisVitalHistory, SepsisVitalHistoryEntity> converter() {

        var javaBeanConverter = new JavaBeanConverter<SepsisVitalHistory, SepsisVitalHistoryEntity>
                (SepsisVitalHistoryEntity.class);

        return javaBeanConverter::convert;
    }
}

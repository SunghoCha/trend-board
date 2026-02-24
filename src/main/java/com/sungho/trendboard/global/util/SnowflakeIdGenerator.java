package com.sungho.trendboard.global.util;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;

import java.util.EnumSet;

/**
 * Hibernate 7.x BeforeExecutionGenerator 기반 Snowflake ID 생성기
 */
public class SnowflakeIdGenerator implements BeforeExecutionGenerator {

    private static final Snowflake SNOWFLAKE = new Snowflake();

    @SuppressWarnings("unused")
    public SnowflakeIdGenerator(SnowflakeId annotation) {
        // @IdGeneratorType 규약: 어노테이션을 받는 생성자 필요
    }

    @Override
    public Object generate(SharedSessionContractImplementor session, Object owner, Object currentValue, EventType eventType) {
        return SNOWFLAKE.nextId();
    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        return EnumSet.of(EventType.INSERT);
    }
}

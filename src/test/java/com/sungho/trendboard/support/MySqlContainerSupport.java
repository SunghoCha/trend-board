package com.sungho.trendboard.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;

public abstract class MySqlContainerSupport {

    private static final MySQLContainer<?> MYSQL_CONTAINER = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("trendboard")
            .withUsername("test")
            .withPassword("test");

    static {
        MYSQL_CONTAINER.start();
    }

    /**
     * Registers Spring datasource properties using values from the MySQL Testcontainers instance.
     *
     * <p>The method exposes the following properties into Spring's environment:
     * <ul>
     *   <li>spring.datasource.url</li>
     *   <li>spring.datasource.username</li>
     *   <li>spring.datasource.password</li>
     *   <li>spring.datasource.driver-class-name</li>
     * </ul>
     *
     * @param registry the DynamicPropertyRegistry used to register configuration properties
     */
    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", MYSQL_CONTAINER::getDriverClassName);
    }
}

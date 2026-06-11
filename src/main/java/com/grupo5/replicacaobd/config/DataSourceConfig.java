package com.grupo5.replicacaobd.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    @Value("${DB_PRIMARY_HOST:localhost}")
    private String primaryHost;

    @Value("${DB_REPLICA_HOSTS:localhost}")
    private String replicaHosts;

    @Value("${DB_USER:root}")
    private String dbUser;

    @Value("${DB_PASS:password}")
    private String dbPass;

    @Value("${DB_NAME:aula-db}")
    private String dbName;

    @Bean
    @Primary
    public DataSource dataSource() {
        Map<Object, Object> targetDataSources = new HashMap<>();
        
        // Primary DataSource
        DataSource primaryDataSource = createDataSource(primaryHost);
        targetDataSources.put("PRIMARY", primaryDataSource);

        // Replica DataSources
        String[] replicas = replicaHosts.split(",");
        for (int i = 0; i < replicas.length; i++) {
            targetDataSources.put("REPLICA_" + i, createDataSource(replicas[i].trim()));
        }

        RoutingDataSource routingDataSource = new RoutingDataSource(replicas.length);
        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(primaryDataSource);
        
        return routingDataSource;
    }

    private DataSource createDataSource(String host) {
        String url = String.format("jdbc:mysql://%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC", host, dbName);
        return DataSourceBuilder.create()
                .url(url)
                .username(dbUser)
                .password(dbPass)
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .build();
    }
}

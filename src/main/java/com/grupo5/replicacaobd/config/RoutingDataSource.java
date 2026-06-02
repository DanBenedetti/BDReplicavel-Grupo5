package com.grupo5.replicacaobd.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoutingDataSource extends AbstractRoutingDataSource {
    private final AtomicInteger counter = new AtomicInteger(0);
    private final int replicaCount;

    public RoutingDataSource(int replicaCount) {
        this.replicaCount = replicaCount;
    }

    @Override
    protected Object determineCurrentLookupKey() {
        DataSourceType type = DataSourceContextHolder.get();
        if (type == DataSourceType.REPLICA && replicaCount > 0) {
            int index = counter.getAndIncrement() % replicaCount;
            return "REPLICA_" + index;
        }
        return "PRIMARY";
    }
}

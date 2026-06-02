package com.grupo5.replicacaobd.config;

public class DataSourceContextHolder {
    private static final ThreadLocal<DataSourceType> CONTEXT = new ThreadLocal<>();

    public static void set(DataSourceType dataSourceType) {
        CONTEXT.set(dataSourceType);
    }

    public static DataSourceType get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}

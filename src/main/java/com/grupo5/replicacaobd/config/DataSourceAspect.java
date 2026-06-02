package com.grupo5.replicacaobd.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

@Aspect
@Component
@Order(0)
public class DataSourceAspect {

    @Around("@annotation(transactional)")
    public Object proceed(ProceedingJoinPoint proceedingJoinPoint, Transactional transactional) throws Throwable {
        try {
            if (transactional.readOnly()) {
                DataSourceContextHolder.set(DataSourceType.REPLICA);
            } else {
                DataSourceContextHolder.set(DataSourceType.PRIMARY);
            }
            return proceedingJoinPoint.proceed();
        } finally {
            DataSourceContextHolder.clear();
        }
    }
}

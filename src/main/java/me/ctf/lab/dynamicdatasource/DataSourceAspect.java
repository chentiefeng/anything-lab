package me.ctf.lab.dynamicdatasource;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 动态数据源切面
 * @author: chentiefeng[chentiefeng@linzikg.com]
 * @create: 2019-12-11 10:04
 */
@Aspect
@Component
@Order(1)
public class DataSourceAspect {
    @Pointcut("@annotation(me.ctf.lab.dynamicdatasource.DataSource)")
    public void dsPointCut() {

    }

    @Around("dsPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        DataSource ds = method.getAnnotation(DataSource.class);
        if (ds == null) {
            DynamicRoutingDataSource.setDataSource(DynamicDataSourceFactory.getDefaultDataSourceKey());
        } else if (DynamicDataSourceFactory.DYNAMIC_DATA_SOURCE.equals(ds.name())) {
            //动态参数数据源
            String dsKey = DynamicDataSourceFactory.getDefaultDataSourceKey();
            Object[] args = point.getArgs();
            if (Objects.nonNull(args) && args.length > 0) {
                dsKey = Objects.toString(args[args.length - 1], DynamicDataSourceFactory.getDefaultDataSourceKey());
            }
            DynamicRoutingDataSource.setDataSource(dsKey);
        } else {
            DynamicRoutingDataSource.setDataSource(ds.name());
        }
        try {
            return point.proceed();
        } finally {
            DynamicRoutingDataSource.clearDataSource();
        }
    }
}

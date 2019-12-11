package me.ctf.lab.dynamicdatasource;

import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author: chentiefeng[chentiefeng@linzikg.com]
 * @create: 2019-12-11 09:38
 */
@Component
@DependsOn("dynamicDataSourceFactory")
public class DynamicRoutingDataSource extends AbstractRoutingDataSource {
    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();

    @PostConstruct
    public void init() {
        super.setDefaultTargetDataSource(DynamicDataSourceFactory.getDefaultDataSource());
        Map<Object, Object> runtimeDataSourceMap = DynamicDataSourceFactory.getRuntimeDataSourceMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        super.setTargetDataSources(runtimeDataSourceMap);
        super.afterPropertiesSet();
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return getDataSource();
    }

    public static void setDataSource(String dataSource) {
        CONTEXT_HOLDER.set(dataSource);
    }

    public static String getDataSource() {
        return CONTEXT_HOLDER.get();
    }

    public static void clearDataSource() {
        CONTEXT_HOLDER.remove();
    }

    @Override
    protected javax.sql.DataSource determineTargetDataSource() {
        Object lookupKey = determineCurrentLookupKey();
        if (Objects.isNull(lookupKey)) {
            return DynamicDataSourceFactory.getDefaultDataSource();
        }
        javax.sql.DataSource dataSource = DynamicDataSourceFactory.getRuntimeDataSourceMap().get(Objects.toString(lookupKey));
        if (dataSource == null) {
            throw new IllegalStateException("Cannot determine target DataSource for lookup key [" + lookupKey + "]");
        }
        return dataSource;
    }
}

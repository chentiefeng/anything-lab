package me.ctf.lab.dynamicdatasource;

import com.alibaba.druid.pool.DruidDataSource;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态数据源工厂
 *
 * @author: chentiefeng[chentiefeng@linzikg.com]
 * @create: 2019-12-11 09:26
 */
@Slf4j
@Component("dynamicDataSourceFactory")
public class DynamicDataSourceFactory {
    /** 数据源mongodb集合 */
    public static final String INDICATOR_CONFIG = "indicator_config";
    private static Gson gson = new GsonBuilder().create();
    /** 运行时数据源 */
    private static Map<String, DataSource> runtimeDataSourceMap = new ConcurrentHashMap<>(8);
    private static MongoTemplate mongoTemplate;
    /** 默认数据源 */
    private static DataSource defaultDataSource;
    /** 默认数据源key */
    private static String defaultDataSourceKey;
    /** druid默认数据库配置 */
    private static DataSourceProperties dataSourceProperties;

    @PostConstruct
    public void init() {
        Query query = Query.query(Criteria.where("key").is("dataSource"));
        Map dsMap = mongoTemplate.findOne(query, Map.class, INDICATOR_CONFIG);
        if (Objects.isNull(dsMap) || dsMap.size() == 0) {
            throw new RuntimeException("数据库配置不存在");
        }
        List<DataSourceConfig> list = gson.fromJson(gson.toJson(dsMap.get("value")), new TypeToken<List<DataSourceConfig>>() {
        }.getType());
        for (DataSourceConfig dataSourceConfig : list) {
            initDataSource(dataSourceConfig);
        }
        if (defaultDataSource == null) {
            throw new RuntimeException("默认数据源不存在");
        }
    }

    /**
     * 初始化数据源
     *
     * @param dataSourceConfig
     */
    private static void initDataSource(DataSourceConfig dataSourceConfig) {
        DruidDataSource druidDataSource = getDruidDataSource(dataSourceProperties);
        druidDataSource.setDriverClassName(dataSourceConfig.getDriverClassName());
        druidDataSource.setUrl(dataSourceConfig.getUrl());
        druidDataSource.setUsername(dataSourceConfig.getUsername());
        druidDataSource.setPassword(dataSourceConfig.getPassword());
        try {
            druidDataSource.init();
            runtimeDataSourceMap.put(dataSourceConfig.getKey(), druidDataSource);
            if (1 == dataSourceConfig.defaultDataSource) {
                defaultDataSourceKey = dataSourceConfig.getKey();
                defaultDataSource = druidDataSource;
                log.info("The default data source has become [{}].", defaultDataSourceKey);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * druid数据源默认参数设置
     * @param properties
     * @return
     */
    public static DruidDataSource getDruidDataSource(DataSourceProperties properties) {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setInitialSize(properties.getInitialSize());
        druidDataSource.setMaxActive(properties.getMaxActive());
        druidDataSource.setMinIdle(properties.getMinIdle());
        druidDataSource.setMaxWait(properties.getMaxWait());
        druidDataSource.setTimeBetweenEvictionRunsMillis(properties.getTimeBetweenEvictionRunsMillis());
        druidDataSource.setMinEvictableIdleTimeMillis(properties.getMinEvictableIdleTimeMillis());
        druidDataSource.setMaxEvictableIdleTimeMillis(properties.getMaxEvictableIdleTimeMillis());
        druidDataSource.setValidationQuery(properties.getValidationQuery());
        druidDataSource.setValidationQueryTimeout(properties.getValidationQueryTimeout());
        druidDataSource.setTestOnBorrow(properties.isTestOnBorrow());
        druidDataSource.setTestOnReturn(properties.isTestOnReturn());
        druidDataSource.setPoolPreparedStatements(properties.isPoolPreparedStatements());
        druidDataSource.setMaxOpenPreparedStatements(properties.getMaxOpenPreparedStatements());
        druidDataSource.setSharePreparedStatements(properties.isSharePreparedStatements());
        try {
            druidDataSource.setFilters(properties.getFilters());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return druidDataSource;
    }

    /** 动态数据源标识 */
    public final static String DYNAMIC_DATA_SOURCE = "dynamic_data_source";

    /**
     * 所有数据源
     *
     * @return
     */
    public static List<DataSourceConfig> list() {
        Query query = Query.query(Criteria.where("key").is("dataSource"));
        Map dsMap = mongoTemplate.findOne(query, Map.class, INDICATOR_CONFIG);
        return gson.fromJson(gson.toJson(dsMap.get("value")), new TypeToken<List<DataSourceConfig>>() {
        }.getType());
    }

    /**
     * 增加数据源
     *
     * @param dataSourceConfig
     */
    public static void addDataSource(DataSourceConfig dataSourceConfig) {
        if (runtimeDataSourceMap.containsKey(dataSourceConfig.key)) {
            throw new RuntimeException(String.format("已经存在key=%s的数据源，请修改key", dataSourceConfig.key));
        }
        String preDefaultDataSourceKey = defaultDataSourceKey;
        initDataSource(dataSourceConfig);
        List<DataSourceConfig> list = list();
        list.add(dataSourceConfig);
        update(preDefaultDataSourceKey, list);
    }

    private static void update(String preDefaultDataSourceKey, List<DataSourceConfig> list) {
        if (!defaultDataSourceKey.equals(preDefaultDataSourceKey)) {
            //默认数据源改变了，需要更新mongodb中的数据源
            list.forEach(ds -> {
                if (ds.getKey().equals(preDefaultDataSourceKey)) {
                    ds.setDefaultDataSource(0);
                }
            });
        }
        mongoTemplate.updateFirst(Query.query(Criteria.where("key").is("dataSource")), Update.update("value", list), INDICATOR_CONFIG);
    }

    /**
     * 更新数据源
     *
     * @param dataSourceConfig
     */
    public static void updateDataSource(DataSourceConfig dataSourceConfig) {
        if (!runtimeDataSourceMap.containsKey(dataSourceConfig.key)) {
            throw new RuntimeException("key不能修改");
        }
        if (dataSourceConfig.getKey().equals(defaultDataSourceKey) && dataSourceConfig.getDefaultDataSource() != 1) {
            throw new RuntimeException("默认数据源不能为空");
        }
        String preDefaultDataSourceKey = defaultDataSourceKey;
        //关闭数据源
        ((DruidDataSource) runtimeDataSourceMap.get(dataSourceConfig.key)).close();
        //重新初始化数据源
        initDataSource(dataSourceConfig);
        List<DataSourceConfig> list = list();
        list.removeIf(ds -> ds.getKey().equals(dataSourceConfig.key));
        list.add(dataSourceConfig);
        update(preDefaultDataSourceKey, list);
    }

    /**
     * 删除数据源
     *
     * @param key
     */
    public static void deleteDataSource(String key) {
        if (defaultDataSourceKey.equals(key)) {
            throw new RuntimeException("不能删除默认数据源");
        }
        ((DruidDataSource) runtimeDataSourceMap.remove(key)).close();
        List<DataSourceConfig> list = list();
        list.removeIf(ds -> ds.getKey().equals(key));
        update(defaultDataSourceKey, list);
    }

    @Data
    public static class DataSourceConfig {
        /** 数据源标识 */
        private String key;
        /** 描述 */
        private String desc;
        /** 驱动类 */
        private String driverClassName;
        /** 地址 */
        private String url;
        /** 用户名 */
        private String username;
        /** 密码 */
        private String password;
        /** 是否默认，1是，0否 */
        private Integer defaultDataSource;
    }

    public static Map<String, DataSource> getRuntimeDataSourceMap() {
        return runtimeDataSourceMap;
    }

    public static DataSource get(String key) {
        return runtimeDataSourceMap.get(key);
    }

    public static DataSource getDefaultDataSource() {
        return defaultDataSource;
    }


    public static String getDefaultDataSourceKey() {
        return defaultDataSourceKey;
    }
    @Resource
    public void setMongoTemplate(MongoTemplate mongoTemplate) {
        DynamicDataSourceFactory.mongoTemplate = mongoTemplate;
    }

    @Resource
    public void setDataSourceProperties(DataSourceProperties dataSourceProperties) {
        DynamicDataSourceFactory.dataSourceProperties = dataSourceProperties;
    }

}

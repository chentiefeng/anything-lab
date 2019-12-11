package me.ctf.lab.dynamicdatasource;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author: chentiefeng[chentiefeng@linzikg.com]
 * @create: 2019-12-11 09:54
 */
@SpringBootTest
public class DynamicDataSourceTest {
    @Resource
    private DynamicDataSourceTestMapper dynamicDataSourceTestMapper;

    @Test
    void contextLoads() {
        System.out.println("列出所有数据源");
        List<DynamicDataSourceFactory.DataSourceConfig> list = DynamicDataSourceFactory.list();
        DynamicDataSourceFactory.DataSourceConfig riskBizDataSourceConfig = list.get(0);
        for (DynamicDataSourceFactory.DataSourceConfig ds : list) {
            System.out.println("数据源key："+ds.getKey()+"是否默认："+ds.getDefaultDataSource());
        }
        System.out.println("=============================================");
        String riskBizSql = "select real_name from arc_user where id = 17910";
        Map<String, Object> query = dynamicDataSourceTestMapper.defaultQuery(riskBizSql);
        System.out.println("查询默认数据源结果：" + query);
        System.out.println("=============================================");
        query = dynamicDataSourceTestMapper.riskBizQuery(riskBizSql);
        System.out.println("查询risk_biz结果：" + query);
        System.out.println("=============================================");
        DynamicDataSourceFactory.DataSourceConfig dataSourceConfig = new DynamicDataSourceFactory.DataSourceConfig();
        dataSourceConfig.setKey("lt_biz");
        dataSourceConfig.setUrl("jdbc:mysql://172.16.157.238:3306/lt_biz?allowMultiQueries=true&useUnicode=true&characterEncoding=UTF-8");
        dataSourceConfig.setDriverClassName("com.mysql.jdbc.Driver");
        dataSourceConfig.setUsername("riskc_user");
        dataSourceConfig.setPassword("Lz@091988");
        dataSourceConfig.setDefaultDataSource(0);
        System.out.println("新增lt_biz数据源");
        DynamicDataSourceFactory.addDataSource(dataSourceConfig);
        System.out.println("=============================================");
        System.out.println("列出所有数据源");
        for (DynamicDataSourceFactory.DataSourceConfig  ds: DynamicDataSourceFactory.list()) {
            System.out.println("数据源key："+ds.getKey()+"是否默认："+ds.getDefaultDataSource());
        }
        System.out.println("=============================================");
        String ltBizSql = "select real_name from lt_user_account where id = 11";
        query = dynamicDataSourceTestMapper.dynamicDataSourceQuery(ltBizSql, "lt_biz");
        System.out.println("查询lt_biz结果：" + query);
        System.out.println("=============================================");
        System.out.println("更新lt_biz数据源为默认数据源");
        dataSourceConfig.setDefaultDataSource(1);
        DynamicDataSourceFactory.updateDataSource(dataSourceConfig);
        System.out.println("=============================================");
        System.out.println("列出所有数据源");
        for (DynamicDataSourceFactory.DataSourceConfig  ds: DynamicDataSourceFactory.list()) {
            System.out.println("数据源key："+ds.getKey()+"是否默认："+ds.getDefaultDataSource());
        }
        System.out.println("=============================================");
        query = dynamicDataSourceTestMapper.defaultQuery(ltBizSql);
        System.out.println("查询默认数据源结果：" + query);
        System.out.println("=============================================");
        System.out.println("更新risk_biz数据源为默认数据源");
        riskBizDataSourceConfig.setDefaultDataSource(1);
        DynamicDataSourceFactory.updateDataSource(riskBizDataSourceConfig);
        System.out.println("=============================================");
        try {
            System.out.println("删除lt_biz数据源");
            DynamicDataSourceFactory.deleteDataSource("lt_biz");
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("=============================================");
    }

}

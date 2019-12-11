package me.ctf.lab.dynamicdatasource;

import org.apache.ibatis.annotations.Select;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * @author: chentiefeng[chentiefeng@linzikg.com]
 * @create: 2019-12-11 09:46
 */
@Repository
public interface DynamicDataSourceTestMapper {
    /**
     * 根据动态SQL查询Map结果
     *
     * @param statement     动态sql查询
     * @param dataSourceKey
     * @return
     */
    @DataSource(name = DynamicDataSourceFactory.DYNAMIC_DATA_SOURCE)
    @Select("${statement}")
    Map<String, Object> dynamicDataSourceQuery(@Param("statement") String statement, String dataSourceKey);

    /**
     * 指定数据源查询
     *
     * @param statement 动态sql查询
     * @return
     */
    @DataSource(name = "risk_biz")
    @Select("${statement}")
    Map<String, Object> riskBizQuery(@Param("statement") String statement);

    /**
     * 默认数据源查询
     *
     * @param statement 动态sql查询
     * @return
     */
    @Select("${statement}")
    Map<String, Object> defaultQuery(@Param("statement") String statement);
}

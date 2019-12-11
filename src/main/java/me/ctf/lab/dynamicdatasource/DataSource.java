package me.ctf.lab.dynamicdatasource;

import java.lang.annotation.*;

/**
 * 多数据源注解
 *
 * @author: chentiefeng[chentiefeng@linzikg.com]
 * @create: 2019-12-11 10:04
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataSource {
    String name();
}

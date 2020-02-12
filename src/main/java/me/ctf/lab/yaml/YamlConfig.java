package me.ctf.lab.yaml;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @author chentiefeng
 * @date 2020-02-12 16:30
 */
@Data
@Configuration
@ConfigurationProperties("yaml-config")
public class YamlConfig {
    private Map<String, Person> personMap;

    @Data
    static class Person {
        private String name;
        private Integer age;
    }
}

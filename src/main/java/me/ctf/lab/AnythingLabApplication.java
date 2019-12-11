package me.ctf.lab;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.stereotype.Repository;

@MapperScan("me.ctf.lab.dynamicdatasource")
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class AnythingLabApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnythingLabApplication.class, args);
    }

}

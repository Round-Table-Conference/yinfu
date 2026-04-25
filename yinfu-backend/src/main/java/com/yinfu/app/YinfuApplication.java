package com.yinfu.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.yinfu.app.mapper")
public class YinfuApplication {

    public static void main(String[] args) {
        SpringApplication.run(YinfuApplication.class, args);
    }
}
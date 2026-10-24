package com.smartlibrary;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 智慧图书馆系统启动类
 * Spring Boot 3 + MyBatis-Plus + Vue 3 全栈应用
 */
@SpringBootApplication
@MapperScan("com.smartlibrary.mapper")
public class SmartLibraryApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartLibraryApplication.class, args);
    }
}

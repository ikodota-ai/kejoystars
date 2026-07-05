package com.mdd.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.TimeZone;

/**
 * 启动器
 */
@Configuration
@ComponentScan(basePackages = {"com.mdd"})
@MapperScan(basePackages = {"com.mdd.*.mapper"})
@EnableTransactionManagement
@SpringBootApplication(exclude = {RedisRepositoriesAutoConfiguration.class})
@EnableScheduling // 启用定时任务支持
public class LikeAdminApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        SpringApplication.run(LikeAdminApplication.class, args);
    }

}

package com.itmd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.itmd.user.mapper")
public class RaoraoUserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RaoraoUserServiceApplication.class, args);
    }

}

package cn.zerolan.zerolanshop;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@MapperScan("cn.zerolan.zerolanshop.mapper")
@EnableScheduling
@SpringBootApplication
public class ZerolanshopApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZerolanshopApplication.class, args);
    }

}

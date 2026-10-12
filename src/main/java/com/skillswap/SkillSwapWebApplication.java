package com.skillswap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * Spring Boot web application entry point for SkillSwap.
 * Excludes DataSourceAutoConfiguration to preserve the existing pure JDBC DBConnection architecture.
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class SkillSwapWebApplication {

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("   Starting SkillSwap Web Platform on http://localhost:8080      ");
        System.out.println("   Color Palette: Mint Green & Warm Cream (Zero Blue)             ");
        System.out.println("==================================================================");
        SpringApplication.run(SkillSwapWebApplication.class, args);
    }
}

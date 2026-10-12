package com.skillswap.config;

import com.skillswap.dao.ExchangeRequestDAO;
import com.skillswap.dao.SkillDAO;
import com.skillswap.dao.StudentDAO;
import com.skillswap.service.ExchangeRequestService;
import com.skillswap.service.SkillMatchingService;
import com.skillswap.service.SkillService;
import com.skillswap.service.StudentService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration registering existing JDBC DAOs and services as Spring Beans.
 * Ensures 100% code reuse of existing business and database access logic.
 */
@Configuration
public class AppConfig {

    @Bean
    public StudentDAO studentDAO() {
        return new StudentDAO();
    }

    @Bean
    public SkillDAO skillDAO() {
        return new SkillDAO();
    }

    @Bean
    public ExchangeRequestDAO exchangeRequestDAO() {
        return new ExchangeRequestDAO();
    }

    @Bean
    public StudentService studentService(StudentDAO studentDAO) {
        return new StudentService(studentDAO);
    }

    @Bean
    public SkillService skillService(SkillDAO skillDAO, StudentDAO studentDAO) {
        return new SkillService(skillDAO, studentDAO);
    }

    @Bean
    public SkillMatchingService skillMatchingService(StudentDAO studentDAO) {
        return new SkillMatchingService(studentDAO);
    }

    @Bean
    public ExchangeRequestService exchangeRequestService(ExchangeRequestDAO requestDAO, StudentDAO studentDAO, SkillDAO skillDAO) {
        return new ExchangeRequestService(requestDAO, studentDAO, skillDAO);
    }
}

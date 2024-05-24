package com.junioroffers;

import com.junioroffers.domain.loginandregister.LoginRepository;
import com.junioroffers.domain.offer.OfferRepository;
import com.junioroffers.infrastructure.security.jwt.JwtConfigurationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableConfigurationProperties(value = {JwtConfigurationProperties.class})
@EnableMongoRepositories(basePackageClasses = {LoginRepository.class, OfferRepository.class})
public class JobOffersApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobOffersApplication.class, args);
    }
}

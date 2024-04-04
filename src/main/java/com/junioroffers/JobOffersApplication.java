package com.junioroffers;

import com.junioroffers.domain.offer.OfferRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackageClasses = OfferRepository.class)
public class JobOffersApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobOffersApplication.class, args);
    }
}


package com.junioroffers.infrastructure.offer.scheduler;

import com.junioroffers.domain.offer.OfferFacade;
import com.junioroffers.domain.offer.dto.OfferResponseDto;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Component
@AllArgsConstructor
@Log4j2
public class HttpOffersScheduler {

    private final OfferFacade offerFecade;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Scheduled(fixedDelayString = "${http.offers.scheduler.request.delay}")
    public List<OfferResponseDto> fetchAllOffersAndSaveAllIfNotExists() {
        log.info("Started fetching offers at {}", dateFormat.format(new Date()));
        List<OfferResponseDto> addedOfferDtos = offerFecade.fetchAllOffersAndSaveAllIfNotExists();
        log.info("Added new {} offers", addedOfferDtos.size());
        log.info("Stopped fetching offers at {}", dateFormat.format(new Date()));
        return addedOfferDtos;
    }
}

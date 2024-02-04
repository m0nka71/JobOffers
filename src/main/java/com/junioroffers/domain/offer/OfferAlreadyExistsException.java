package com.junioroffers.domain.offer;

import java.util.List;

public class OfferAlreadyExistsException extends RuntimeException {

    private final List<String> offerUrls;

    public OfferAlreadyExistsException(String offerUrl) {
        super(String.format("Offer with offerUrl [%s] already exists", offerUrl));
        this.offerUrls = List.of(offerUrl);
    }

    public OfferAlreadyExistsException(String message, List<Offer> offers) {
        super(String.format("error" + message + offers.toString()));
        this.offerUrls = offers.stream().map(Offer::url).toList();
    }
}

package com.junioroffers.domain.offer.dto;

import lombok.Builder;

@Builder
public record OfferDto(String companyName, String position, String salary, String url) {
}

package com.junioroffers.domain.offer;


import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryOfferRepository implements OfferRepository {

    Map<String, Offer> offerDatabase = new ConcurrentHashMap<>();

    @Override
    public List<Offer> findAll() {
        return offerDatabase.values().stream().toList();
    }

    @Override
    public Optional<Offer> findById(String id) {
        return Optional.ofNullable(offerDatabase.get(id));
    }

    @Override
    public Offer save(Offer offerToSave) {
        if(offerDatabase.values().stream().anyMatch(offer -> offer.url().equals(offerToSave.url()))) {
            throw new OfferAlreadyExistsException(offerToSave.url());
        }
        UUID id = UUID.randomUUID();
        Offer offer = new Offer(
                id.toString(),
                offerToSave.companyName(),
                offerToSave.position(),
                offerToSave.salary(),
                offerToSave.url()
        );
        offerDatabase.put(id.toString(), offer);
        return offer;
    }

    @Override
    public boolean existsByOfferUrl(String offerUrl) {
        long count = offerDatabase.values()
                .stream()
                .filter(offer -> offer.url().equals(offerUrl))
                .count();
        return count == 1;
    }

    @Override
    public List<Offer> saveAll(List<Offer> offers) {
        return offers.stream()
                .map(this::save)
                .toList();
    }
}

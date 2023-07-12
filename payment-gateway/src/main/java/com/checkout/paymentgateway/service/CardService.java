package com.checkout.paymentgateway.service;

import com.checkout.paymentgateway.api.request.CardDetailsRequest;
import com.checkout.paymentgateway.exception.NotFoundException;
import com.checkout.paymentgateway.integration.response.TokeniseCardResponse;
import com.checkout.paymentgateway.model.Card;
import com.checkout.paymentgateway.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository repository;


    public Card createCard(CardDetailsRequest request,
                           TokeniseCardResponse tokenizedCard) {
        return repository.saveAndFlush(Card.builder()
                .expiryMonth(request.getExpiryMonth())
                .expiryYear(request.getExpiryYear())
                .nameOnAccount(request.getNameOnCard())
                .maskedReference(tokenizedCard.getMaskedCardNumber())
                .token(tokenizedCard.getToken())
                .build());
    }

    public Optional<Card> getOptionalCardByToken(String token) {
        return repository.findByToken(token);
    }

    public Card getCardByToken(String token) {
        return repository.findByToken(token).
                orElseThrow(() -> new NotFoundException("Card does not exist."));
    }

}

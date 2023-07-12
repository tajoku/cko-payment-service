package com.checkout.cko.service;

import com.checkout.cko.api.request.TokiniseCardRequest;
import com.checkout.cko.enums.CardStatus;
import com.checkout.cko.enums.CardType;
import com.checkout.cko.exception.CardExpiredException;
import com.checkout.cko.exception.CardNotActiveException;
import com.checkout.cko.exception.NotFoundException;
import com.checkout.cko.exception.UnknownCardProcessorException;
import com.checkout.cko.model.Card;
import com.checkout.cko.repository.CardRepository;
import com.checkout.cko.service.processor.CardProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;

    private final Map<CardType, CardProcessor> cardProcessors;


    public Card getCard(TokiniseCardRequest request) {
        Card card = findCard(request.getCardNumber(),
                request.getCvv(),
                request.getNameOnCard(),
                request.getExpiryMonth(),
                request.getExpiryYear());


        if (isCardExpired(card)) {
            throw new CardExpiredException("This card is expired");
        }

        if (card.getCardStatus().equals(CardStatus.ACTIVE)) {
            return card;
        }

        throw new CardNotActiveException("This card is blocked");

    }


    public Card findCardByToken(String token) {
        return cardRepository.findByToken(token).
                orElseThrow(() -> new NotFoundException("Card does not exist."));
    }


    public CardProcessor getCardProcessor(Card card) {
        if (!cardProcessors.containsKey(card.getCardType())) {
            throw new UnknownCardProcessorException("Card type " + card.getCardType() + " has no configured processor");
        }

        return cardProcessors.get(card.getCardType());
    }

    private boolean isCardExpired(Card card) {
        if (card.getCardStatus().equals(CardStatus.EXPIRED)) {
            return true;
        }

        // Create expected expiry date time and sets last day of the month
        LocalDate localDate = LocalDate.of(card.getExpiryYear(), card.getExpiryMonth(),
                1);
        localDate = localDate.withDayOfMonth(localDate.lengthOfMonth());

        // Checks if the date is older than today and marks the card as expired
        if (localDate.isBefore(LocalDate.now())) {
            card.setCardStatus(CardStatus.EXPIRED);
            cardRepository.saveAndFlush(card);
            return true;
        }
        return false;
    }


    private Card findCard(String cardNumber,
                          String cvv,
                          String nameOnCard,
                          Integer expiryMonth,
                          Integer expiryYear) {
        return cardRepository.findByCardNumberAndCvvAndNameOnCardAndExpiryMonthAndExpiryYear(cardNumber,
                        cvv, nameOnCard, expiryMonth, expiryYear).
                orElseThrow(() -> new NotFoundException("Card does not exist."));
    }
}

package com.checkout.cko.service;

import com.checkout.cko.api.request.TokiniseCardRequest;
import com.checkout.cko.enums.CardStatus;
import com.checkout.cko.enums.CardType;
import com.checkout.cko.exception.CardExpiredException;
import com.checkout.cko.exception.CardNotActiveException;
import com.checkout.cko.exception.NotFoundException;
import com.checkout.cko.exception.UnknownCardProcessorException;
import com.checkout.cko.model.Card;
import com.checkout.cko.model.Payment;
import com.checkout.cko.repository.CardRepository;
import com.checkout.cko.service.processor.CardProcessor;
import com.checkout.cko.service.processor.VisaProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private VisaProcessor visaProcessor;


    private Map<CardType, CardProcessor> cardProcessors;

    private CardService cardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cardProcessors = new HashMap<>();
        cardProcessors.put(CardType.VISA, visaProcessor);
        cardService = new CardService(cardRepository, cardProcessors);
    }

    @Test
    void getCard_ValidCard_ShouldReturnCard() {
        String cardNumber = "123456789";
        String cvv = "123";
        String nameOnCard = "John Doe";
        Integer expiryMonth = 10;
        Integer expiryYear = 2023;
        TokiniseCardRequest request = TokiniseCardRequest.builder()
                .cardNumber(cardNumber)
                .nameOnCard(nameOnCard)
                .cvv(cvv)
                .expiryMonth(expiryMonth)
                .expiryYear(expiryYear)
                .build();
        Card card = Card.builder()
                .cardNumber(cardNumber)
                .nameOnCard(nameOnCard)
                .cvv(cvv)
                .cardStatus(CardStatus.ACTIVE)
                .expiryMonth(expiryMonth)
                .expiryYear(expiryYear)
                .build();

        when(cardRepository.findByCardNumberAndCvvAndNameOnCardAndExpiryMonthAndExpiryYear(
                cardNumber, cvv, nameOnCard, expiryMonth, expiryYear))
                .thenReturn(Optional.of(card));

        // Call the method under test
        Card result = cardService.getCard(request);

        // Assert the result
        assertEquals(card, result);
    }

    @Test
    void getCard_ExpiredCard_ShouldThrowCardExpiredException() {
        String cardNumber = "123456789";
        String cvv = "123";
        String nameOnCard = "John Doe";
        Integer expiryMonth = 10;
        Integer expiryYear = LocalDate.now().getYear() - 1;
        TokiniseCardRequest request = TokiniseCardRequest.builder()
                .cardNumber(cardNumber)
                .nameOnCard(nameOnCard)
                .cvv(cvv)
                .expiryMonth(expiryMonth)
                .expiryYear(expiryYear)
                .build();
        Card card = Card.builder()
                .cardNumber(cardNumber)
                .nameOnCard(nameOnCard)
                .cvv(cvv)
                .cardStatus(CardStatus.ACTIVE)
                .expiryMonth(expiryMonth)
                .expiryYear(expiryYear)
                .build();

        when(cardRepository.findByCardNumberAndCvvAndNameOnCardAndExpiryMonthAndExpiryYear(
                cardNumber, cvv, nameOnCard, expiryMonth, expiryYear))
                .thenReturn(Optional.of(card));

        // Call the method under test and assert the exception
        assertThrows(CardExpiredException.class, () -> cardService.getCard(request));
    }

    @Test
    void getCard_BlockedCard_ShouldThrowCardNotActiveException() {
        // Mock data
        String cardNumber = "123456789";
        String cvv = "123";
        String nameOnCard = "John Doe";
        Integer expiryMonth = 10;
        Integer expiryYear = 2023;
        TokiniseCardRequest request = TokiniseCardRequest.builder()
                .cardNumber(cardNumber)
                .nameOnCard(nameOnCard)
                .cvv(cvv)
                .expiryMonth(expiryMonth)
                .expiryYear(expiryYear)
                .build();
        Card card = Card.builder()
                .cardNumber(cardNumber)
                .nameOnCard(nameOnCard)
                .cvv(cvv)
                .cardStatus(CardStatus.BLOCKED)
                .expiryMonth(expiryMonth)
                .expiryYear(expiryYear)
                .build();

        when(cardRepository.findByCardNumberAndCvvAndNameOnCardAndExpiryMonthAndExpiryYear(
                cardNumber, cvv, nameOnCard, expiryMonth, expiryYear))
                .thenReturn(Optional.of(card));

        // Call the method under test and assert the exception
        assertThrows(CardNotActiveException.class, () -> cardService.getCard(request));
    }

    @Test
    void findCardByToken_ValidToken_ShouldReturnCard() {
        // Mock data
        String token = "abc123";
        Card card = new Card();

        // Mock repository
        when(cardRepository.findByToken(token)).thenReturn(Optional.of(card));

        // Call the method under test
        Card result = cardService.findCardByToken(token);

        // Assert the result
        assertEquals(card, result);
    }

    @Test
    void findCardByToken_NonExistentToken_ShouldThrowNotFoundException() {
        // Mock data
        String token = "nonexistent";

        // Mock repository
        when(cardRepository.findByToken(token)).thenReturn(Optional.empty());

        // Call the method under test and assert the exception
        assertThrows(NotFoundException.class, () -> cardService.findCardByToken(token));
    }

    @Test
    void getCardProcessor_ValidCard_ShouldReturnCardProcessor() {
        // Mock data
        Card card = new Card();
        card.setCardType(CardType.VISA);

        when(visaProcessor.processPayment(any(Payment.class))).thenReturn(true);

        // Call the method under test
        CardProcessor result = cardService.getCardProcessor(card);

        // Assert the result
        assertEquals(visaProcessor, result);
    }

    @Test
    void getCardProcessor_UnknownCardType_ShouldThrowUnknownCardProcessorException() {
        // Mock data
        Card card = new Card();
        card.setCardType(CardType.MASTERCARD);

        // Call the method under test and assert the exception
        assertThrows(UnknownCardProcessorException.class, () -> cardService.getCardProcessor(card));
    }
}
package com.checkout.paymentgateway.service;

import com.checkout.paymentgateway.api.request.CardDetailsRequest;
import com.checkout.paymentgateway.integration.response.TokeniseCardResponse;
import com.checkout.paymentgateway.model.Card;
import com.checkout.paymentgateway.repository.CardRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    private CardService cardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cardService = new CardService(cardRepository);
    }

    @Test
    void testCreateCard() {
        CardDetailsRequest request = CardDetailsRequest.builder()
                .cardNumber("5555555551234").nameOnCard("John Doe").cvv("123").expiryMonth(10).expiryYear(2025).build();

        TokeniseCardResponse tokenizedCard = new TokeniseCardResponse("token123", "************1234");

        when(cardRepository.saveAndFlush(any(Card.class))).then((result) -> {
            Card card = result.getArgument(0);
            assertEquals("token123", card.getToken());
            assertEquals("************1234", card.getMaskedReference());
            assertEquals("John Doe", card.getNameOnAccount());
            assertEquals(10, card.getExpiryMonth());
            assertEquals(2025, card.getExpiryYear());
            return card;
        });

        Card createdCard = cardService.createCard(request, tokenizedCard);

        verify(cardRepository, times(1)).saveAndFlush(any(Card.class));
        Assertions.assertNotNull(createdCard);
    }

}
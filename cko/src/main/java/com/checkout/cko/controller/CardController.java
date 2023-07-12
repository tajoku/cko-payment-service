package com.checkout.cko.controller;

import com.checkout.cko.api.request.TokiniseCardRequest;
import com.checkout.cko.api.response.TokeniseCardResponse;
import com.checkout.cko.model.Card;
import com.checkout.cko.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @PostMapping(path = "/api/cards/tokenise-card")
    public ResponseEntity<TokeniseCardResponse> tokeniseCard(@RequestBody TokiniseCardRequest request) {
        Card card = cardService.getCard(request);
        return ResponseEntity.status(HttpStatus.OK).body(TokeniseCardResponse.builder().
                token(card.getToken()).maskedCardNumber(card.getMaskedCardNumber()).build());
    }
}

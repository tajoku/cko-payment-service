package com.checkout.cko.service.processor;

import com.checkout.cko.enums.CardType;
import com.checkout.cko.model.Payment;
import org.springframework.stereotype.Component;

@Component
public class AmexProcessor implements CardProcessor {
    @Override
    public CardType getCardType() {
        return CardType.AMERICAN_EXPRESS;
    }

    @Override
    public boolean processPayment(Payment payment) {
        return true;
    }
}

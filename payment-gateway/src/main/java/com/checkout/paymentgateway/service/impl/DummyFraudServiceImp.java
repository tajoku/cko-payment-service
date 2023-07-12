package com.checkout.paymentgateway.service.impl;

import com.checkout.paymentgateway.model.Card;
import com.checkout.paymentgateway.service.FraudService;
import org.springframework.stereotype.Service;

@Service
public class DummyFraudServiceImp implements FraudService {
    @Override
    public boolean isCardFraudulent(Card card) {
        return false;
    }
}

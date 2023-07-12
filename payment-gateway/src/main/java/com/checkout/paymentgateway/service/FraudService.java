package com.checkout.paymentgateway.service;

import com.checkout.paymentgateway.model.Card;


public interface FraudService {

    boolean isCardFraudulent(Card card);
}

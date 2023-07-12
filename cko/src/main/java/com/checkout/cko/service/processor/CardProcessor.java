package com.checkout.cko.service.processor;

import com.checkout.cko.enums.CardType;
import com.checkout.cko.model.Payment;

public interface CardProcessor {

    CardType getCardType();

    boolean processPayment(Payment payment);
}

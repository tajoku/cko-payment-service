package com.checkout.paymentgateway.service;

import com.checkout.paymentgateway.enums.PaymentStatus;


public interface NotificationService {

    boolean notifyMerchant(String webHook, String reference, PaymentStatus paymentStatus);
}

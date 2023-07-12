package com.checkout.paymentgateway.service.impl;

import com.checkout.paymentgateway.enums.PaymentStatus;
import com.checkout.paymentgateway.service.NotificationService;
import org.springframework.stereotype.Service;

@Service
public class DummyNotificationServiceImpl implements NotificationService {
    @Override
    public boolean notifyMerchant(String webHook, String reference, PaymentStatus paymentStatus) {
        return true;
    }
}

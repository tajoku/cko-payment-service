package com.checkout.paymentgateway.api.response;

import com.checkout.paymentgateway.enums.Currency;
import com.checkout.paymentgateway.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PaymentResponse {

    private Long id;

    private PaymentStatus status;

    private String description;

    private String reference;

    private Long amountInCents;

    private Currency currency;

    private String narration;

    private Date submittedAt;

}

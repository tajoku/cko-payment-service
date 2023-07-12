package com.checkout.paymentgateway.integration.response;

import com.checkout.paymentgateway.enums.Currency;
import com.checkout.paymentgateway.enums.PaymentStatus;
import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class GetPaymentResponse {

    private Long id;

    private PaymentStatus status;

    private String reference;

    private Long amountInCents;

    private Currency currency;

    private String beneficiaryName;

    private String narration;

    private String description;

    private Date createdAt;
}

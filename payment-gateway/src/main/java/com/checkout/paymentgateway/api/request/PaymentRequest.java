package com.checkout.paymentgateway.api.request;

import com.checkout.paymentgateway.enums.Currency;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PaymentRequest {

    @NonNull
    private String reference;

    private String narration;

    @NonNull
    private Long amountInCents;

    @NonNull
    private Currency currency;

}

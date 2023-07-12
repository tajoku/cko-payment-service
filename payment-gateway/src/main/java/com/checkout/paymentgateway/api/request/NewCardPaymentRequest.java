package com.checkout.paymentgateway.api.request;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class NewCardPaymentRequest extends PaymentRequest {

    @NonNull
    private CardDetailsRequest cardDetails;
}

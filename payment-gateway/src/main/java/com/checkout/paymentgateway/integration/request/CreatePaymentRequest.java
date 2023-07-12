package com.checkout.paymentgateway.integration.request;

import com.checkout.paymentgateway.enums.Currency;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CreatePaymentRequest {

    @NonNull
    private String reference;

    private String narration;

    @NonNull
    private Long amountInCents;

    @NonNull
    private Currency currency;

    @NonNull
    private String beneficiaryName;

    @NonNull
    private String beneficiaryAccount;

    @NonNull
    private String beneficiaryBank;

    @NonNull
    private String cardToken;

    private String callbackUrl;

}

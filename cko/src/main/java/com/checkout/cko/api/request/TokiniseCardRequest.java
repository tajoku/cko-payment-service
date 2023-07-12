package com.checkout.cko.api.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TokiniseCardRequest {

    @NonNull
    private String nameOnCard;

    @NonNull
    private String cardNumber;

    @NonNull
    private String cvv;

    @NonNull
    private Integer expiryMonth;

    @NonNull
    private Integer expiryYear;

}

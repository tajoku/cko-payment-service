package com.checkout.cko.api.response;

import com.checkout.cko.enums.Currency;
import com.checkout.cko.enums.PaymentStatus;
import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PaymentResponse {

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

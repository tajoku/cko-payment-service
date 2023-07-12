package com.checkout.cko.api.response;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class GetPaymentsResponse {

    private List<PaymentResponse> payments;
}

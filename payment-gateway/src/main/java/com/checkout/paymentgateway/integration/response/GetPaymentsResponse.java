package com.checkout.paymentgateway.integration.response;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class GetPaymentsResponse {

    private List<GetPaymentResponse> payments;
}

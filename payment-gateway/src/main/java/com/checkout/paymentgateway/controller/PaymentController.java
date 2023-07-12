package com.checkout.paymentgateway.controller;

import com.checkout.paymentgateway.api.request.CardPaymentRequest;
import com.checkout.paymentgateway.api.request.NewCardPaymentRequest;
import com.checkout.paymentgateway.api.response.CardDetailsResponse;
import com.checkout.paymentgateway.api.response.GetPaymentsResponse;
import com.checkout.paymentgateway.api.response.PaymentDetailsResponse;
import com.checkout.paymentgateway.api.response.PaymentResponse;
import com.checkout.paymentgateway.enums.PaymentStatus;
import com.checkout.paymentgateway.model.Card;
import com.checkout.paymentgateway.model.Payment;
import com.checkout.paymentgateway.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;


    @PostMapping(path = "/api/{merchantId}/pay/new-card")
    public ResponseEntity<PaymentResponse> createNewCardPayment(@PathVariable Long merchantId,
                                                                @RequestBody NewCardPaymentRequest request) {
        Payment payment = paymentService.processNewCardPayment(merchantId, request);
        return paymentToPaymentResponse(payment);
    }


    @PostMapping(path = "/api/{merchantId}/pay/charge-card")
    public ResponseEntity<PaymentResponse> createCardPayment(@PathVariable Long merchantId,
                                                             @RequestBody CardPaymentRequest request) {
        Payment payment = paymentService.processCardPayment(merchantId, request);
        return paymentToPaymentResponse(payment);
    }

    @GetMapping(path = "/api/{merchantId}/payments")
    public ResponseEntity<GetPaymentsResponse> getPayments(@PathVariable Long merchantId) {
        List<Payment> payments = paymentService.getMerchantPayments(merchantId);
        return ResponseEntity.status(HttpStatus.OK).body(
                GetPaymentsResponse.builder()
                        .payments(payments.stream()
                                .map(payment -> PaymentResponse.builder()
                                        .id(payment.getId())
                                        .amountInCents(payment.getAmountInCents())
                                        .currency(payment.getCurrency())
                                        .status(payment.getStatus())
                                        .description(payment.getStatusDescription())
                                        .reference(payment.getPaymentReference())
                                        .narration(payment.getDescription())
                                        .submittedAt(payment.getSubmittedAt())
                                        .build()
                                )
                                .collect(Collectors.toList()))
                        .build()
        );
    }

    @GetMapping(path = "/api/{merchantId}/payments/{paymentId}")
    public ResponseEntity<PaymentDetailsResponse> createCardPayment(@PathVariable Long merchantId,
                                                                    @PathVariable Long paymentId,
                                                                    @RequestParam(name = "refresh", required = false) boolean refresh) {
        Payment payment = paymentService.getPayment(merchantId, paymentId, refresh);
        Card card = payment.getCard();
        return ResponseEntity.status(HttpStatus.OK).body(
                PaymentDetailsResponse.builder()
                        .id(payment.getId())
                        .amountInCents(payment.getAmountInCents())
                        .currency(payment.getCurrency())
                        .status(payment.getStatus())
                        .description(payment.getStatusDescription())
                        .reference(payment.getPaymentReference())
                        .narration(payment.getDescription())
                        .submittedAt(payment.getSubmittedAt())
                        .cardDetails(CardDetailsResponse.builder()
                                .maskedCardNumber(card.getMaskedReference())
                                .cardToken(card.getToken())
                                .nameOnCard(card.getNameOnAccount())
                                .expiryMonth(card.getExpiryMonth())
                                .expiryYear(card.getExpiryYear())
                                .build())
                        .build());
    }


    @GetMapping(path = "/api/{merchantId}/{paymentReference}/callback")
    public ResponseEntity<Void> processCallback(@PathVariable Long merchantId,
                                                @PathVariable String paymentReference,
                                                @RequestParam(name = "status") PaymentStatus status,
                                                @RequestParam(name = "description") String description) {
        paymentService.processCallback(merchantId, paymentReference, status, description);
        return ResponseEntity.ok().build();
    }

    private ResponseEntity<PaymentResponse> paymentToPaymentResponse(Payment payment) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                PaymentResponse.builder()
                        .id(payment.getId())
                        .amountInCents(payment.getAmountInCents())
                        .currency(payment.getCurrency())
                        .status(payment.getStatus())
                        .description(payment.getStatusDescription())
                        .reference(payment.getPaymentReference())
                        .narration(payment.getDescription())
                        .submittedAt(payment.getSubmittedAt())
                        .build());
    }
}

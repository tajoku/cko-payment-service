package com.checkout.cko.controller;

import com.checkout.cko.api.request.CreatePaymentRequest;
import com.checkout.cko.api.response.GetPaymentsResponse;
import com.checkout.cko.api.response.PaymentResponse;
import com.checkout.cko.model.Payment;
import com.checkout.cko.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping(path = "/api/payments")
    public ResponseEntity<PaymentResponse> submitPayment(
            @RequestHeader(value = "X-Idempotency-Key") String idempotencyKey,
            @RequestBody CreatePaymentRequest request) {
        Payment payment = paymentService.submitPayment(request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(PaymentResponse.builder()
                .id(payment.getId())
                .amountInCents(payment.getAmountInCents())
                .beneficiaryName(payment.getBeneficiaryName())
                .createdAt(payment.getCreatedAt())
                .currency(payment.getCurrency())
                .description(payment.getDescription())
                .narration(payment.getNarration())
                .reference(payment.getReference())
                .status(payment.getPaymentStatus())
                .build());
    }

    @GetMapping(path = "/api/payments/{id}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable(name = "id") Long id) {
        Payment payment = paymentService.findById(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(PaymentResponse.builder()
                .id(payment.getId())
                .amountInCents(payment.getAmountInCents())
                .beneficiaryName(payment.getBeneficiaryName())
                .createdAt(payment.getCreatedAt())
                .currency(payment.getCurrency())
                .description(payment.getDescription())
                .narration(payment.getNarration())
                .reference(payment.getReference())
                .status(payment.getPaymentStatus())
                .build());
    }

    @GetMapping(path = "/api/payments/account/{account}")
    public ResponseEntity<GetPaymentsResponse> getAccountPayments(@PathVariable(name = "account") String account) {
        return ResponseEntity.status(HttpStatus.CREATED).body(GetPaymentsResponse.builder()
                .payments(paymentService.getAllPaymentsByAccount(account).stream()
                        .map(payment -> PaymentResponse.builder()
                                .id(payment.getId())
                                .amountInCents(payment.getAmountInCents())
                                .beneficiaryName(payment.getBeneficiaryName())
                                .createdAt(payment.getCreatedAt())
                                .currency(payment.getCurrency())
                                .description(payment.getDescription())
                                .narration(payment.getNarration())
                                .reference(payment.getReference())
                                .status(payment.getPaymentStatus())
                                .build())
                        .collect(Collectors.toList()))
                .build());
    }
}

package com.checkout.cko.service;

import com.checkout.cko.api.request.CreatePaymentRequest;
import com.checkout.cko.enums.CardStatus;
import com.checkout.cko.enums.Currency;
import com.checkout.cko.enums.PaymentStatus;
import com.checkout.cko.exception.CardNotActiveException;
import com.checkout.cko.exception.InvalidCurrencyException;
import com.checkout.cko.exception.NotFoundException;
import com.checkout.cko.model.Card;
import com.checkout.cko.model.Payment;
import com.checkout.cko.repository.PaymentRepository;
import com.checkout.cko.service.processor.CardProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private CardService cardService;

    @Mock
    private CallbackService callbackService;


    @Mock
    private CardProcessor cardProcessor;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        paymentService = new PaymentService(paymentRepository, cardService, callbackService);
    }

    @Test
    void testSubmitPayment_IdempotentPaymentExists() {
        String idempotencyKey = UUID.randomUUID().toString();
        String reference = "123456";
        String beneficiaryAccount = "20000";
        String beneficiaryBank = "CKO";
        String beneficiaryName = "John Doe";

        CreatePaymentRequest request = CreatePaymentRequest.builder()
                .reference(reference)
                .amountInCents(100L)
                .currency(Currency.GBP)
                .beneficiaryAccount(beneficiaryAccount)
                .beneficiaryBank(beneficiaryBank)
                .beneficiaryName(beneficiaryName)
                .cardToken("token")
                .build();
        Payment idempotentPayment = Payment.builder()
                .idempotencyKey(idempotencyKey)
                .reference(reference)
                .currency(Currency.GBP)
                .amountInCents(100L)
                .beneficiaryAccount(beneficiaryAccount)
                .build();

        when(paymentRepository.findFirstByIdempotencyKey(
                eq(idempotencyKey), eq(PaymentStatus.SUCCESS), any(Date.class)))
                .thenReturn(Optional.of(idempotentPayment));

        Payment submittedPayment = paymentService.submitPayment(request, idempotencyKey);

        verify(paymentRepository, never()).saveAndFlush(any(Payment.class));
        assertSame(idempotentPayment, submittedPayment);
    }

    @Test
    void testSubmitPayment_SuccessfulPayment() {
        String idempotencyKey = UUID.randomUUID().toString();
        String cardToken = UUID.randomUUID().toString();
        CreatePaymentRequest request = CreatePaymentRequest.builder()
                .cardToken(cardToken)
                .amountInCents(100L)
                .currency(Currency.GBP)
                .reference("123")
                .beneficiaryName("John e")
                .beneficiaryBank("cko")
                .beneficiaryAccount("200000")
                .build();
        Card card = Card.builder()
                .cardStatus(CardStatus.ACTIVE)
                .token(cardToken)
                .currency(Currency.GBP)
                .build();

        when(cardService.findCardByToken(cardToken)).thenReturn(card);

        Payment payment = Payment.builder()
                .amountInCents(100L)
                .currency(Currency.GBP)
                .reference("123")
                .beneficiaryName("John e")
                .beneficiaryBank("cko")
                .beneficiaryAccount("200000")
                .idempotencyKey("2222")
                .card(card)
                .build();

        when(cardService.findCardByToken(anyString()))
                .thenReturn(card);
        when(paymentRepository.saveAndFlush(any(Payment.class)))
                .thenReturn(payment);

        when(cardService.getCardProcessor(card)).thenReturn(cardProcessor);
        when(cardProcessor.processPayment(payment)).thenReturn(true);

        Payment submittedPayment = paymentService.submitPayment(request, idempotencyKey);

        verify(cardService, times(1)).findCardByToken(anyString());
        verify(paymentRepository, times(1)).saveAndFlush(any(Payment.class));
        assertSame(payment, submittedPayment);
    }

    @Test
    void testSubmitPayment_CardNotActiveException() {
        String idempotencyKey = UUID.randomUUID().toString();
        String cardToken = UUID.randomUUID().toString();
        CreatePaymentRequest request = CreatePaymentRequest.builder()
                .cardToken(cardToken)
                .amountInCents(100L)
                .currency(Currency.GBP)
                .reference("123")
                .beneficiaryName("John e")
                .beneficiaryBank("cko")
                .beneficiaryAccount("200000")
                .build();
        Card card = Card.builder().cardStatus(CardStatus.BLOCKED).token(cardToken).build();

        when(cardService.findCardByToken(cardToken)).thenReturn(card);

        assertThrows(CardNotActiveException.class, () -> {
            paymentService.submitPayment(request, idempotencyKey);
        });

        verify(cardService, times(1)).findCardByToken(anyString());
        verify(paymentRepository, never()).saveAndFlush(any(Payment.class));
    }

    @Test
    void testSubmitPayment_InvalidCurrencyException() {
        String idempotencyKey = UUID.randomUUID().toString();
        String cardToken = UUID.randomUUID().toString();
        CreatePaymentRequest request = CreatePaymentRequest.builder()
                .cardToken(cardToken)
                .amountInCents(100L)
                .currency(Currency.GBP)
                .reference("123")
                .beneficiaryName("John e")
                .beneficiaryBank("cko")
                .beneficiaryAccount("200000")
                .build();
        Card card = Card.builder()
                .cardStatus(CardStatus.ACTIVE)
                .token(cardToken)
                .currency(Currency.EUR)
                .build();

        when(cardService.findCardByToken(cardToken)).thenReturn(card);
        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(new Payment());

        assertThrows(InvalidCurrencyException.class, () -> {
            paymentService.submitPayment(request, idempotencyKey);
        });

        verify(cardService, times(1)).findCardByToken(anyString());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void testFindById() {
        Long id = 1L;
        Payment payment = new Payment();

        when(paymentRepository.findById(id)).thenReturn(Optional.of(payment));

        Payment retrievedPayment = paymentService.findById(id);

        verify(paymentRepository, times(1)).findById(id);
        assertSame(payment, retrievedPayment);
    }

    @Test
    void testFindByIdNotFound() {
        Long id = 1L;

        when(paymentRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            paymentService.findById(id);
        });

        verify(paymentRepository, times(1)).findById(id);
    }

    @Test
    void testGetAllPaymentsByAccount() {
        String beneficiaryAccount = "account123";
        List<Payment> paymentList = List.of(new Payment());

        when(paymentRepository.findAllByBeneficiaryAccount(beneficiaryAccount)).thenReturn(paymentList);

        List<Payment> retrievedPayments = paymentService.getAllPaymentsByAccount(beneficiaryAccount);

        verify(paymentRepository, times(1)).findAllByBeneficiaryAccount(beneficiaryAccount);
        assertSame(paymentList, retrievedPayments);
    }

}
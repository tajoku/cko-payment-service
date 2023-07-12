package com.checkout.paymentgateway.service;

import com.checkout.paymentgateway.api.request.CardDetailsRequest;
import com.checkout.paymentgateway.api.request.CardPaymentRequest;
import com.checkout.paymentgateway.api.request.NewCardPaymentRequest;
import com.checkout.paymentgateway.enums.Currency;
import com.checkout.paymentgateway.enums.PaymentStatus;
import com.checkout.paymentgateway.integration.CkoClient;
import com.checkout.paymentgateway.integration.request.CreatePaymentRequest;
import com.checkout.paymentgateway.integration.request.TokeniseCardRequest;
import com.checkout.paymentgateway.integration.response.GetPaymentResponse;
import com.checkout.paymentgateway.integration.response.TokeniseCardResponse;
import com.checkout.paymentgateway.model.Card;
import com.checkout.paymentgateway.model.Merchant;
import com.checkout.paymentgateway.model.Payment;
import com.checkout.paymentgateway.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private MerchantService merchantService;

    @Mock
    private CardService cardService;

    @Mock
    private FraudService fraudService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private CkoClient ckoClient;

    private String appUrl = "http://example.com";

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        paymentService = new PaymentService(paymentRepository, merchantService, cardService,
                notificationService, fraudService, ckoClient);

        ReflectionTestUtils.setField(paymentService, "appUrl", appUrl);
    }


    @Test
    void processNewCardPayment_ShouldCreatePaymentWithCardDetails() {
        // Mock the dependencies
        Long merchantId = 1L;
        String cardToken = "token123";
        Long thirdPartyPaymentId = 12L;
        String idempotencyKey = "123344";

        GetPaymentResponse paymentResponse = GetPaymentResponse.builder()
                .id(thirdPartyPaymentId)
                .status(PaymentStatus.PENDING)
                .build();
        NewCardPaymentRequest request = new NewCardPaymentRequest();
        CardDetailsRequest cardDetailsRequest = CardDetailsRequest.builder()
                .cardNumber("5555555551234")
                .nameOnCard("John Doe")
                .cvv("123")
                .expiryMonth(10)
                .expiryYear(2025)
                .build();
        request.setCardDetails(cardDetailsRequest);
        TokeniseCardRequest tokeniseCardRequest = TokeniseCardRequest.builder()
                .cardNumber("5555555551234")
                .nameOnCard("John Doe")
                .cvv("123")
                .expiryMonth(10)
                .expiryYear(2025)
                .build();
        TokeniseCardResponse tokenizedCard = TokeniseCardResponse.builder()
                .token(cardToken)
                .maskedCardNumber("************1234")
                .build();

        Merchant merchant = Merchant.builder()
                .id(merchantId)
                .name("John e")
                .bankName("cko")
                .bankAccountNumber("200000")
                .build();
        Card card = Card.builder().token(cardToken).build();
        Payment payment = Payment.builder()
                .amountInCents(100L)
                .currency(Currency.GBP)
                .paymentReference("123")
                .card(card)
                .merchant(merchant)
                .idempotencyKey(idempotencyKey)
                .build();
        CreatePaymentRequest createPaymentRequest = CreatePaymentRequest.builder()
                .cardToken(cardToken)
                .amountInCents(100L)
                .currency(Currency.GBP)
                .reference("123")
                .beneficiaryName("John e")
                .beneficiaryBank("cko")
                .beneficiaryAccount("200000")
                .callbackUrl(String.format("%s/api/%s/%s/callback", appUrl, merchant.getId(), payment.getPaymentReference()))
                .build();

        when(merchantService.getMerchant(merchantId)).thenReturn(merchant);
        when(cardService.createCard(eq(cardDetailsRequest), any(TokeniseCardResponse.class))).thenReturn(card);
        when(cardService.getOptionalCardByToken(eq(cardToken))).thenReturn(Optional.empty());
        when(ckoClient.tokeniseCard(tokeniseCardRequest)).thenReturn(tokenizedCard);
        when(ckoClient.submitPayment(createPaymentRequest, idempotencyKey)).thenReturn(Mono.just(paymentResponse));
        when(paymentRepository.saveAndFlush(any(Payment.class))).thenReturn(payment);

        // Invoke the method
        paymentService.processNewCardPayment(merchantId, request);

        // Verify the interactions
        verify(merchantService).getMerchant(merchantId);
        verify(cardService).createCard(request.getCardDetails(), tokenizedCard);
        verify(cardService).getOptionalCardByToken(cardToken);
        verify(paymentRepository).saveAndFlush(any(Payment.class));
        verify(paymentRepository).save(any(Payment.class));
        verify(ckoClient).tokeniseCard(tokeniseCardRequest);
        verify(ckoClient).submitPayment(createPaymentRequest, idempotencyKey);
        verifyNoMoreInteractions(merchantService, cardService, paymentRepository);
    }


    @Test
    void processNewCardPayment_ShouldCreatePaymentWithExistingCardDetails() {
        // Mock the dependencies
        Long merchantId = 1L;
        String cardToken = "token123";
        Long thirdPartyPaymentId = 12L;
        String idempotencyKey = "123344";

        GetPaymentResponse paymentResponse = GetPaymentResponse.builder()
                .id(thirdPartyPaymentId)
                .status(PaymentStatus.PENDING)
                .build();
        NewCardPaymentRequest request = new NewCardPaymentRequest();
        CardDetailsRequest cardDetailsRequest = CardDetailsRequest.builder()
                .cardNumber("5555555551234")
                .nameOnCard("John Doe")
                .cvv("123")
                .expiryMonth(10)
                .expiryYear(2025)
                .build();
        request.setCardDetails(cardDetailsRequest);
        TokeniseCardRequest tokeniseCardRequest = TokeniseCardRequest.builder()
                .cardNumber("5555555551234")
                .nameOnCard("John Doe")
                .cvv("123")
                .expiryMonth(10)
                .expiryYear(2025)
                .build();
        TokeniseCardResponse tokenizedCard = TokeniseCardResponse.builder()
                .token(cardToken)
                .maskedCardNumber("************1234")
                .build();

        Merchant merchant = Merchant.builder()
                .id(merchantId)
                .name("John e")
                .bankName("cko")
                .bankAccountNumber("200000")
                .build();
        Card card = Card.builder().token(cardToken).build();
        Payment payment = Payment.builder()
                .amountInCents(100L)
                .currency(Currency.GBP)
                .paymentReference("123")
                .card(card)
                .merchant(merchant)
                .idempotencyKey(idempotencyKey)
                .build();
        CreatePaymentRequest createPaymentRequest = CreatePaymentRequest.builder()
                .cardToken(cardToken)
                .amountInCents(100L)
                .currency(Currency.GBP)
                .reference("123")
                .beneficiaryName("John e")
                .beneficiaryBank("cko")
                .beneficiaryAccount("200000")
                .callbackUrl(String.format("%s/api/%s/%s/callback", appUrl, merchant.getId(), payment.getPaymentReference()))
                .build();

        when(merchantService.getMerchant(merchantId)).thenReturn(merchant);
        when(cardService.getOptionalCardByToken(eq(cardToken))).thenReturn(Optional.of(card));
        when(ckoClient.tokeniseCard(tokeniseCardRequest)).thenReturn(tokenizedCard);
        when(ckoClient.submitPayment(createPaymentRequest, idempotencyKey)).thenReturn(Mono.just(paymentResponse));
        when(paymentRepository.saveAndFlush(any(Payment.class))).thenReturn(payment);

        // Invoke the method
        paymentService.processNewCardPayment(merchantId, request);

        // Verify the interactions
        verify(merchantService).getMerchant(merchantId);
        verify(cardService).getOptionalCardByToken(cardToken);
        verify(paymentRepository).saveAndFlush(any(Payment.class));
        verify(paymentRepository).save(any(Payment.class));
        verify(ckoClient).tokeniseCard(tokeniseCardRequest);
        verify(ckoClient).submitPayment(createPaymentRequest, idempotencyKey);
        verifyNoMoreInteractions(merchantService, cardService, paymentRepository);
    }

    @Test
    void processCardPayment_ShouldCreatePaymentWithCardToken() {
        // Mock the dependencies
        Long merchantId = 1L;
        Long thirdPartyPaymentId = 12L;
        String idempotencyKey = "12334";
        GetPaymentResponse paymentResponse = GetPaymentResponse.builder()
                .id(thirdPartyPaymentId)
                .status(PaymentStatus.PENDING)
                .build();
        String cardToken = UUID.randomUUID().toString();
        CardPaymentRequest request = CardPaymentRequest.builder()
                .cardToken(cardToken)
                .amountInCents(100L)
                .currency(Currency.GBP)
                .reference("123")
                .build();

        Merchant merchant = Merchant.builder()
                .id(merchantId)
                .name("John e")
                .bankName("cko")
                .bankAccountNumber("200000")
                .build();
        Card card = Card.builder().token(cardToken).build();
        Payment payment = Payment.builder()
                .amountInCents(100L)
                .currency(Currency.GBP)
                .paymentReference("123")
                .card(card)
                .merchant(merchant)
                .idempotencyKey(idempotencyKey)
                .build();

        CreatePaymentRequest createPaymentRequest = CreatePaymentRequest.builder()
                .cardToken(cardToken)
                .amountInCents(100L)
                .currency(Currency.GBP)
                .reference("123")
                .beneficiaryName("John e")
                .beneficiaryBank("cko")
                .beneficiaryAccount("200000")
                .callbackUrl(String.format("%s/api/%s/%s/callback", appUrl, merchant.getId(), payment.getPaymentReference()))
                .build();

        when(paymentRepository.saveAndFlush(any(Payment.class))).thenReturn(payment);
        when(merchantService.getMerchant(merchantId)).thenReturn(merchant);
        when(cardService.getCardByToken(cardToken)).thenReturn(card);
        when(ckoClient.submitPayment(createPaymentRequest, idempotencyKey)).thenReturn(Mono.just(paymentResponse));


        // Invoke the method
        paymentService.processCardPayment(merchantId, request);

        // Verify the interactions
        verify(merchantService).getMerchant(merchantId);
        verify(cardService).getCardByToken(request.getCardToken());
        verify(paymentRepository).saveAndFlush(any(Payment.class));
        verify(paymentRepository).save(any(Payment.class));
        verifyNoMoreInteractions(merchantService, cardService, paymentRepository);
    }

    @Test
    void getPayment_ShouldRetrievePaymentFromRepository() {
        // Mock the dependencies
        Long merchantId = 1L;
        Long paymentId = 1L;
        Long thirdPartyPaymentId = 12L;
        boolean refresh = true;
        Merchant merchant = new Merchant();
        Payment payment = Payment.builder()
                .id(paymentId)
                .status(PaymentStatus.PENDING)
                .thirdPartyId(thirdPartyPaymentId)
                .build();
        GetPaymentResponse paymentResponse = GetPaymentResponse.builder()
                .status(PaymentStatus.SUCCESS)
                .id(thirdPartyPaymentId)
                .build();
        when(merchantService.getMerchant(merchantId)).thenReturn(merchant);
        when(ckoClient.getPayment(thirdPartyPaymentId)).thenReturn(paymentResponse);

        when(paymentRepository.findByIdAndMerchant(paymentId, merchant)).thenReturn(Optional.of(payment));
        when(paymentRepository.saveAndFlush(payment)).thenReturn(payment);

        // Invoke the method
        Payment retrievedPayment = paymentService.getPayment(merchantId, paymentId, refresh);

        // Verify the interactions
        verify(merchantService).getMerchant(merchantId);
        verify(paymentRepository).findByIdAndMerchant(paymentId, merchant);
        verify(ckoClient).getPayment(payment.getThirdPartyId());
        verify(paymentRepository).saveAndFlush(payment);
        verifyNoMoreInteractions(merchantService, paymentRepository, ckoClient);

        assertSame(payment, retrievedPayment);
    }

    @Test
    void getMerchantPayments_ShouldRetrieveMerchantPaymentsFromRepository() {
        // Mock the dependencies
        Long merchantId = 1L;
        Merchant merchant = new Merchant();
        when(merchantService.getMerchant(merchantId)).thenReturn(merchant);
        when(paymentRepository.findAllByMerchant(merchant)).thenReturn(List.of(new Payment(), new Payment()));

        // Invoke the method
        paymentService.getMerchantPayments(merchantId);

        // Verify the interactions
        verify(merchantService).getMerchant(merchantId);
        verify(paymentRepository).findAllByMerchant(merchant);
        verifyNoMoreInteractions(merchantService, paymentRepository);
    }

    @Test
    void processCallback_ShouldUpdatePaymentAndNotifyMerchant() {
        // Mock the dependencies
        Long merchantId = 1L;
        String paymentReference = "paymentReference";
        String description = "Success";
        PaymentStatus paymentStatus = PaymentStatus.SUCCESS;
        Merchant merchant = new Merchant();
        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentReference(paymentReference);
        when(merchantService.getMerchant(merchantId)).thenReturn(merchant);
        when(paymentRepository.findByPaymentReferenceAndMerchant(paymentReference, merchant)).thenReturn(java.util.Optional.of(payment));


        // Invoke the method
        paymentService.processCallback(merchantId, paymentReference, paymentStatus, description);

        // Verify the interactions
        verify(merchantService).getMerchant(merchantId);
        verify(paymentRepository).findByPaymentReferenceAndMerchant(paymentReference, merchant);
        verify(paymentRepository).save(payment);
        verify(notificationService, never()).notifyMerchant("webhookUrl", paymentReference, paymentStatus);
        verifyNoMoreInteractions(merchantService, paymentRepository, notificationService);
    }

}
package com.checkout.paymentgateway.service;

import com.checkout.paymentgateway.api.request.CardDetailsRequest;
import com.checkout.paymentgateway.api.request.CardPaymentRequest;
import com.checkout.paymentgateway.api.request.NewCardPaymentRequest;
import com.checkout.paymentgateway.api.request.PaymentRequest;
import com.checkout.paymentgateway.enums.PaymentStatus;
import com.checkout.paymentgateway.exception.NotFoundException;
import com.checkout.paymentgateway.integration.CkoClient;
import com.checkout.paymentgateway.integration.request.CreatePaymentRequest;
import com.checkout.paymentgateway.integration.request.TokeniseCardRequest;
import com.checkout.paymentgateway.integration.response.GetPaymentResponse;
import com.checkout.paymentgateway.integration.response.TokeniseCardResponse;
import com.checkout.paymentgateway.model.Card;
import com.checkout.paymentgateway.model.Merchant;
import com.checkout.paymentgateway.model.Payment;
import com.checkout.paymentgateway.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    @Value("${app.url}")
    private String appUrl;

    private final PaymentRepository repository;

    private final MerchantService merchantService;

    private final CardService cardService;

    private final NotificationService notificationService;

    private final FraudService fraudService;

    private final CkoClient ckoClient;


    // Processes payment with card details
    public Payment processNewCardPayment(Long merchantId, NewCardPaymentRequest request) {
        Merchant merchant = merchantService.getMerchant(merchantId);
        TokeniseCardResponse tokenizedCard = getTokenizedCard(request.getCardDetails());
        Card card = cardService.getOptionalCardByToken(tokenizedCard.getToken())
                .orElseGet(() -> cardService.createCard(request.getCardDetails(), tokenizedCard));

        return createPayment(merchant, request, card);
    }

    // Processes payment with card token
    public Payment processCardPayment(Long merchantId, CardPaymentRequest request) {
        Merchant merchant = merchantService.getMerchant(merchantId);
        return createPayment(merchant, request, request.getCardToken());
    }

    @Transactional
    public Payment getPayment(Long merchantId, Long paymentId, boolean refresh) {
        Merchant merchant = merchantService.getMerchant(merchantId);
        Payment payment = getPayment(paymentId, merchant);

        if (payment.isStatusTerminal() || !refresh) {
            return payment;
        }

        GetPaymentResponse paymentResponse = getCkoPayment(payment.getThirdPartyId());

        payment.setStatus(paymentResponse.getStatus());

        return repository.saveAndFlush(payment);
    }

    public List<Payment> getMerchantPayments(Long merchantId) {
        Merchant merchant = merchantService.getMerchant(merchantId);
        return repository.findAllByMerchant(merchant);
    }

    @Transactional
    public void processCallback(Long merchantId,
                                String paymentReference,
                                PaymentStatus paymentStatus,
                                String description) {
        try {

            log.info("processing callback from bank");
            Merchant merchant = merchantService.getMerchant(merchantId);
            Payment payment = getPayment(paymentReference, merchant);

            if (!payment.isStatusTerminal()) {
                payment.setStatus(paymentStatus);
                payment.setStatusDescription(description);
                repository.save(payment);
            }
            // Notify the merchant asynchronously if webhook is present
            if (merchant.getWebHookUrl() != null) {
                log.info("Sending webhook to merchant");
                notifyMerchant(merchant.getWebHookUrl(), payment.getPaymentReference(), paymentStatus);
            }

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    private Payment getPayment(Long paymentId, Merchant merchant) {
        return repository.findByIdAndMerchant(paymentId, merchant).
                orElseThrow(() -> new NotFoundException("Payment with ID " + paymentId + " does not exist."));
    }

    private Payment getPayment(String paymentReference, Merchant merchant) {
        return repository.findByPaymentReferenceAndMerchant(paymentReference, merchant).
                orElseThrow(() -> new NotFoundException("Payment with reference " + paymentReference + " does not exist."));
    }

    private Payment createPayment(Merchant merchant, PaymentRequest request, String cardToken) {
        Card card = cardService.getCardByToken(cardToken);
        return createPayment(merchant, request, card);
    }

    @Transactional
    private Payment createPayment(Merchant merchant, PaymentRequest request, Card card) {
        if (fraudService.isCardFraudulent(card)) {
            // Hide actual error
            log.error("Card " + card.getMaskedReference() + " belonging to " + card.getNameOnAccount() + " is fraudulent");
            throw new IllegalStateException("Something went wrong");
        }

        Payment newPayment = persistNewPayment(merchant, request, card);

        handlePaymentSubmission(merchant, card, newPayment);

        // Notify the merchant asynchronously if webhook is present
        if (merchant.getWebHookUrl() != null) {
            log.info("Sending webhook to merchant");
            notifyMerchant(merchant.getWebHookUrl(), newPayment.getPaymentReference(), newPayment.getStatus());
        }

        return newPayment;
    }

    private Payment persistNewPayment(Merchant merchant, PaymentRequest request, Card card) {
        return repository.saveAndFlush(Payment.builder()
                .amountInCents(request.getAmountInCents())
                .currency(request.getCurrency())
                .paymentReference(request.getReference())
                .description(request.getNarration())
                .statusDescription("Payment is being processed")
                .card(card)
                .status(PaymentStatus.PENDING)
                .merchant(merchant)
                .idempotencyKey(UUID.randomUUID().toString())
                .build());
    }


    @Transactional
    public void handlePaymentSubmission(Merchant merchant,
                                        Card card,
                                        Payment payment) {
        submitCkoPayment(merchant, card, payment).subscribe(
                response -> {
                    payment.setThirdPartyId(response.getId());
                    payment.setStatus(response.getStatus());
                    payment.setStatusDescription(response.getDescription());
                    payment.setSubmittedAt(response.getCreatedAt());

                    repository.save(payment);
                }
        );


    }

    private Mono<GetPaymentResponse> submitCkoPayment(Merchant merchant,
                                                      Card card,
                                                      Payment payment) {
        String callBackUrl = String.format("%s/api/%s/%s/callback", appUrl, merchant.getId(), payment.getPaymentReference());
        return ckoClient.submitPayment(CreatePaymentRequest.builder()
                .amountInCents(payment.getAmountInCents())
                .currency(payment.getCurrency())
                .beneficiaryAccount(merchant.getBankAccountNumber())
                .beneficiaryBank(merchant.getBankName())
                .beneficiaryName(merchant.getName())
                .narration(payment.getDescription())
                .reference(payment.getPaymentReference())
                .callbackUrl(callBackUrl)
                .cardToken(card.getToken())
                .build(), payment.getIdempotencyKey());
    }

    @Async
    public void notifyMerchant(String webHook, String reference, PaymentStatus paymentStatus) {
        notificationService.notifyMerchant(webHook, reference, paymentStatus);
    }

    private TokeniseCardResponse getTokenizedCard(CardDetailsRequest request) {
        return ckoClient.tokeniseCard(TokeniseCardRequest.builder()
                .cardNumber(request.getCardNumber())
                .cvv(request.getCvv())
                .expiryMonth(request.getExpiryMonth())
                .expiryYear(request.getExpiryYear())
                .nameOnCard(request.getNameOnCard())
                .build());
    }

    private GetPaymentResponse getCkoPayment(Long paymentId) {
        return ckoClient.getPayment(paymentId);
    }
}

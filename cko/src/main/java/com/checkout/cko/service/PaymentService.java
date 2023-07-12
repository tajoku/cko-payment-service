package com.checkout.cko.service;

import com.checkout.cko.api.request.CreatePaymentRequest;
import com.checkout.cko.enums.CardStatus;
import com.checkout.cko.enums.PaymentStatus;
import com.checkout.cko.exception.CardNotActiveException;
import com.checkout.cko.exception.InvalidCurrencyException;
import com.checkout.cko.exception.NotFoundException;
import com.checkout.cko.exception.UnknownCardProcessorException;
import com.checkout.cko.model.Card;
import com.checkout.cko.model.Payment;
import com.checkout.cko.repository.PaymentRepository;
import com.checkout.cko.service.processor.CardProcessor;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository repository;

    private final CardService cardService;

    private final CallbackService callbackService;


    public Payment submitPayment(CreatePaymentRequest request, String idempotencyKey) {

        // Ensures no payments with the same details are created within the last minute
        Optional<Payment> IdempotentPayment = findIdempotencyPayment(idempotencyKey);

        if (IdempotentPayment.isPresent()) {
            return IdempotentPayment.get();
        }

        // Initialised a payment
        Payment payment = Payment.builder()
                .amountInCents(request.getAmountInCents())
                .paymentStatus(PaymentStatus.PENDING)
                .description("Payment is being processed")
                .narration(request.getNarration())
                .idempotencyKey(idempotencyKey)
                .beneficiaryName(request.getBeneficiaryName())
                .beneficiaryAccount(request.getBeneficiaryAccount())
                .beneficiaryBank(request.getBeneficiaryBank())
                .currency(request.getCurrency())
                .reference(request.getReference())
                .callbackUrl(request.getCallbackUrl())
                .build();

        try {
            Card card = cardService.findCardByToken(request.getCardToken());

            if (!card.getCardStatus().equals(CardStatus.ACTIVE)) {
                throw new CardNotActiveException("The card used for this payment is not active");
            }
            if (!request.getCurrency().equals(card.getCurrency())) {
                throw new InvalidCurrencyException("The card used does not support the payment currency");
            }
            payment.setCard(card);

            payment = repository.saveAndFlush(payment);

            // Asynchronously process payment
            processPayment(payment);

            return payment;
        } catch (NotFoundException | CardNotActiveException | InvalidCurrencyException e) {

            // Marks payment as failed if any of these exceptions are caught
            payment.setDescription(e.getMessage());
            payment.setPaymentStatus(PaymentStatus.FAILED);
            repository.save(payment);
            throw e;
        }
    }


    public Payment findById(Long id) {
        return repository.findById(id).
                orElseThrow(() -> new NotFoundException("Payment with ID " + id + " does not exist."));
    }

    public List<Payment> getAllPaymentsByAccount(String beneficiaryAccount) {
        return repository.findAllByBeneficiaryAccount(beneficiaryAccount);
    }

    @Async
    @Transactional
    public void processPayment(Payment payment) {
        PaymentStatus status = PaymentStatus.SUCCESS;
        String description = "Payment Successful";
        try {
            //Gets appropriate card processor based on the card type
            CardProcessor cardProcessor = cardService.getCardProcessor(payment.getCard());
            if (!cardProcessor.processPayment(payment)) {
                status = PaymentStatus.FAILED;
                description = "Failed to process card";
            }
        } catch (UnknownCardProcessorException ex) {
            log.error(ex.getMessage(), ex);
            status = PaymentStatus.FAILED;
            description = ex.getMessage();
        }

        payment.setPaymentStatus(status);
        payment.setDescription(description);
        repository.save(payment);

        if (payment.getCallbackUrl() != null) {
            callbackService.sendCallback(payment.getCallbackUrl(), status, description);
        }

    }

    private Optional<Payment> findIdempotencyPayment(String idempotencyKey) {
        // Returns any payment with same idempotency key in the last minute
        long currentTimeMillis = System.currentTimeMillis();
        long oneMinutesInMillis = 60 * 1000;
        Date lastOneMinute = new Date(currentTimeMillis - oneMinutesInMillis);

        return repository.findFirstByIdempotencyKey(idempotencyKey, PaymentStatus.SUCCESS, lastOneMinute);
    }
}

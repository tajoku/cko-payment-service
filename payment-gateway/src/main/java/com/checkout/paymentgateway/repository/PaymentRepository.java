package com.checkout.paymentgateway.repository;

import com.checkout.paymentgateway.model.Merchant;
import com.checkout.paymentgateway.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findAllByMerchant(Merchant merchant);

    Optional<Payment> findByPaymentReferenceAndMerchant(String paymentReference, Merchant merchant);

    Optional<Payment> findByIdAndMerchant(Long paymentId, Merchant merchant);
}

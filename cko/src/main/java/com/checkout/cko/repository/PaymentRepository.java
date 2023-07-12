package com.checkout.cko.repository;

import com.checkout.cko.enums.PaymentStatus;
import com.checkout.cko.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findAllByBeneficiaryAccount(String beneficiaryAccount);

    @Query("SELECT p FROM Payment p WHERE p.idempotencyKey = :idempotencyKey and " +
            "p.paymentStatus = :paymentStatus and p.createdAt >= :timeLimit")
    Optional<Payment> findFirstByIdempotencyKey(String idempotencyKey,
                                                PaymentStatus paymentStatus,
                                                Date timeLimit);
}

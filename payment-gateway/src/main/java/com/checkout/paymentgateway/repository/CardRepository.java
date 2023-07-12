package com.checkout.paymentgateway.repository;

import com.checkout.paymentgateway.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    Optional<Card> findByToken(String token);

    boolean existsByToken(String token);

}

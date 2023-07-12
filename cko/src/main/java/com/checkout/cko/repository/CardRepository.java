package com.checkout.cko.repository;

import com.checkout.cko.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    Optional<Card> findByToken(String token);

    Optional<Card> findByCardNumberAndCvvAndNameOnCardAndExpiryMonthAndExpiryYear(String cardNumber,
                                                                                  String cvv,
                                                                                  String nameOnCard,
                                                                                  Integer expiryMonth,
                                                                                  Integer expiryYear);
}

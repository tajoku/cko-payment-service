package com.checkout.cko.model;

import com.checkout.cko.enums.CardStatus;
import com.checkout.cko.enums.CardType;
import com.checkout.cko.enums.Currency;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@Entity
@Table(name = "card_tbl")
@NoArgsConstructor
@AllArgsConstructor
public class Card {

    private static final int BIN_LENGTH = 6;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nameOnCard;

    @Column(nullable = false, unique = true)
    private String cardNumber;

    @Column(nullable = false)
    private String cvv;

    @Column(nullable = false)
    private Integer expiryMonth;

    @Column(nullable = false)
    private Integer expiryYear;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    private CardType cardType;

    @Enumerated(EnumType.STRING)
    private CardStatus cardStatus;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToMany(mappedBy = "card")
    private List<Payment> payments;

    public String getMaskedCardNumber() {
        if (cardNumber == null) {
            return null;
        }

        String pan = String.valueOf(cardNumber);
        return pan.substring(0, BIN_LENGTH) + "****" + pan.substring(pan.length() - 4);
    }

}

package com.checkout.paymentgateway.model;

import com.checkout.paymentgateway.enums.Currency;
import com.checkout.paymentgateway.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;


@Data
@Builder
@Entity
@Table(name = "payment_tbl",
        uniqueConstraints = @UniqueConstraint(columnNames = {"merchant_id", "paymentReference"}))
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(nullable = false)
    private String paymentReference;

    @Column
    private String description;

    @Column(nullable = false)
    private Long amountInCents;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column
    private String statusDescription;

    @Column(nullable = false)
    private String idempotencyKey;

    @Column
    private Long thirdPartyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    @Temporal(TemporalType.TIMESTAMP)
    private Date submittedAt;

    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @UpdateTimestamp
    private Date updatedAt;


    public boolean isStatusTerminal() {
        return status.equals(PaymentStatus.FAILED) || status.equals(PaymentStatus.SUCCESS);
    }

}

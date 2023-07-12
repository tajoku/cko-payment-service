package com.checkout.paymentgateway.model;

import com.checkout.paymentgateway.enums.MerchantType;
import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@Entity
@Table(name = "merchant_tbl")
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class Merchant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String bankAccountNumber;

    @Column(nullable = false)
    private String bankName;

    @Column
    private String webHookUrl;

    @Enumerated(EnumType.STRING)
    private MerchantType merchantType;

}
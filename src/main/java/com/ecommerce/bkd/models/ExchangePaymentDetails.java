package com.ecommerce.bkd.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "exchange_payment_details")
public class ExchangePaymentDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethodType paymentMethodType;

    @NotBlank
    private String bankAccountHolderName;

    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC code")
    private String bankIfscCode;

    @Pattern(regexp = "^[0-9]{9,18}$", message = "Invalid bank account number")
    private String bankAccountNumber;

    @Pattern(regexp = "^[a-zA-Z0-9]+@[a-zA-Z0-9]+$", message = "Invalid UPI ID")
    private String upiId;

    public enum PaymentMethodType {
        BANK_TRANSFER,
        UPI
    }
}

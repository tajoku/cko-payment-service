package com.checkout.paymentgateway.service;


import com.checkout.paymentgateway.exception.NotFoundException;
import com.checkout.paymentgateway.model.Merchant;
import com.checkout.paymentgateway.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantRepository merchantRepository;

    public Merchant getMerchant(Long id) {
        return merchantRepository.findById(id).
                orElseThrow(() -> new NotFoundException("Merchant with id " + id + " does not exist."));
    }
}

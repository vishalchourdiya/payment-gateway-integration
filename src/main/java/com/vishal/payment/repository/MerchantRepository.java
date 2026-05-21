package com.vishal.payment.repository;

import com.vishal.payment.domain.merchant.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MerchantRepository extends JpaRepository<Merchant, Long> {
    Optional<Merchant> findByMidAndActiveTrue(String mid);
}

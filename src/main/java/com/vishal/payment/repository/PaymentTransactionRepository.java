package com.vishal.payment.repository;

import com.vishal.payment.domain.PaymentStatus;
import com.vishal.payment.domain.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findByOrderNo(String orderNo);
    Optional<PaymentTransaction> findByTxnId(String txnId);
    boolean existsByOrderNo(String orderNo);
    List<PaymentTransaction> findTop50ByStatusInAndCreatedAtBeforeOrderByCreatedAtAsc(List<PaymentStatus> statuses, Instant before);
}

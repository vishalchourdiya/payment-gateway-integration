package com.vishal.payment.repository;

import com.vishal.payment.domain.callback.CallbackEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CallbackEventRepository extends JpaRepository<CallbackEvent, Long> {
    boolean existsBySignature(String signature);
    Optional<CallbackEvent> findBySignature(String signature);
}

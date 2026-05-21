package com.vishal.payment.repository;

import com.vishal.payment.domain.audit.TransactionAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionAuditRepository extends JpaRepository<TransactionAudit, Long> {}

package com.uniloop.backend.repository;

import com.uniloop.backend.entity.BorrowRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BorrowRequestRepository extends JpaRepository<BorrowRequest, Long> {

    List<BorrowRequest> findByRequesterEmail(String requesterEmail);

    List<BorrowRequest> findByResourceId(Long resourceId);
}
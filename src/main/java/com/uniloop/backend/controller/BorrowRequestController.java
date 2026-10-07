package com.uniloop.backend.controller;

import com.uniloop.backend.entity.BorrowRequest;
import com.uniloop.backend.repository.BorrowRequestRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/borrow-requests")
@CrossOrigin(origins = "http://localhost:5173")
public class BorrowRequestController {

    private final BorrowRequestRepository borrowRequestRepository;

    public BorrowRequestController(BorrowRequestRepository borrowRequestRepository) {
        this.borrowRequestRepository = borrowRequestRepository;
    }

    @PostMapping
    public BorrowRequest createRequest(@RequestBody BorrowRequest request) {

        request.setStatus("PENDING");

        return borrowRequestRepository.save(request);
    }

    @GetMapping
    public List<BorrowRequest> getAllRequests() {
        return borrowRequestRepository.findAll();
    }

    @GetMapping("/user/{email}")
    public List<BorrowRequest> getUserRequests(@PathVariable String email) {
        return borrowRequestRepository.findByRequesterEmail(email);
    }

    @GetMapping("/resource/{resourceId}")
    public List<BorrowRequest> getResourceRequests(
            @PathVariable Long resourceId) {

        return borrowRequestRepository.findByResourceId(resourceId);
    }
}
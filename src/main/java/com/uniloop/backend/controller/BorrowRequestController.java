
package com.uniloop.backend.controller;

import com.uniloop.backend.entity.BorrowRequest;
import com.uniloop.backend.entity.Resource;
import com.uniloop.backend.repository.BorrowRequestRepository;
import com.uniloop.backend.repository.ResourceRepository;
import com.uniloop.backend.service.EmailService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/borrow-requests")
public class BorrowRequestController {

    private static final Logger logger =
            LoggerFactory.getLogger(BorrowRequestController.class);

    private final BorrowRequestRepository borrowRequestRepository;
    private final ResourceRepository resourceRepository;
    private final EmailService emailService;

    public BorrowRequestController(
            BorrowRequestRepository borrowRequestRepository,
            ResourceRepository resourceRepository,
            EmailService emailService) {

        this.borrowRequestRepository = borrowRequestRepository;
        this.resourceRepository = resourceRepository;
        this.emailService = emailService;
    }

    @PostMapping
    public BorrowRequest createRequest(
            @RequestBody BorrowRequest request) {

        Resource resource = resourceRepository
                .findById(request.getResourceId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Resource not found"));

        if (!resource.isAvailable()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Resource is not available");
        }

        request.setId(null);
        request.setStatus("PENDING");

        return borrowRequestRepository.save(request);
    }

    @GetMapping
    public List<BorrowRequest> getAllRequests() {
        return borrowRequestRepository.findAll();
    }

    @GetMapping("/user/{email}")
    public List<BorrowRequest> getUserRequests(
            @PathVariable String email) {

        return borrowRequestRepository.findByRequesterEmail(email);
    }

    @GetMapping("/resource/{resourceId}")
    public List<BorrowRequest> getResourceRequests(
            @PathVariable Long resourceId) {

        return borrowRequestRepository.findByResourceId(resourceId);
    }

    @GetMapping("/received")
    public List<BorrowRequest> getReceivedRequests(
            @RequestParam String ownerEmail) {

        List<Resource> resources =
                resourceRepository.findByOwnerEmailIgnoreCase(ownerEmail);

        return resources.stream()
                .flatMap(resource ->
                        borrowRequestRepository
                                .findByResourceId(resource.getId())
                                .stream())
                .toList();
    }

    @PutMapping("/{requestId}/approve")
    public BorrowRequest approveRequest(
            @PathVariable Long requestId,
            @RequestParam String ownerEmail) {

        BorrowRequest request = getPendingRequest(requestId);
        Resource resource = getOwnedResource(request, ownerEmail);

        resource.setAvailable(false);
        resourceRepository.save(resource);

        request.setStatus("APPROVED");

        BorrowRequest savedRequest =
                borrowRequestRepository.save(request);

        sendStatusEmail(
                savedRequest.getRequesterEmail(),
                resource.getTitle(),
                "APPROVED");

        return savedRequest;
    }

    @PutMapping("/{requestId}/reject")
    public BorrowRequest rejectRequest(
            @PathVariable Long requestId,
            @RequestParam String ownerEmail) {

        BorrowRequest request = getPendingRequest(requestId);
        Resource resource = getOwnedResource(request, ownerEmail);

        request.setStatus("REJECTED");

        BorrowRequest savedRequest =
                borrowRequestRepository.save(request);

        sendStatusEmail(
                savedRequest.getRequesterEmail(),
                resource.getTitle(),
                "REJECTED");

        return savedRequest;
    }

    private void sendStatusEmail(
            String recipientEmail,
            String resourceTitle,
            String status) {

        try {
            emailService.sendBorrowRequestUpdate(
                    recipientEmail,
                    resourceTitle,
                    status);

            logger.info(
                    "UniLoop email submitted successfully. Status: {}",
                    status);

        } catch (Exception e) {
            logger.error(
                    "Email failed, but the borrow request status was saved. "
                            + "Status: {}. Reason: {}",
                    status,
                    e.getMessage());
        }
    }

    private BorrowRequest getPendingRequest(Long requestId) {

        BorrowRequest request = borrowRequestRepository
                .findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Borrow request not found"));

        if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This request has already been processed");
        }

        return request;
    }

    private Resource getOwnedResource(
            BorrowRequest request,
            String ownerEmail) {

        Resource resource = resourceRepository
                .findById(request.getResourceId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Resource not found"));

        if (ownerEmail == null
                || resource.getOwnerEmail() == null
                || !resource.getOwnerEmail()
                        .equalsIgnoreCase(ownerEmail)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the resource owner can process this request");
        }

        return resource;
    }
}

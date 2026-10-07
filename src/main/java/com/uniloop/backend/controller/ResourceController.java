package com.uniloop.backend.controller;

import com.uniloop.backend.entity.Resource;
import com.uniloop.backend.repository.ResourceRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
@CrossOrigin(origins = "http://localhost:5173")
public class ResourceController {

    private final ResourceRepository resourceRepository;

    public ResourceController(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    @PostMapping
    public Resource addResource(@RequestBody Resource resource) {
        return resourceRepository.save(resource);
    }

    @GetMapping
    public List<Resource> getAllResources() {
        return resourceRepository.findAll();
    }
}
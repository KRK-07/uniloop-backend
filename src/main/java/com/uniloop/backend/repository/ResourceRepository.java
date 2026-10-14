package com.uniloop.backend.repository;

import com.uniloop.backend.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResourceRepository extends JpaRepository<Resource, Long> {

    List<Resource> findByOwnerEmailIgnoreCase(String ownerEmail);
}
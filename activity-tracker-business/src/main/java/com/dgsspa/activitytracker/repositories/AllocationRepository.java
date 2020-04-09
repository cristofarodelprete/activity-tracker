package com.dgsspa.activitytracker.repositories;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dgsspa.activitytracker.models.Allocation;

@Repository
@Transactional
public interface AllocationRepository extends JpaRepository<Allocation, Long> {
	List<Allocation> findByEndIsNull();

	List<Allocation> findByEndIsNotNullOrderByStartDesc();
}

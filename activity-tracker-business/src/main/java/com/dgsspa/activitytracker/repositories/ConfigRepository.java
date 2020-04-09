package com.dgsspa.activitytracker.repositories;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dgsspa.activitytracker.models.Config;

@Repository
@Transactional
public interface ConfigRepository extends JpaRepository<Config, Long> {

	Optional<Config> findByKey(String key);
}

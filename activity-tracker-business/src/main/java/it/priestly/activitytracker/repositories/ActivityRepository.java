package it.priestly.activitytracker.repositories;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.priestly.activitytracker.models.Activity;

@Repository
@Transactional
public interface ActivityRepository extends JpaRepository<Activity, Long> {

	Long countByName(String name);
}

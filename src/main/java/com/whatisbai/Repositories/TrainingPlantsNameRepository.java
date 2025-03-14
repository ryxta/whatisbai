package com.whatisbai.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.whatisbai.Entities.TrainingPlantsName;

@Repository
public interface TrainingPlantsNameRepository extends JpaRepository<TrainingPlantsName, Integer> {
    
}

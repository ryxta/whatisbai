package com.whatisbai.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.whatisbai.Entities.TrainingData;

@Repository
public interface TrainingDataRepository extends JpaRepository<TrainingData, Integer> {

}

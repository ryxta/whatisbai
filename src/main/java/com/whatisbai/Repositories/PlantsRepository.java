package com.whatisbai.Repositories;

import com.whatisbai.Entities.Plants;
import com.whatisbai.Entities.PlantsName;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PlantsRepository extends JpaRepository<Plants, Integer> {

    @Query("SELECT p FROM Plants p WHERE p.plantsName.plantName LIKE %:name%")
    List<Plants> findByPlantNameContaining(@Param("name") String name);

    List<Plants> findByPlantsName(PlantsName plantsName);
}


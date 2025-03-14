package com.whatisbai.Repositories;

import com.whatisbai.Entities.PlantsName;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PlantsNameRepository extends JpaRepository<PlantsName, Integer> {
    @Query("SELECT p FROM PlantsName p WHERE p.plantNameInModel = :name")
    PlantsName findByPlantNameInModel(@Param("name") String name);

    @Query("SELECT p FROM PlantsName p WHERE p.plantNameInModel IS NOT NULL AND p.plantNameInModel != ''")
    List<PlantsName> findAllByPlantNameInModelNotNullOrEmpty();

    @Query("SELECT p FROM PlantsName p WHERE p.plantName =:name")
    PlantsName findByPlantName(@Param("name") String name);

    @Query("SELECT p FROM PlantsName p WHERE p.plantName LIKE %:name%")
    List<PlantsName> findByPlantNameContaining(@Param("name") String name);

    List<PlantsName> findFirst30ByOrderByPlantNameIdAsc();
}


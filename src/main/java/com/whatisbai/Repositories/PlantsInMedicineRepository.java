package com.whatisbai.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.whatisbai.Entities.Medicine;
import com.whatisbai.Entities.PlantsInMedicine;

@Repository
public interface PlantsInMedicineRepository extends JpaRepository<PlantsInMedicine, Integer>{
    void deleteByMedicine(Medicine medicine);
    List<PlantsInMedicine> findByMedicine(Medicine medicine);

    @Query("SELECT p FROM PlantsInMedicine p WHERE p.medicine.medId =:id")
    List<PlantsInMedicine> findByMedicineId(@Param("id") int id);

    @Query("SELECT p FROM PlantsInMedicine p WHERE p.plant.plantsName.plantNameId =:id")
    List<PlantsInMedicine> findByPlantsNameId(@Param("id") int id);
}

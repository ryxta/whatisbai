package com.whatisbai.Repositories;

import com.whatisbai.Entities.*;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Integer> {
    @Query("SELECT m FROM Medicine m WHERE m.medName LIKE %:name%")
    List<Medicine> findByMedicineNameContaining(String name);

    List<Medicine> findFirst30ByOrderByMedIdAsc();

    @Query("SELECT m FROM Medicine m WHERE m.medName =:name")
    Medicine findByMedicineName(@Param("name") String name);
}

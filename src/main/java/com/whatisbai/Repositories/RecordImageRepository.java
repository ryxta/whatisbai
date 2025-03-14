package com.whatisbai.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.whatisbai.Entities.RecordImage;
import com.whatisbai.Entities.Records;

@Repository
public interface RecordImageRepository extends JpaRepository<RecordImage, Integer> {
    @Query("SELECT r FROM RecordImage r WHERE r.records.recordVerify = false")
    List<RecordImage> getUnVerifyRecordImage();

    @Query("SELECT r FROM RecordImage r WHERE r.records.plantsName.plantNameId =:id")
    List<RecordImage> getByPlantsNameId(@Param("id") int id);


    List<RecordImage> getByRecords(Records records);
}

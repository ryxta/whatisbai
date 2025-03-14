package com.whatisbai.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.whatisbai.Entities.Records;
import java.util.List;


@Repository
public interface RecordsRepository extends JpaRepository<Records, Integer> {
    @Query("SELECT r FROM Records r WHERE r.recordVerify = false")
    List<Records> getUnVerifyRecords();

    @Query("SELECT r FROM Records r WHERE r.recordVerify = true")
    List<Records> getVerifyRecords();
}

package com.whatisbai.Entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "records")
public class Records {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private int recordId;

    @Column(name = "record_verify", columnDefinition = "TINYINT")
    private boolean recordVerify;

    @Column(name = "record_timestamp")
    private LocalDateTime recordTimestamp;

    @ManyToOne
    @JoinColumn(name = "record_verify_by", referencedColumnName = "user_id")
    private Users userId;

    @ManyToOne
    @JoinColumn(name = "plant_name_id", referencedColumnName = "plant_name_id")
    private PlantsName plantsName;

    @OneToMany(mappedBy = "records", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecordImage> recordImage = new ArrayList<>();

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public boolean isRecordVerify() {
        return recordVerify;
    }

    public void setRecordVerify(boolean recordVerify) {
        this.recordVerify = recordVerify;
    }

    public LocalDateTime getRecordTimestamp() {
        return recordTimestamp;
    }

    public void setRecordTimestamp(LocalDateTime recordTimestamp) {
        this.recordTimestamp = recordTimestamp;
    }

    public Users getUserId() {
        return userId;
    }

    public void setUserId(Users userId) {
        this.userId = userId;
    }

    public PlantsName getPlantsName() {
        return plantsName;
    }

    public void setPlantsName(PlantsName plantsName) {
        this.plantsName = plantsName;
    }

    public List<RecordImage> getRecordImage() {
        return recordImage;
    }

    public void setRecordImage(List<RecordImage> recordImage) {
        this.recordImage = recordImage;
    }
    
}

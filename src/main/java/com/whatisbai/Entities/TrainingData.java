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
@Table(name = "trainingdata")
public class TrainingData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "training_data_id")
    private int trainingDataId;

    @Column(name = "export_timestamp")
    private LocalDateTime exportTimestamp;

    @ManyToOne
    @JoinColumn(name = "export_by", referencedColumnName = "user_id")
    private Users users;

    @OneToMany(mappedBy = "trainingData", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TrainingPlantsName> trainingPlantsName = new ArrayList<>();

    public int getTrainingDataId() {
        return trainingDataId;
    }

    public void setTrainingDataId(int trainingDataId) {
        this.trainingDataId = trainingDataId;
    }

    public LocalDateTime getExportTimestamp() {
        return exportTimestamp;
    }

    public void setExportTimestamp(LocalDateTime exportTimestamp) {
        this.exportTimestamp = exportTimestamp;
    }

    public Users getUsers() {
        return users;
    }

    public void setUsers(Users users) {
        this.users = users;
    }

    public List<TrainingPlantsName> getTrainingPlantsName() {
        return trainingPlantsName;
    }

    public void setTrainingPlantsName(List<TrainingPlantsName> trainingPlantsName) {
        this.trainingPlantsName = trainingPlantsName;
    }

    
    
}

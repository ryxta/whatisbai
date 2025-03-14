package com.whatisbai.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "trainingplantsname")
public class TrainingPlantsName {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "training_plants_name")
    private int trainingPlantsNameId;

    @ManyToOne
    @JoinColumn(name = "training_data_id", referencedColumnName = "training_data_id")
    private TrainingData trainingData;

    @ManyToOne
    @JoinColumn(name = "plant_name_id", referencedColumnName = "plant_name_id")
    private PlantsName plantsName;

    public int getTrainingPlantsNameId() {
        return trainingPlantsNameId;
    }

    public void setTrainingPlantsNameId(int trainingPlantsNameId) {
        this.trainingPlantsNameId = trainingPlantsNameId;
    }

    public TrainingData getTrainingData() {
        return trainingData;
    }

    public void setTrainingData(TrainingData trainingData) {
        this.trainingData = trainingData;
    }

    public PlantsName getPlantsName() {
        return plantsName;
    }

    public void setPlantsName(PlantsName plantsName) {
        this.plantsName = plantsName;
    }

}

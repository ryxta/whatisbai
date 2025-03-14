package com.whatisbai.Classes;

import com.whatisbai.Entities.PlantsName;

public class ModelOutput {
    private String className;
    private double confidenceScore;
    private PlantsName plantsName;

    public ModelOutput(){}

    public ModelOutput(String className, double confidenceScore) {
        this.className = className;
        this.confidenceScore = confidenceScore;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public PlantsName getPlantsName() {
        return plantsName;
    }

    public void setPlantsName(PlantsName plantsName) {
        this.plantsName = plantsName;
    }
    

}

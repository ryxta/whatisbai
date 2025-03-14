package com.whatisbai.Entities;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "medicine")
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "med_id")
    private int medId;

    @Column(name = "med_name", length = 1000, nullable = false)
    private String medName;

    @Column(name = "med_ref", length = 1000)
    private String medRef;

    @Column(name = "med_benifit", length = 1000)
    private String medBenifit;

    @Column(name = "med_use_form", length = 1000)
    private String medUseForm;

    @Column(name = "med_desc", length = 1000)
    private String medDesc;

    @Column(name = "med_prohibition", length = 1000)
    private String medProhibition;

    @Column(name = "med_caution", length = 1000)
    private String medCaution;

    @Column(name = "med_dose", length = 1000)
    private String medDose;

    @Column(name = "med_side_effect", length = 1000)
    private String medSideEffect;

    @OneToMany(mappedBy = "medicine", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlantsInMedicine> plantsInMedicine = new ArrayList<>();
    
    public int getMedId() {
        return medId;
    }

    public void setMedId(int medId) {
        this.medId = medId;
    }

    public String getMedName() {
        return medName;
    }

    public void setMedName(String medName) {
        this.medName = medName;
    }

    public String getMedRef() {
        return medRef;
    }

    public void setMedRef(String medRef) {
        this.medRef = medRef;
    }

    public String getMedBenifit() {
        return medBenifit;
    }

    public void setMedBenifit(String medBenifit) {
        this.medBenifit = medBenifit;
    }

    public String getMedUseForm() {
        return medUseForm;
    }

    public void setMedUseForm(String medUseForm) {
        this.medUseForm = medUseForm;
    }

    public String getMedDesc() {
        return medDesc;
    }

    public void setMedDesc(String medDesc) {
        this.medDesc = medDesc;
    }

    public String getMedProhibition() {
        return medProhibition;
    }

    public void setMedProhibition(String medProhibition) {
        this.medProhibition = medProhibition;
    }

    public String getMedCaution() {
        return medCaution;
    }

    public void setMedCaution(String medCaution) {
        this.medCaution = medCaution;
    }

    public String getMedDose() {
        return medDose;
    }

    public void setMedDose(String medDose) {
        this.medDose = medDose;
    }

    public String getMedSideEffect() {
        return medSideEffect;
    }

    public void setMedSideEffect(String medSideEffect) {
        this.medSideEffect = medSideEffect;
    }

    public List<PlantsInMedicine> getPlantsInMedicine() {
        return plantsInMedicine;
    }

    public void setPlantsInMedicine(List<PlantsInMedicine> plantsInMedicine) {
        this.plantsInMedicine = plantsInMedicine;
    }
}


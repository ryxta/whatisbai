package com.whatisbai.Entities;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "plantsname")
public class PlantsName {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plant_name_id")
    private int plantNameId;

    @Column(name = "plant_name", length = 45, unique = true, nullable = false)
    private String plantName;

    @Column(name = "plant_name_in_model", length = 45)
    private String plantNameInModel;

    @Column(name = "plant_show_image_path", length = 500)
    private String plantShowImagePath;
    
    @Column(name = "plant_name_verify", columnDefinition = "TINYINT")
    private boolean plantNameVerify;

    @Column(name = "plant_name_desc", columnDefinition = "TEXT")
    private String plantNameDesc;

    @OneToMany(mappedBy = "plantsName", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Plants> plants = new ArrayList<>();

    @OneToMany(mappedBy = "plantsName", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Records> records = new ArrayList<>();

    public int getPlantNameId() {
        return plantNameId;
    }

    public void setPlantNameId(int plantNameId) {
        this.plantNameId = plantNameId;
    }

    public String getPlantName() {
        return plantName;
    }

    public void setPlantName(String plantsName) {
        this.plantName = plantsName;
    }

    public String getPlantNameInModel() {
        return plantNameInModel;
    }

    public void setPlantNameInModel(String plantNameInModel) {
        this.plantNameInModel = plantNameInModel;
    }

    public String getPlantShowImagePath() {
        return plantShowImagePath;
    }

    public void setPlantShowImagePath(String plantShowImagePath) {
        this.plantShowImagePath = plantShowImagePath;
    }

    public boolean isPlantNameVerify() {
        return plantNameVerify;
    }

    public void setPlantNameVerify(boolean plantNameVerify) {
        this.plantNameVerify = plantNameVerify;
    }

    public List<Plants> getPlants() {
        return plants;
    }

    public void setPlants(List<Plants> plants) {
        this.plants = plants;
    }

    public List<Records> getRecords() {
        return records;
    }

    public void setRecords(List<Records> records) {
        this.records = records;
    }

    public String getPlantNameDesc() {
        return plantNameDesc;
    }

    public void setPlantNameDesc(String plantNameDesc) {
        this.plantNameDesc = plantNameDesc;
    }

    
}

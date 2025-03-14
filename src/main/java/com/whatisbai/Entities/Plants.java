package com.whatisbai.Entities;


import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "plants")
public class Plants {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plant_id")
    private int plantId;

    @ManyToOne
    @JoinColumn(name = "plant_name_id", referencedColumnName = "plant_name_id")
    private PlantsName plantsName;

    @Column(name = "plant_unit", length = 45)
    private String plantUnit;

    @Column(name = "plant_latin_name", length = 100)
    private String plantLatinName;

    @Column(name = "plant_sci_name", length = 200)
    private String plantSciName;

    /*
    @OneToMany(mappedBy = "plant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlantsInMedicine> PlantsInMedicine = new ArrayList<>();
    */
    // Constructors
    public Plants() {}

    // Getters and Setters
    public int getPlantId() {
        return plantId;
    }

    public void setPlantId(int plantId) {
        this.plantId = plantId;
    }

    public String getPlantUnit() {
        return plantUnit;
    }

    public void setPlantUnit(String plantUnit) {
        this.plantUnit = plantUnit;
    }

    public String getPlantLatinName() {
        return plantLatinName;
    }

    public void setPlantLatinName(String plantLatinName) {
        this.plantLatinName = plantLatinName;
    }

    public String getPlantSciName() {
        return plantSciName;
    }

    public void setPlantSciName(String plantSciName) {
        this.plantSciName = plantSciName;
    }

    public PlantsName getPlantsName() {
        return plantsName;
    }

    public void setPlantsName(PlantsName plantsName) {
        this.plantsName = plantsName;
    }

    // toString() method (optional)
    @Override
    public String toString() {
        return "Plant{" +
                "plantId=" + plantId +
                ", plantUnit='" + plantUnit + '\'' +
                ", plantLatinName='" + plantLatinName + '\'' +
                ", plantSciName='" + plantSciName + '\'' +
                '}';
    }

    /*
    public List<PlantsInMedicine> getPlantsInMedicine() {
        return PlantsInMedicine;
    }

    public void setPlantsInMedicine(List<PlantsInMedicine> plantsInMedicine) {
        PlantsInMedicine = plantsInMedicine;
    }
        */
    
}


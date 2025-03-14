package com.whatisbai.Entities;

import jakarta.persistence.*;

@Entity
@Table(name = "plantsinmedicine")
public class PlantsInMedicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plant_med_id")
    private int plantMedId;

    @ManyToOne
    @JoinColumn(name = "plant_id", nullable = false)
    private Plants plant;

    @ManyToOne
    @JoinColumn(name = "med_id", nullable = false)
    private Medicine medicine;

    @Column(name="quantity")
    private double quantity;

    @Column(name = "unit")
    private String unit;

    public int getPlantMedId() {
        return plantMedId;
    }

    public void setPlantMedId(int plantMedId) {
        this.plantMedId = plantMedId;
    }

    public Plants getPlant() {
        return plant;
    }

    public void setPlant(Plants plant) {
        this.plant = plant;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public void setMedicine(Medicine medicine) {
        this.medicine = medicine;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    
}

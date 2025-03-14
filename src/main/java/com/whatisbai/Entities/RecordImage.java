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
@Table(name = "recordimage")
public class RecordImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rec_image_id")
    private int recImageId;

    @Column(name = "rec_image_path", length = 200)
    private String recImagePath;

    @ManyToOne
    @JoinColumn(name = "record_id", referencedColumnName = "record_id")
    private Records records;

    public int getRecImageId() {
        return recImageId;
    }

    public void setRecImageId(int recImageId) {
        this.recImageId = recImageId;
    }

    public String getRecImagePath() {
        return recImagePath;
    }

    public void setRecImagePath(String recImagePath) {
        this.recImagePath = recImagePath;
    }

    public Records getRecords() {
        return records;
    }

    public void setRecords(Records records) {
        this.records = records;
    }

    
}

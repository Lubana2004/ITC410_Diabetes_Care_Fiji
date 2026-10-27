package com.example.diabetes_care_fiji;

public class MedicineItem {

    private long id;
    private String name;
    private String dosage;
    private String frequency;
    private String time;

    public MedicineItem(long id, String name, String dosage,
                        String frequency, String time) {
        this.id = id;
        this.name = name;
        this.dosage = dosage;
        this.frequency = frequency;
        this.time = time;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDosage() {
        return dosage;
    }

    public String getFrequency() {
        return frequency;
    }

    public String getTime() {
        return time;
    }
}
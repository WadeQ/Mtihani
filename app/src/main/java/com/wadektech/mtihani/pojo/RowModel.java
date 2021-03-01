package com.wadektech.mtihani.pojo;

public class RowModel {
    private String year;
    private int photo;

    public RowModel(String year, int photo) {
        this.year = year;
        this.photo = photo;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public int getPhoto() {
        return photo;
    }

    public void setPhoto(int photo) {
        this.photo = photo;
    }
}

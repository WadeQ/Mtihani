package com.wadektech.mtihanirevise.pojo;

public class PdfModel {
    String title ;
    String year ;

    public PdfModel(String title, String year) {
        this.title = title;
        this.year = year;
    }

    public PdfModel() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }
}

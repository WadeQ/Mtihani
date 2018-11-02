package com.wadektech.mtihanirevise.pojo;

public class Model {
    String title,pdf;

    public Model(String title, String pdf) {
        this.title = title;
        this.pdf = pdf;
    }

    public Model() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public void setPdf(String pdf) {
        this.pdf = pdf;
    }

    public String getPdf() {
        return pdf;
    }
}

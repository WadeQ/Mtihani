package com.wadektech.mtihanirevise.POJO;

public class PdfListItems {
    private String subject;
    private String year;

    public PdfListItems(String subject, String year) {
        this.subject = subject;
        this.year = year;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }
}

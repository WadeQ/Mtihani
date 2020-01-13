package com.wadektech.mtihanirevise.pojo;

public class SinglePDF {
    private String fileName;
    private String pdfUrl;
    private String category;

    public SinglePDF(String fileName, String pdfUrl, String category) {
        this.fileName = fileName;
        this.pdfUrl = pdfUrl;
        this.category = category;
    }

    public SinglePDF() {
        //This empty constructor is required by Firestore
        //DO NOT DELETE
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}

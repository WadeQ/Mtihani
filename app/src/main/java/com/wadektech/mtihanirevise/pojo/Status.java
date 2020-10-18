package com.wadektech.mtihanirevise.pojo;

public class Status {
    private String status ;
    private String time ;
    private String date ;

    public Status() {
    }

    public Status(String status, String time, String date) {
        this.status = status;
        this.time = time;
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public String getTime() {
        return time;
    }

    public String getDate() {
        return date;
    }
}

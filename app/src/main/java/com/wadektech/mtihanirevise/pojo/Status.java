package com.wadektech.mtihanirevise.pojo;

public class Status {
    private String state ;
    private String time ;
    private String date ;

    public Status() {
    }

    public Status(String state, String time, String date) {
        this.state = state;
        this.time = time;
        this.date = date;
    }

    public String getState() {
        return state;
    }

    public String getTime() {
        return time;
    }

    public String getDate() {
        return date;
    }
}

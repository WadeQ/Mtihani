package com.wadektech.mtihanirevise.room;

public class Status {
    private String userId;
    private String date ;
    private String state ;
    private String time;

    public Status() {
    }

    public Status(String userId, String date, String state, String time) {
        this.userId = userId;
        this.date = date;
        this.state = state;
        this.time = time;
    }

    public String getUserId() {
        return userId;
    }

    public String getDate() {
        return date;
    }

    public String getState() {
        return state;
    }

    public String getTime() {
        return time;
    }
}

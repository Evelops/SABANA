package com.example.sabana;

public class MessageItem {

    String name;
    String message;
    String time;
    String lat;
    String lon;

    public MessageItem(String name, String message, String time, String lat, String lon){
        this.name = name;
        this.message = message;
        this.time = time;
        this.lat = lat;
        this.lon = lon;
    }

    // Firebase DB에 객체로 값을 읽어올때, 파라미터가 비어있는 생성자 필요
    public MessageItem(){

    }

    public String getName(){
        return name;
    }
    public String getMessage(){
        return message;
    }
    public String getTime(){
        return time;
    }
    public String getLat() { return lat; }
    public String getLon() { return lon; }


    public void setName(String name){
        this.name = name;
    }
    public void setMessage(String message){
        this.message = message;
    }
    public void setTime(String time){
        this.time = time;
    }
    public void setLoc(String lat, String lon){ this.lat = lat; this.lon = lon; }

}

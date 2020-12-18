package com.example.sabana;

public class ChatInfo {

    String chatName;
    String admin;
    String distance; // 설정 거리
    String time;
    String lat;
    String lon;

    // chatName, admin, distance, time, lat, lon
    public ChatInfo  (String chatName, String admin, String distance,  String time, String lat, String lon){
        this.chatName = chatName;
        this.admin = admin;
        this.distance = distance;
        this.time = time;
        this.lat = lat;
        this.lon = lon;
    }

    // Firebase DB에 객체로 값을 읽어올때, 파라미터가 비어있는 생성자 필요
    public ChatInfo(){ }

    public String getChatName() { return chatName; }
    public String getDistance(){
        return distance;
    }
    public String getAdmin() {return admin; }
    public String getTime(){
        return time;
    }
    public String getLat() { return lat; }
    public String getLon() { return lon; }

    public void setChatName(String chatName) { this.chatName = chatName; }
    public void setDistance(String distance){
        this.distance = distance;
    }
    public void setAdmin(String admin){this.admin = admin;}
    public void setTime(String time){
        this.time = time;
    }
    public void setLoc(String lat, String lon){ this.lat = lat; this.lon = lon; }

}

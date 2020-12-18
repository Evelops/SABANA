package com.example.sabana;

public class MemberInfo {
    // for Quick Search

    String nickName;
    String limitTime;
    String lat;
    String lon;

    public MemberInfo(String nickName, String limitTime, String lat, String lon){
        this.nickName = nickName;
        this.limitTime = limitTime;
        this.lat = lat;
        this.lon = lon;
    }

    public MemberInfo(){

    }

    public String getNickName() { return nickName; }
    public String getLimitTime() { return limitTime; }
    public String getLat() { return lat; }
    public String getLon() { return lon; }


}

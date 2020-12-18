package com.example.sabana.fcmsendservice;

import com.google.gson.annotations.SerializedName;

public class FcmSend {

    @SerializedName("to")
    private String to;

    @SerializedName("data")
    private SendData sendData;

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public SendData getSendData() {
        return sendData;
    }

    public void setSendData(SendData sendData) {
        this.sendData = sendData;
    }

    public FcmSend(String to, SendData sendData) {
        this.to = to;
        this.sendData = sendData;
    }

    public static class SendData {
        private String title;

        private String body;

        public SendData(String title, String body) {
            this.title = title;
            this.body = body;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }
    }
}

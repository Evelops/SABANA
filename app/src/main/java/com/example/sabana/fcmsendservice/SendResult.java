package com.example.sabana.fcmsendservice;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SendResult {

    public static class Result {
        @Expose
        @SerializedName("message_id")
        private String messageId;

        public String getMessageId() {
            return messageId;
        }

        public void setMessageId(String messageId) {
            this.messageId = messageId;
        }
    }


    @Expose
    @SerializedName("multicast_id")
    private Long multicastId;

    @Expose
    private Long success;

    @Expose
    private Long failure;

    @Expose
    @SerializedName("canonical_ids")
    private Long canonical_ids;

    @Expose
    private List<Result> results;

    public Long getMulticastId() {
        return multicastId;
    }

    public void setMulticastId(Long multicastId) {
        this.multicastId = multicastId;
    }

    public Long getSuccess() {
        return success;
    }

    public void setSuccess(Long success) {
        this.success = success;
    }

    public Long getFailure() {
        return failure;
    }

    public void setFailure(Long failure) {
        this.failure = failure;
    }

    public Long getCanonical_ids() {
        return canonical_ids;
    }

    public void setCanonical_ids(Long canonical_ids) {
        this.canonical_ids = canonical_ids;
    }

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }
}

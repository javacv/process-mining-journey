package com.abc.process.mining.journey.model;


public class CorrelationKeyMap {
    private String ck;
    private String journeyId;

    public CorrelationKeyMap() {
    }

    public CorrelationKeyMap(String ck, String journeyId) {
        this.ck = ck;
        this.journeyId = journeyId;
    }

    public String getCk() {
        return ck;
    }

    public void setCk(String ck) {
        this.ck = ck;
    }

    public String getJourneyId() {
        return journeyId;
    }

    public void setJourneyId(String journeyId) {
        this.journeyId = journeyId;
    }
}

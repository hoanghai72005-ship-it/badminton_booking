package com.example.badmintonbooking.data;

public class MatchModel {
    private String id;
    private String hostName;
    private String courtName;
    private String matchTime;
    private String level;
    private int currentSlots;
    private int maxSlots;
    private String note;
    private boolean isJoined;

    public MatchModel(String id, String hostName, String courtName, String matchTime, String level, int currentSlots, int maxSlots, String note, boolean isJoined) {
        this.id = id;
        this.hostName = hostName;
        this.courtName = courtName;
        this.matchTime = matchTime;
        this.level = level;
        this.currentSlots = currentSlots;
        this.maxSlots = maxSlots;
        this.note = note;
        this.isJoined = isJoined;
    }

    public String getId() { return id; }
    public String getHostName() { return hostName; }
    public String getCourtName() { return courtName; }
    public String getMatchTime() { return matchTime; }
    public String getLevel() { return level; }
    public int getCurrentSlots() { return currentSlots; }
    public void setCurrentSlots(int currentSlots) { this.currentSlots = currentSlots; }
    public int getMaxSlots() { return maxSlots; }
    public String getNote() { return note; }
    public boolean isJoined() { return isJoined; }
    public void setJoined(boolean joined) { isJoined = joined; }
}
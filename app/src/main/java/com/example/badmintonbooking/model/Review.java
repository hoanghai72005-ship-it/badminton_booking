package com.example.badmintonbooking.model;

public class Review {
    private String userId;
    private String userEmail;
    private float rating;
    private String comment;
    private long timestamp;

    public Review() {
        // Constructor rỗng bắt buộc phải có cho Firebase
    }

    public Review(String userId, String userEmail, float rating, String comment, long timestamp) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.rating = rating;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    public String getUserId() { return userId; }
    public String getUserEmail() { return userEmail; }
    public float getRating() { return rating; }
    public String getComment() { return comment; }
    public long getTimestamp() { return timestamp; }
}
package com.example.badmintonbooking.model;

public class Court {
    private String courtId;
    private String courtName;
    private String address;
    private int totalCourts;
    private double price;
    private double rating;
    private int reviewCount;
    private String imageUrl;
    private String ownerEmail;

    public Court() {
    }

    public Court(String courtId, String courtName, String address, int totalCourts, double price, double rating, int reviewCount, String imageUrl, String ownerEmail) {
        this.courtId = courtId;
        this.courtName = courtName;
        this.address = address;
        this.totalCourts = totalCourts;
        this.price = price;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.imageUrl = imageUrl;
        this.ownerEmail = ownerEmail;
    }

    public String getCourtId() { return courtId; }
    public void setCourtId(String courtId) { this.courtId = courtId; }

    public String getCourtName() { return courtName; }
    public void setCourtName(String courtName) { this.courtName = courtName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public int getTotalCourts() { return totalCourts; }
    public void setTotalCourts(int totalCourts) { this.totalCourts = totalCourts; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }
}
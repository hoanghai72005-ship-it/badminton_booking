package com.example.badmintonbooking.model;

public class Court {
    private String courtId;
    private String courtName;
    private double price;
    private double rating;
    private int reviewCount;
    private String imageUrl;

    // Firebase BẮT BUỘC phải có một constructor rỗng
    public Court() {
    }

    public Court(String courtId, String courtName, double price, double rating, int reviewCount, String imageUrl) {
        this.courtId = courtId;
        this.courtName = courtName;
        this.price = price;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.imageUrl = imageUrl;
    }

    // Các hàm Getter và Setter
    public String getCourtId() { return courtId; }
    public void setCourtId(String courtId) { this.courtId = courtId; }

    public String getCourtName() { return courtName; }
    public void setCourtName(String courtName) { this.courtName = courtName; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
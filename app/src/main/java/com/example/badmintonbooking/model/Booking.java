package com.example.badmintonbooking.model;

import java.util.List;
import com.example.badmintonbooking.data.TimeSlot;

public class Booking {
    private String bookingId;
    private String courtId;
    private String userId;
    private String userEmail;
    private double totalPrice;
    private List<TimeSlot> bookedSlots;
    private long timestamp;
    private String status;        // "PENDING", "CONFIRMED", "CANCELLED", "COMPLETED"
    private boolean reviewed;

    public Booking() {
    }

    public Booking(String bookingId, String courtId, String userEmail, double totalPrice, List<TimeSlot> bookedSlots, long timestamp) {
        this.bookingId = bookingId;
        this.courtId = courtId;
        this.userEmail = userEmail;
        this.totalPrice = totalPrice;
        this.bookedSlots = bookedSlots;
        this.timestamp = timestamp;
        this.status = "CONFIRMED";
        this.reviewed = false;
    }


    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getCourtId() {
        return courtId;
    }

    public void setCourtId(String courtId) {
        this.courtId = courtId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public List<TimeSlot> getBookedSlots() {
        return bookedSlots;
    }

    public void setBookedSlots(List<TimeSlot> bookedSlots) {
        this.bookedSlots = bookedSlots;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isReviewed() {
        return reviewed;
    }

    public void setReviewed(boolean reviewed) {
        this.reviewed = reviewed;
    }
}
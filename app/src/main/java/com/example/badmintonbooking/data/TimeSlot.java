package com.example.badmintonbooking.data;

public class TimeSlot {
    private String _id;
    private String courtId;
    private String date;
    private String startTime;
    private String endTime;
    private double price;
    private String status;

    // 1. BẮT BUỘC: Constructor rỗng cho Firebase (Giải quyết triệt để lỗi Toast)
    public TimeSlot(String slotId, String cId, String date, String part, String[] parts, double price, String available) {
    }

    // 2. Constructor đầy đủ tham số
    public TimeSlot(String _id, String courtId, String date, String startTime, String endTime, double price, String status) {
        this._id = _id;
        this.courtId = courtId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.price = price;
        this.status = status;
    }

    // Getter & Setter
    public String get_id() { return _id; }
    public void set_id(String _id) { this._id = _id; }

    public String getSlotId() { return _id; }
    public void setSlotId(String slotId) { this._id = slotId; }

    public String getCourtId() { return courtId; }
    public void setCourtId(String courtId) { this.courtId = courtId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getStatus() { return status != null ? status : "AVAILABLE"; }
    public void setStatus(String status) { this.status = status; }
}
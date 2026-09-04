package com.example.badmintonbooking.ui;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.badmintonbooking.R;
import com.example.badmintonbooking.adapter.TimeSlotAdapter;
import com.example.badmintonbooking.data.Booking;
import com.example.badmintonbooking.data.TimeSlot;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CourtDetailActivity extends AppCompatActivity {

    private RecyclerView rvTimeSlots;
    private TimeSlotAdapter timeSlotAdapter;
    private List<TimeSlot> timeSlotList;
    private TextView tvTotalPrice, tvDetailCourtName, tvSelectedSubCourt, tvSelectedDate;
    private LinearLayout layoutSubCourts, layoutDates;
    private String courtId, courtName;
    private DatabaseReference mDatabase;
    private String selectedDate;
    private String selectedSubCourt = "Sân 1";

    private DatabaseReference currentSlotsRef;
    private ValueEventListener currentSlotsListener;

    // Lớp chứa dữ liệu của 7 ngày trong tuần
    public static class DayItem {
        String dbDate;      // Định dạng lưu Firebase: "2026-09-04"
        String displayDay;  // "Hôm nay", "Thứ Bảy", "Chủ Nhật"...
        String displayDate; // "04/09", "05/09"...

        public DayItem(String dbDate, String displayDay, String displayDate) {
            this.dbDate = dbDate;
            this.displayDay = displayDay;
            this.displayDate = displayDate;
        }
    }

    private List<DayItem> weekDays = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_court_detail);

        courtName = getIntent().getStringExtra("COURT_NAME");
        courtId = getIntent().getStringExtra("COURT_ID");
        if (courtId == null || courtId.isEmpty()) courtId = "court_02";
        if (courtName == null || courtName.isEmpty()) courtName = "Sân Cầu Lông ĐH Công Nghiệp";

        tvDetailCourtName = findViewById(R.id.tvDetailCourtName);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        rvTimeSlots = findViewById(R.id.rvTimeSlots);
        tvSelectedSubCourt = findViewById(R.id.tvSelectedSubCourt);
        layoutSubCourts = findViewById(R.id.layoutSubCourts);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        layoutDates = findViewById(R.id.layoutDates);
        MaterialButton btnBookCourt = findViewById(R.id.btnBookCourt);
        ImageView ivBack = findViewById(R.id.ivBack);

        if (tvDetailCourtName != null) tvDetailCourtName.setText(courtName);
        if (ivBack != null) ivBack.setOnClickListener(v -> finish());

        // 1. Khởi tạo 18 sân con
        if (layoutSubCourts != null) setupSubCourts();

        // 2. Khởi tạo 7 ngày trong tuần theo thời gian thực
        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        setupWeekDays();

        // 3. Khởi tạo danh sách khung giờ cuộn ngang
        rvTimeSlots.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        timeSlotList = new ArrayList<>();
        timeSlotAdapter = new TimeSlotAdapter(this, timeSlotList, this::calculateTotalPrice);
        rvTimeSlots.setAdapter(timeSlotAdapter);

        // 4. Kết nối Firebase
        mDatabase = FirebaseDatabase.getInstance("https://badminton-booking-9e3a2-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
        loadSlotsForCourt(courtId, selectedDate);

        // 5. Nút Đặt sân
        if (btnBookCourt != null) {
            btnBookCourt.setOnClickListener(v -> processBookingWithTransaction());
        }
    }

    // TẠO 7 NGÀY THEO THỜI GIAN THỰC (BẮT ĐẦU TỪ HÔM NAY)
    private void setupWeekDays() {
        weekDays.clear();
        Calendar cal = Calendar.getInstance();

        for (int i = 0; i < 7; i++) {
            Date d = cal.getTime();
            String dbDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(d);
            String displayDate = new SimpleDateFormat("dd/MM", Locale.getDefault()).format(d);

            String displayDay;
            if (i == 0) {
                displayDay = "Hôm nay";
            } else {
                int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                if (dayOfWeek == Calendar.SUNDAY) displayDay = "Chủ Nhật";
                else displayDay = "Thứ " + dayOfWeek;
            }

            weekDays.add(new DayItem(dbDate, displayDay, displayDate));
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        renderDateButtons();
    }

    private void renderDateButtons() {
        layoutDates.removeAllViews();
        for (DayItem day : weekDays) {
            MaterialButton btn = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            btn.setText(day.displayDay + "\n" + day.displayDate);
            btn.setCornerRadius(16);
            btn.setTextSize(12);

            boolean isSelected = day.dbDate.equals(selectedDate);
            btn.setStrokeColorResource(isSelected ? R.color.teal_700 : android.R.color.darker_gray);
            btn.setTextColor(isSelected ? getResources().getColor(R.color.teal_700) : getResources().getColor(android.R.color.tab_indicator_text));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            btn.setLayoutParams(params);

            btn.setOnClickListener(v -> {
                selectedDate = day.dbDate;
                if (tvSelectedDate != null) {
                    tvSelectedDate.setText("Chọn ngày: " + day.displayDay + " (" + day.displayDate + ")");
                }
                renderDateButtons();
                loadSlotsForCourt(courtId, selectedDate);
            });

            layoutDates.addView(btn);
        }
    }

    private void setupSubCourts() {
        String[] subCourts = {
                "Sân 1", "Sân 2", "Sân 3", "Sân 4", "Sân 5", "Sân 6",
                "Sân 7", "Sân 8", "Sân 9", "Sân 10", "Sân 11", "Sân 12",
                "Sân A", "Sân B", "Sân C", "Sân D", "Sân E", "Sân F"
        };

        layoutSubCourts.removeAllViews();
        for (String courtStr : subCourts) {
            MaterialButton btn = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            btn.setText(courtStr);
            btn.setCornerRadius(20);
            btn.setTextSize(13);

            boolean isSelected = courtStr.equals(selectedSubCourt);
            btn.setStrokeColorResource(isSelected ? R.color.teal_700 : android.R.color.darker_gray);
            btn.setTextColor(isSelected ? getResources().getColor(R.color.teal_700) : getResources().getColor(android.R.color.tab_indicator_text));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            btn.setLayoutParams(params);

            btn.setOnClickListener(v -> {
                selectedSubCourt = courtStr;
                if (tvSelectedSubCourt != null) {
                    tvSelectedSubCourt.setText("Chọn sân: " + courtStr + " (Đang chọn)");
                }
                setupSubCourts();
                loadSlotsForCourt(courtId, selectedDate);
            });

            layoutSubCourts.addView(btn);
        }
    }

    private void loadSlotsForCourt(String cId, String date) {
        if (currentSlotsRef != null && currentSlotsListener != null) {
            currentSlotsRef.removeEventListener(currentSlotsListener);
        }

        // ĐƯỜNG DẪN DATABASE: timeSlots/{courtId}/{subCourt}/{selectedDate}
        currentSlotsRef = mDatabase.child("timeSlots").child(cId).child(selectedSubCourt).child(date);

        currentSlotsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    timeSlotList.clear();

                    if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                        initDefaultSlotsSafely(currentSlotsRef, cId, date);
                        return;
                    }

                    for (DataSnapshot item : snapshot.getChildren()) {
                        String sId = item.getKey();
                        String startTime = item.child("startTime").getValue(String.class);
                        String endTime = item.child("endTime").getValue(String.class);
                        String status = item.child("status").getValue(String.class);

                        Double price = 80000.0;
                        try {
                            Object pObj = item.child("price").getValue();
                            if (pObj instanceof Number) price = ((Number) pObj).doubleValue();
                        } catch (Exception ignored) {}

                        TimeSlot slot = new TimeSlot(sId, cId, date,
                                startTime != null ? startTime : "06:00",
                                endTime != null ? endTime : "07:00",
                                price,
                                status != null ? status : "AVAILABLE");

                        timeSlotList.add(slot);
                    }

                    timeSlotAdapter.notifyDataSetChanged();
                    calculateTotalPrice();
                } catch (Exception e) {
                    Toast.makeText(CourtDetailActivity.this, "Lỗi nạp khung giờ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };

        currentSlotsRef.addValueEventListener(currentSlotsListener);
    }

    private void initDefaultSlotsSafely(DatabaseReference ref, String cId, String date) {
        String[] startTimes = {"06:00", "07:00", "08:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00"};
        String[] endTimes   = {"07:00", "08:00", "09:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00"};
        double[] prices     = {80000, 80000, 80000, 80000, 80000, 120000, 120000, 120000, 120000};

        Map<String, Object> batchSlots = new HashMap<>();
        timeSlotList.clear();

        for (int i = 0; i < startTimes.length; i++) {
            String slotId = "slot_" + (i + 1);
            TimeSlot s = new TimeSlot(slotId, cId, date, startTimes[i], endTimes[i], prices[i], "AVAILABLE");
            timeSlotList.add(s);
            batchSlots.put(slotId, s);
        }

        ref.setValue(batchSlots);
        timeSlotAdapter.notifyDataSetChanged();
        calculateTotalPrice();
    }

    private void calculateTotalPrice() {
        double total = 0;
        for (TimeSlot slot : timeSlotList) {
            if ("SELECTED".equalsIgnoreCase(slot.getStatus())) {
                total += slot.getPrice();
            }
        }
        if (tvTotalPrice != null) {
            tvTotalPrice.setText(new DecimalFormat("###,###,###").format(total) + "đ");
        }
    }

    private void processBookingWithTransaction() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để đặt sân!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<TimeSlot> selectedSlots = new ArrayList<>();
        double total = 0;
        for (TimeSlot slot : timeSlotList) {
            if ("SELECTED".equalsIgnoreCase(slot.getStatus())) {
                selectedSlots.add(slot);
                total += slot.getPrice();
            }
        }

        if (selectedSlots.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất 1 khung giờ!", Toast.LENGTH_SHORT).show();
            return;
        }

        final double finalTotal = total;
        final String userEmail = currentUser.getEmail();
        final String userId = currentUser.getUid();

        // 1. Tạo khối mã QR thu nhỏ 180dp
        LinearLayout layoutQr = new LinearLayout(this);
        layoutQr.setOrientation(LinearLayout.VERTICAL);
        layoutQr.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
        layoutQr.setPadding(20, 10, 20, 10);

        ImageView ivQr = new ImageView(this);
        ivQr.setImageResource(R.drawable.qrtest);

        int qrSizeInPx = (int) (180 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(qrSizeInPx, qrSizeInPx);
        params.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        ivQr.setLayoutParams(params);
        ivQr.setScaleType(ImageView.ScaleType.FIT_CENTER);
        ivQr.setAdjustViewBounds(true);
        layoutQr.addView(ivQr);

        new AlertDialog.Builder(this)
                .setTitle("Thanh toán tiền cọc")
                .setMessage("Sân: " + courtName + " (" + selectedSubCourt + ")\nNgày: " + selectedDate + "\nTổng tiền: " + new DecimalFormat("###,###,###").format(finalTotal) + "đ")
                .setView(layoutQr)
                .setPositiveButton("Xác nhận đã chuyển khoản", (dialog, which) -> {
                    // Lưu đơn vào bookings
                    String bookingId = mDatabase.child("bookings").push().getKey();

                    Map<String, Object> bookingMap = new HashMap<>();
                    bookingMap.put("bookingId", bookingId);
                    bookingMap.put("courtId", courtName + " (" + selectedSubCourt + ")");
                    bookingMap.put("actualCourtId", courtId);
                    bookingMap.put("subCourt", selectedSubCourt);
                    bookingMap.put("date", selectedDate);
                    bookingMap.put("userEmail", userEmail);
                    bookingMap.put("userId", userId);
                    bookingMap.put("totalPrice", finalTotal);
                    bookingMap.put("status", "PENDING");
                    bookingMap.put("bookedSlots", selectedSlots);
                    bookingMap.put("timestamp", System.currentTimeMillis());
                    bookingMap.put("reviewed", false);

                    if (bookingId != null) {
                        mDatabase.child("bookings").child(bookingId).setValue(bookingMap);
                    }

                    // Khóa ca giờ thành BOOKED trên Firebase
                    for (TimeSlot slot : selectedSlots) {
                        String sId = slot.get_id() != null ? slot.get_id() : "slot_01";
                        DatabaseReference slotRef = mDatabase.child("timeSlots").child(courtId).child(selectedSubCourt).child(selectedDate).child(sId);
                        slotRef.child("status").setValue("BOOKED");
                        slot.setStatus("BOOKED");
                    }

                    timeSlotAdapter.notifyDataSetChanged();
                    calculateTotalPrice();
                    Toast.makeText(CourtDetailActivity.this, "🎉 Đã gửi đơn đặt! Vui lòng chờ chủ sân duyệt.", Toast.LENGTH_LONG).show();
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentSlotsRef != null && currentSlotsListener != null) {
            currentSlotsRef.removeEventListener(currentSlotsListener);
        }
    }
}
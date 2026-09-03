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
import com.example.badmintonbooking.data.TimeSlot;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CourtDetailActivity extends AppCompatActivity {

    private RecyclerView rvTimeSlots;
    private TimeSlotAdapter timeSlotAdapter;
    private List<TimeSlot> timeSlotList;
    private TextView tvTotalPrice, tvDetailCourtName, tvSelectedSubCourt;
    private LinearLayout layoutSubCourts;
    private String courtId, courtName;
    private DatabaseReference mDatabase;
    private String selectedDate;
    private String selectedSubCourt = "Sân 1";

    // Quản lý listener để tránh xung đột dữ liệu giữa 18 sân con
    private DatabaseReference currentSlotsRef;
    private ValueEventListener currentSlotsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_court_detail);

        // 1. Nhận thông tin cụm sân truyền sang
        courtName = getIntent().getStringExtra("COURT_NAME");
        courtId = getIntent().getStringExtra("COURT_ID");
        if (courtId == null || courtId.isEmpty()) courtId = "court_02"; // Mặc định khớp với database
        if (courtName == null || courtName.isEmpty()) courtName = "Sân Cầu Lông ĐH Công Nghiệp";

        // 2. Ánh xạ các thành phần giao diện
        tvDetailCourtName = findViewById(R.id.tvDetailCourtName);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        rvTimeSlots = findViewById(R.id.rvTimeSlots);
        tvSelectedSubCourt = findViewById(R.id.tvSelectedSubCourt);
        layoutSubCourts = findViewById(R.id.layoutSubCourts);
        MaterialButton btnBookCourt = findViewById(R.id.btnBookCourt);
        ImageView ivBack = findViewById(R.id.ivBack);

        if (tvDetailCourtName != null) tvDetailCourtName.setText(courtName);
        if (ivBack != null) ivBack.setOnClickListener(v -> finish());

        // 3. Khởi tạo danh sách 18 nút chọn sân con (Sân 1 -> 12, Sân A -> F)
        if (layoutSubCourts != null) {
            setupSubCourts();
        }

        // 4. Khởi tạo danh sách khung giờ cuộn ngang (Horizontal)
        rvTimeSlots.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        timeSlotList = new ArrayList<>();
        timeSlotAdapter = new TimeSlotAdapter(this, timeSlotList, this::calculateTotalPrice);
        rvTimeSlots.setAdapter(timeSlotAdapter);

        // 5. Kết nối Firebase & Nạp lịch theo ngày hiện tại
        mDatabase = FirebaseDatabase.getInstance("https://badminton-booking-9e3a2-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        loadSlotsForCourt(courtId, selectedDate);

        // 6. Bắt sự kiện bấm nút ĐẶT SÂN
        if (btnBookCourt != null) {
            btnBookCourt.setOnClickListener(v -> processBookingWithTransaction());
        }
    }

    // Khởi tạo 18 nút chọn sân con theo sơ đồ
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
                setupSubCourts();
                loadSlotsForCourt(courtId, selectedDate); // Tải lại ca giờ của đúng sân con vừa bấm
            });

            layoutSubCourts.addView(btn);
        }
    }

    // Nạp lịch trống theo thời gian thực (Giữ nguyên trạng thái BOOKED, không ghi đè)
    private void loadSlotsForCourt(String cId, String date) {
        // Hủy listener cũ để tránh xung đột giữa các sân con
        if (currentSlotsRef != null && currentSlotsListener != null) {
            currentSlotsRef.removeEventListener(currentSlotsListener);
        }

        currentSlotsRef = mDatabase.child("timeSlots").child(cId).child(selectedSubCourt).child(date);

        if (tvSelectedSubCourt != null) {
            tvSelectedSubCourt.setText("Đang chọn: " + selectedSubCourt + " (Nhánh: " + selectedSubCourt + ")");
        }

        currentSlotsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    timeSlotList.clear();

                    // CHỈ KHỞI TẠO MẪU KHI NHÁNH NÀY CHƯA TỒN TẠI (KHÔNG BAO GIỜ TỰ Ý RESET)
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

                        // Luôn đọc chính xác trạng thái BOOKED hoặc AVAILABLE từ Firebase
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

    // Luồng Đặt sân
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

        LinearLayout layoutQr = new LinearLayout(this);
        layoutQr.setOrientation(LinearLayout.VERTICAL);
        layoutQr.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
        layoutQr.setPadding(20, 10, 20, 10);

        ImageView ivQr = new ImageView(this);
        ivQr.setImageResource(R.drawable.qrtest);

        // Chuyển đổi 180dp sang pixel theo mật độ màn hình máy
        int qrSizeInPx = (int) (180 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(qrSizeInPx, qrSizeInPx);
        params.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        ivQr.setLayoutParams(params);
        ivQr.setScaleType(ImageView.ScaleType.FIT_CENTER);
        ivQr.setAdjustViewBounds(true);

        layoutQr.addView(ivQr);

        new AlertDialog.Builder(this)
                .setTitle("Thanh toán tiền cọc")
                .setMessage("Sân: " + courtName + " (" + selectedSubCourt + ")\nTổng tiền: " + new DecimalFormat("###,###,###").format(finalTotal) + "đ")
                .setView(layoutQr)
                .setPositiveButton("Xác nhận đã chuyển khoản", (dialog, which) -> {
                    // 1. Tạo đơn PENDING lưu vào nhánh bookings
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
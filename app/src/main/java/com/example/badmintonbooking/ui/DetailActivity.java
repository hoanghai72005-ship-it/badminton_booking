package com.example.badmintonbooking.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.badmintonbooking.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class DetailActivity extends AppCompatActivity {

    private TextView tvDetailBookingId, tvDetailCourtName, tvDetailTimeSlots, tvDetailTotalAmount;
    private TextView tvStatusChoXacNhan, tvStatusDaXacNhan, tvStatusHoanThanh;
    private MaterialButton btnHuyDon, btnDanhGia;
    private DatabaseReference bookingRef;
    private String bookingId, courtId;
    private boolean isReviewed = false;
    private String currentStatus = "PENDING";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        bookingId = getIntent().getStringExtra("BOOKING_ID");
        if (bookingId == null) {
            Toast.makeText(this, "Không tìm thấy mã đơn hàng!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        tvDetailBookingId = findViewById(R.id.tvDetailBookingId);
        tvDetailCourtName = findViewById(R.id.tvDetailCourtName);
        tvDetailTimeSlots = findViewById(R.id.tvDetailTimeSlots);
        tvDetailTotalAmount = findViewById(R.id.tvDetailTotalAmount);

        tvStatusChoXacNhan = findViewById(R.id.tvStatusChoXacNhan);
        tvStatusDaXacNhan = findViewById(R.id.tvStatusDaXacNhan);
        tvStatusHoanThanh = findViewById(R.id.tvStatusHoanThanh);

        btnHuyDon = findViewById(R.id.btnHuyDon);
        btnDanhGia = findViewById(R.id.btnDanhGia);

        bookingRef = FirebaseDatabase.getInstance("https://badminton-booking-9e3a2-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("bookings").child(bookingId);

        loadBookingDetailsSafely();

        btnHuyDon.setOnClickListener(v -> handleCancelBooking());
        btnDanhGia.setOnClickListener(v -> showReviewDialog());
    }

    private void loadBookingDetailsSafely() {
        bookingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    if (!snapshot.exists()) return;

                    String shortId = bookingId.length() > 6 ? bookingId.substring(0, 6) : bookingId;
                    tvDetailBookingId.setText("Mã đơn: #" + shortId);

                    courtId = snapshot.child("courtId").getValue(String.class);
                    if (courtId != null) tvDetailCourtName.setText(courtId);

                    Double total = 0.0;
                    Object tObj = snapshot.child("totalPrice").getValue();
                    if (tObj instanceof Number) total = ((Number) tObj).doubleValue();
                    tvDetailTotalAmount.setText("Tổng thanh toán: " + new DecimalFormat("###,###,###").format(total) + " VNĐ");

                    // Đọc danh sách ca giờ
                    DataSnapshot slotsSnap = snapshot.child("bookedSlots");
                    if (slotsSnap.exists()) {
                        StringBuilder sb = new StringBuilder("Khung giờ đặt:\n");
                        for (DataSnapshot s : slotsSnap.getChildren()) {
                            String st = s.child("startTime").getValue(String.class);
                            String et = s.child("endTime").getValue(String.class);
                            if (st != null && et != null) {
                                sb.append("• ").append(st).append(" - ").append(et).append("\n");
                            }
                        }
                        tvDetailTimeSlots.setText(sb.toString().trim());
                    }

                    currentStatus = snapshot.child("status").getValue(String.class);
                    if (currentStatus == null) currentStatus = "PENDING";
                    updateStatusUI(currentStatus);

                    Boolean rev = snapshot.child("reviewed").getValue(Boolean.class);
                    isReviewed = Boolean.TRUE.equals(rev);

                    if (isReviewed) {
                        btnDanhGia.setEnabled(false);
                        btnDanhGia.setText("Đã đánh giá");
                        btnDanhGia.setAlpha(0.5f);
                    } else {
                        btnDanhGia.setEnabled(true);
                        btnDanhGia.setText("Đánh giá sân");
                        btnDanhGia.setAlpha(1.0f);
                    }

                    if ("CANCELLED".equalsIgnoreCase(currentStatus)) {
                        btnHuyDon.setEnabled(false);
                        btnHuyDon.setText("Đã hủy đơn");
                        btnHuyDon.setAlpha(0.5f);
                    }
                } catch (Exception e) {
                    Toast.makeText(DetailActivity.this, "Lỗi nạp đơn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateStatusUI(String status) {
        // Reset mặc định
        tvStatusChoXacNhan.setBackgroundColor(Color.parseColor("#F1F5F9"));
        tvStatusChoXacNhan.setTextColor(Color.parseColor("#64748B"));
        tvStatusDaXacNhan.setBackgroundColor(Color.parseColor("#F1F5F9"));
        tvStatusDaXacNhan.setTextColor(Color.parseColor("#64748B"));
        tvStatusHoanThanh.setBackgroundColor(Color.parseColor("#F1F5F9"));
        tvStatusHoanThanh.setTextColor(Color.parseColor("#64748B"));

        if ("PENDING".equalsIgnoreCase(status)) {
            tvStatusChoXacNhan.setText("●  Chờ xác nhận thanh toán");
            tvStatusChoXacNhan.setBackgroundColor(Color.parseColor("#FEF3C7"));
            tvStatusChoXacNhan.setTextColor(Color.parseColor("#B45309"));
        } else if ("CONFIRMED".equalsIgnoreCase(status)) {
            tvStatusDaXacNhan.setText("●  Đã xác nhận đặt sân");
            tvStatusDaXacNhan.setBackgroundColor(Color.parseColor("#DCFCE7"));
            tvStatusDaXacNhan.setTextColor(Color.parseColor("#16A34A"));
        } else if ("COMPLETED".equalsIgnoreCase(status)) {
            tvStatusHoanThanh.setText("●  Hoàn thành ca chơi");
            tvStatusHoanThanh.setBackgroundColor(Color.parseColor("#DCFCE7"));
            tvStatusHoanThanh.setTextColor(Color.parseColor("#16A34A"));
        } else if ("CANCELLED".equalsIgnoreCase(status)) {
            tvStatusChoXacNhan.setText("●  Đơn đặt đã bị hủy");
            tvStatusChoXacNhan.setBackgroundColor(Color.parseColor("#FEE2E2"));
            tvStatusChoXacNhan.setTextColor(Color.parseColor("#DC2626"));
        }
    }

    private void handleCancelBooking() {
        new AlertDialog.Builder(this)
                .setTitle("Hủy lịch đặt")
                .setMessage("Bạn có chắc chắn muốn hủy đơn đặt sân này? Khung giờ sẽ được giải phóng ngay lập tức.")
                .setPositiveButton("Hủy đơn", (dialog, which) -> {
                    bookingRef.child("status").setValue("CANCELLED");

                    bookingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            try {
                                String targetCourtId = snapshot.child("actualCourtId").getValue(String.class);
                                if (targetCourtId == null) targetCourtId = "court_02"; // Khớp đúng nhánh trong ảnh Firebase

                                String subCourt = snapshot.child("subCourt").getValue(String.class);
                                if (subCourt == null) subCourt = "Sân 1";

                                String bookingDate = snapshot.child("date").getValue(String.class);
                                if (bookingDate == null) bookingDate = "2026-09-03";

                                DatabaseReference timeSlotsRoot = FirebaseDatabase.getInstance("https://badminton-booking-9e3a2-default-rtdb.asia-southeast1.firebasedatabase.app/")
                                        .getReference("timeSlots");

                                DataSnapshot slotsSnap = snapshot.child("bookedSlots");
                                if (slotsSnap.exists()) {
                                    for (DataSnapshot s : slotsSnap.getChildren()) {
                                        String sId = s.child("_id").getValue(String.class);
                                        if (sId == null) sId = s.child("slotId").getValue(String.class);
                                        if (sId == null) sId = s.getKey();

                                        if (sId != null) {
                                            timeSlotsRoot.child(targetCourtId)
                                                    .child(subCourt)
                                                    .child(bookingDate)
                                                    .child(sId)
                                                    .child("status").setValue("AVAILABLE");

                                            timeSlotsRoot.child("court_01")
                                                    .child(subCourt)
                                                    .child(bookingDate)
                                                    .child(sId)
                                                    .child("status").setValue("AVAILABLE");
                                        }
                                    }
                                }

                                Toast.makeText(DetailActivity.this, "🎉 Đã hủy đơn và trả sân về trạng thái Còn trống!", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Toast.makeText(DetailActivity.this, "Lỗi hủy: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void showReviewDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_review, null);
        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        EditText edtComment = dialogView.findViewById(R.id.edtComment);

        new AlertDialog.Builder(this)
                .setTitle("Đánh giá cụm sân")
                .setView(dialogView)
                .setPositiveButton("Gửi", (dialog, which) -> {
                    float rating = ratingBar.getRating();
                    String comment = edtComment.getText().toString().trim();
                    String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                            ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "anonymous";

                    DatabaseReference revRef = FirebaseDatabase.getInstance("https://badminton-booking-9e3a2-default-rtdb.asia-southeast1.firebasedatabase.app/")
                            .getReference("reviews").child(courtId != null ? courtId : "court_01");

                    String rId = revRef.push().getKey();
                    Map<String, Object> map = new HashMap<>();
                    map.put("bookingId", bookingId);
                    map.put("userId", userId);
                    map.put("rating", rating);
                    map.put("comment", comment);
                    map.put("createdAt", System.currentTimeMillis());

                    if (rId != null) {
                        revRef.child(rId).setValue(map).addOnSuccessListener(aVoid -> {
                            bookingRef.child("reviewed").setValue(true);
                            Toast.makeText(this, "Cảm ơn bạn đã gửi nhận xét!", Toast.LENGTH_SHORT).show();
                        });
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
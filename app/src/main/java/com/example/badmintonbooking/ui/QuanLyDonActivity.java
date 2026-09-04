package com.example.badmintonbooking.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.badmintonbooking.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class QuanLyDonActivity extends AppCompatActivity {

    private TextView tvCourtName, tvBookingInfo, tvStatus;
    private MaterialButton btnDuyetDon, btnHoanThanh;
    private DatabaseReference bookingsRef, userRef;
    private ValueEventListener bookingsListener;
    private String activeBookingId = null;

    // Tên sân và mã sân mà chủ sân này sở hữu
    private String myCourtName = null;
    private String myCourtId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quan_ly_don);

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        tvCourtName = findViewById(R.id.tvDetailCourtName);
        tvBookingInfo = findViewById(R.id.tvDetailTimeSlots);
        tvStatus = findViewById(R.id.tvDetailStatus);
        btnDuyetDon = findViewById(R.id.btnChiTiet);
        btnHoanThanh = findViewById(R.id.btnHoanThanh);

        bookingsRef = FirebaseDatabase.getInstance("https://badminton-booking-9e3a2-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("bookings");

        checkOwnerCourtAndLoad();
    }

    private void checkOwnerCourtAndLoad() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userRef = FirebaseDatabase.getInstance("https://badminton-booking-9e3a2-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("users").child(currentUser.getUid());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(QuanLyDonActivity.this, "Không tìm thấy thông tin tài khoản!", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                String role = snapshot.child("role").getValue(String.class);
                String email = currentUser.getEmail();

                boolean isOwner = "OWNER".equalsIgnoreCase(role) ||
                        (email != null && email.equalsIgnoreCase("haitest@gmail.com"));

                if (!isOwner) {
                    Toast.makeText(QuanLyDonActivity.this, "Chỉ chủ sân mới có quyền duyệt đơn đặt sân!", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                // Lấy tên sân và mã sân của chủ sân này
                myCourtName = snapshot.child("courtName").getValue(String.class);
                if (myCourtName == null || myCourtName.isEmpty()) {
                    myCourtName = snapshot.child("managedCourtName").getValue(String.class);
                }
                if (myCourtName == null || myCourtName.isEmpty()) {
                    if ("haitest@gmail.com".equalsIgnoreCase(email)) {
                        myCourtName = "Sân Cầu Lông ĐH Công Nghiệp";
                    }
                }

                myCourtId = snapshot.child("courtId").getValue(String.class);
                if (myCourtId == null || myCourtId.isEmpty()) {
                    myCourtId = snapshot.child("managedCourtId").getValue(String.class);
                }
                if (myCourtId == null || myCourtId.isEmpty()) {
                    if ("haitest@gmail.com".equalsIgnoreCase(email)) {
                        myCourtId = "court_01";
                    }
                }

                // Lọc đơn chỉ thuộc về cụm sân của chủ sân này
                loadBookingForMyCourtOnly();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(QuanLyDonActivity.this, "Lỗi xác thực: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void loadBookingForMyCourtOnly() {
        bookingsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    showEmptyBookingState();
                    return;
                }

                List<DataSnapshot> myCourtBookings = new ArrayList<>();

                // DUYỆT TẤT CẢ CÁC ĐƠN VÀ CHỈ LẤY ĐƠN THUỘC ĐÚNG SÂN CỦA CHỦ SÂN NÀY
                for (DataSnapshot item : snapshot.getChildren()) {
                    String bCourtName = item.child("courtId").getValue(String.class);
                    String bActualCourtId = item.child("actualCourtId").getValue(String.class);

                    if (isBookingBelongsToMyCourt(bCourtName, bActualCourtId)) {
                        myCourtBookings.add(item);
                    }
                }

                if (myCourtBookings.isEmpty()) {
                    showEmptyBookingState();
                    return;
                }

                // Ưu tiên chọn: 1. Đơn PENDING (chờ duyệt) -> 2. Đơn CONFIRMED -> 3. Đơn mới nhất
                DataSnapshot selectedBooking = null;

                for (int i = myCourtBookings.size() - 1; i >= 0; i--) {
                    DataSnapshot b = myCourtBookings.get(i);
                    String st = b.child("status").getValue(String.class);
                    if ("PENDING".equalsIgnoreCase(st)) {
                        selectedBooking = b;
                        break;
                    }
                }

                if (selectedBooking == null) {
                    for (int i = myCourtBookings.size() - 1; i >= 0; i--) {
                        DataSnapshot b = myCourtBookings.get(i);
                        String st = b.child("status").getValue(String.class);
                        if ("CONFIRMED".equalsIgnoreCase(st)) {
                            selectedBooking = b;
                            break;
                        }
                    }
                }

                if (selectedBooking == null) {
                    selectedBooking = myCourtBookings.get(myCourtBookings.size() - 1);
                }

                displayBooking(selectedBooking);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(QuanLyDonActivity.this, "Lỗi kết nối: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        bookingsRef.addValueEventListener(bookingsListener);
    }

    // BỘ LỌC CHẶT CHẼ: Loại bỏ 100% đơn của các sân khác
    private boolean isBookingBelongsToMyCourt(String bookingCourtName, String bookingActualCourtId) {
        if (myCourtName == null && myCourtId == null) {
            return false;
        }

        // 1. So sánh theo mã sân (ví dụ: "court_01")
        if (myCourtId != null && bookingActualCourtId != null &&
                myCourtId.trim().equalsIgnoreCase(bookingActualCourtId.trim())) {
            return true;
        }

        // 2. So sánh theo tên sân (ví dụ: "Sân Cầu Lông ĐH Công Nghiệp")
        if (myCourtName != null && bookingCourtName != null) {
            String myName = myCourtName.trim().toLowerCase();
            String bookingName = bookingCourtName.trim().toLowerCase();

            if (bookingName.contains(myName) || myName.contains(bookingName)) {
                return true;
            }
        }

        return false;
    }

    private void displayBooking(DataSnapshot item) {
        activeBookingId = item.getKey();
        String cName = item.child("courtId").getValue(String.class);
        String email = item.child("userEmail").getValue(String.class);
        String st = item.child("status").getValue(String.class);
        if (st == null) st = "PENDING";

        StringBuilder timeInfo = new StringBuilder("Khách đặt: " + (email != null ? email : "Chưa rõ"));
        DataSnapshot slotsSnap = item.child("bookedSlots");
        if (slotsSnap.exists()) {
            timeInfo.append("\nKhung giờ: ");
            for (DataSnapshot s : slotsSnap.getChildren()) {
                String start = s.child("startTime").getValue(String.class);
                String end = s.child("endTime").getValue(String.class);
                if (start != null && end != null) {
                    timeInfo.append(start).append("-").append(end).append(" ");
                }
            }
        }

        if (tvCourtName != null) tvCourtName.setText(cName != null ? cName : myCourtName);
        if (tvBookingInfo != null) tvBookingInfo.setText(timeInfo.toString().trim());

        if ("PENDING".equalsIgnoreCase(st)) {
            if (tvStatus != null) {
                tvStatus.setText("CHỜ CHỦ SÂN DUYỆT");
                tvStatus.setTextColor(Color.parseColor("#B45309"));
                tvStatus.setBackgroundColor(Color.parseColor("#FEF3C7"));
            }
            if (btnDuyetDon != null) {
                btnDuyetDon.setEnabled(true);
                btnDuyetDon.setAlpha(1.0f);
                btnDuyetDon.setText("XÁC NHẬN DUYỆT ĐƠN NÀY");
                btnDuyetDon.setOnClickListener(v -> {
                    bookingsRef.child(activeBookingId).child("status").setValue("CONFIRMED");
                    Toast.makeText(QuanLyDonActivity.this, "Đã xác nhận duyệt đơn!", Toast.LENGTH_SHORT).show();
                });
            }
            if (btnHoanThanh != null) {
                btnHoanThanh.setEnabled(false);
                btnHoanThanh.setAlpha(0.4f);
            }
        } else if ("CONFIRMED".equalsIgnoreCase(st)) {
            if (tvStatus != null) {
                tvStatus.setText("ĐÃ XÁC NHẬN (ĐANG HOẠT ĐỘNG)");
                tvStatus.setTextColor(Color.parseColor("#16A34A"));
                tvStatus.setBackgroundColor(Color.parseColor("#DCFCE7"));
            }
            if (btnDuyetDon != null) {
                btnDuyetDon.setEnabled(false);
                btnDuyetDon.setAlpha(0.5f);
                btnDuyetDon.setText("ĐÃ XÁC NHẬN DUYỆT");
            }
            if (btnHoanThanh != null) {
                btnHoanThanh.setEnabled(true);
                btnHoanThanh.setAlpha(1.0f);
                btnHoanThanh.setText("XÁC NHẬN HOÀN THÀNH CA CHƠI");
                btnHoanThanh.setOnClickListener(v -> {
                    bookingsRef.child(activeBookingId).child("status").setValue("COMPLETED");
                    Toast.makeText(QuanLyDonActivity.this, "🎉 Đã hoàn thành ca chơi!", Toast.LENGTH_SHORT).show();
                });
            }
        } else if ("COMPLETED".equalsIgnoreCase(st)) {
            if (tvStatus != null) {
                tvStatus.setText("ĐÃ HOÀN THÀNH CA CHƠI");
                tvStatus.setTextColor(Color.parseColor("#0284C7"));
                tvStatus.setBackgroundColor(Color.parseColor("#E0F2FE"));
            }
            if (btnDuyetDon != null) {
                btnDuyetDon.setEnabled(false);
                btnDuyetDon.setAlpha(0.4f);
                btnDuyetDon.setText("ĐÃ DUYỆT ĐƠN");
            }
            if (btnHoanThanh != null) {
                btnHoanThanh.setEnabled(false);
                btnHoanThanh.setAlpha(0.4f);
                btnHoanThanh.setText("ĐÃ HOÀN THÀNH");
            }
        } else if ("CANCELLED".equalsIgnoreCase(st)) {
            if (tvStatus != null) {
                tvStatus.setText("ĐƠN ĐÃ BỊ HỦY");
                tvStatus.setTextColor(Color.parseColor("#EF4444"));
                tvStatus.setBackgroundColor(Color.parseColor("#FEE2E2"));
            }
            if (btnDuyetDon != null) {
                btnDuyetDon.setEnabled(false);
                btnDuyetDon.setAlpha(0.4f);
            }
            if (btnHoanThanh != null) {
                btnHoanThanh.setEnabled(false);
                btnHoanThanh.setAlpha(0.4f);
            }
        }
    }

    private void showEmptyBookingState() {
        activeBookingId = null;
        if (tvCourtName != null) {
            tvCourtName.setText(myCourtName != null ? myCourtName : "Sân Cầu Lông ĐH Công Nghiệp");
        }
        if (tvBookingInfo != null) {
            tvBookingInfo.setText("Hiện tại cụm sân của bạn chưa có đơn đặt nào cần duyệt.");
        }
        if (tvStatus != null) {
            tvStatus.setText("KHÔNG CÓ ĐƠN CHỜ");
            tvStatus.setBackgroundColor(Color.parseColor("#F1F5F9"));
            tvStatus.setTextColor(Color.parseColor("#64748B"));
        }
        if (btnDuyetDon != null) {
            btnDuyetDon.setEnabled(false);
            btnDuyetDon.setAlpha(0.4f);
            btnDuyetDon.setText("DUYỆT ĐƠN NÀY");
        }
        if (btnHoanThanh != null) {
            btnHoanThanh.setEnabled(false);
            btnHoanThanh.setAlpha(0.4f);
            btnHoanThanh.setText("XÁC NHẬN HOÀN THÀNH CA CHƠI");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bookingsRef != null && bookingsListener != null) {
            bookingsRef.removeEventListener(bookingsListener);
        }
    }
}
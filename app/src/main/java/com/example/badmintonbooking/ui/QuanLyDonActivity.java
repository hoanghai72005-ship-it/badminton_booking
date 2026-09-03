package com.example.badmintonbooking.ui;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.badmintonbooking.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.*;

public class QuanLyDonActivity extends AppCompatActivity {

    private TextView tvCourtName, tvBookingInfo, tvStatus;
    private MaterialButton btnDuyetDon, btnHoanThanh;
    private DatabaseReference bookingsRef;
    private String activeBookingId = null;

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

        loadLatestBookingForOwner();
    }

    private void loadLatestBookingForOwner() {
        // Lấy đơn mới nhất để chủ sân duyệt
        bookingsRef.limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot item : snapshot.getChildren()) {
                        activeBookingId = item.getKey();
                        String cName = item.child("courtId").getValue(String.class);
                        String email = item.child("userEmail").getValue(String.class);
                        String st = item.child("status").getValue(String.class);
                        if (st == null) st = "PENDING";

                        if (tvCourtName != null) tvCourtName.setText(cName);
                        if (tvBookingInfo != null) tvBookingInfo.setText("Khách đặt: " + email);
                        if (tvStatus != null) tvStatus.setText("Trạng thái hiện tại: " + st);

                        // Nếu đơn đang PENDING -> Cho phép duyệt
                        if ("PENDING".equalsIgnoreCase(st)) {
                            if (btnDuyetDon != null) {
                                btnDuyetDon.setEnabled(true);
                                btnDuyetDon.setText("DUYỆT ĐƠN NÀY (CONFIRMED)");
                                btnDuyetDon.setOnClickListener(v -> {
                                    bookingsRef.child(activeBookingId).child("status").setValue("CONFIRMED");
                                    Toast.makeText(QuanLyDonActivity.this, "Đã xác nhận đơn!", Toast.LENGTH_SHORT).show();
                                });
                            }
                            if (btnHoanThanh != null) btnHoanThanh.setEnabled(false);
                        }
                        // Nếu đơn đã CONFIRMED -> Bật nút hoàn thành ca chơi
                        else if ("CONFIRMED".equalsIgnoreCase(st)) {
                            if (btnDuyetDon != null) {
                                btnDuyetDon.setEnabled(false);
                                btnDuyetDon.setText("ĐÃ XÁC NHẬN");
                            }
                            if (btnHoanThanh != null) {
                                btnHoanThanh.setEnabled(true);
                                btnHoanThanh.setOnClickListener(v -> {
                                    bookingsRef.child(activeBookingId).child("status").setValue("COMPLETED");
                                    Toast.makeText(QuanLyDonActivity.this, "🎉 Đã hoàn thành ca chơi!", Toast.LENGTH_SHORT).show();
                                });
                            }
                        } else {
                            if (btnDuyetDon != null) btnDuyetDon.setEnabled(false);
                            if (btnHoanThanh != null) btnHoanThanh.setEnabled(false);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
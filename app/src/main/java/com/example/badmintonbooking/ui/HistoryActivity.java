package com.example.badmintonbooking.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.badmintonbooking.R;
import com.example.badmintonbooking.adapter.HistoryAdapter;
import com.example.badmintonbooking.data.Booking;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private HistoryAdapter historyAdapter;
    private List<Booking> bookingList;
    private LinearLayout layoutEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Nút quay lại trang Hồ sơ
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        layoutEmpty = findViewById(R.id.layoutEmpty);
        rvHistory = findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        bookingList = new ArrayList<>();
        historyAdapter = new HistoryAdapter(this, bookingList, booking -> {
            Intent intent = new Intent(HistoryActivity.this, DetailActivity.class);
            intent.putExtra("BOOKING_ID", booking.getBookingId());
            startActivity(intent);
        });
        rvHistory.setAdapter(historyAdapter);

        loadUserHistorySafely();
    }

    private void loadUserHistorySafely() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userEmail = currentUser.getEmail();
        DatabaseReference bookingsRef = FirebaseDatabase.getInstance("https://badminton-booking-9e3a2-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("bookings");

        // Lắng nghe dữ liệu đơn đặt
        bookingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    bookingList.clear();
                    for (DataSnapshot item : snapshot.getChildren()) {
                        // Đọc thủ công từng trường để chống crash 100%
                        String email = item.child("userEmail").getValue(String.class);

                        // Chỉ lấy các đơn thuộc tài khoản đang đăng nhập
                        if (email != null && userEmail != null && !email.trim().equalsIgnoreCase(userEmail.trim())) {
                            continue;
                        }

                        String bId = item.child("bookingId").getValue(String.class);
                        if (bId == null) bId = item.getKey();

                        String cId = item.child("courtId").getValue(String.class);
                        if (cId == null) cId = "Sân Cầu Lông";

                        Double total = 0.0;
                        try {
                            Object tObj = item.child("totalPrice").getValue();
                            if (tObj instanceof Number) total = ((Number) tObj).doubleValue();
                        } catch (Exception ignored) {}

                        String status = item.child("status").getValue(String.class);
                        if (status == null) status = "CONFIRMED";

                        Booking booking = new Booking(bId, cId, email, total, null, System.currentTimeMillis());
                        booking.setStatus(status);

                        // Đưa đơn đặt mới nhất lên đầu danh sách
                        bookingList.add(0, booking);
                    }

                    historyAdapter.notifyDataSetChanged();

                    if (layoutEmpty != null) {
                        layoutEmpty.setVisibility(bookingList.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                } catch (Exception e) {
                    Toast.makeText(HistoryActivity.this, "Lỗi nạp lịch sử: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HistoryActivity.this, "Lỗi kết nối: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
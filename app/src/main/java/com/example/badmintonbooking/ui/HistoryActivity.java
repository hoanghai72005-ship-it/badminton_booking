package com.example.badmintonbooking.ui;

import android.content.Intent;
import android.graphics.Color;
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
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private HistoryAdapter historyAdapter;
    private List<Booking> allBookingList;
    private List<Booking> displayedBookingList;
    private LinearLayout layoutEmpty;

    // 4 tab lọc trạng thái
    private MaterialButton btnFilterAll, btnFilterConfirmed, btnFilterCompleted, btnFilterCancelled;
    private String currentFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        layoutEmpty = findViewById(R.id.layoutEmpty);
        rvHistory = findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterConfirmed = findViewById(R.id.btnFilterConfirmed);
        btnFilterCompleted = findViewById(R.id.btnFilterCompleted);
        btnFilterCancelled = findViewById(R.id.btnFilterCancelled);

        allBookingList = new ArrayList<>();
        displayedBookingList = new ArrayList<>();

        historyAdapter = new HistoryAdapter(this, displayedBookingList, booking -> {
            Intent intent = new Intent(HistoryActivity.this, DetailActivity.class);
            intent.putExtra("BOOKING_ID", booking.getBookingId());
            startActivity(intent);
        });
        rvHistory.setAdapter(historyAdapter);

        // Sự kiện click tương tác cho cả 4 tab
        btnFilterAll.setOnClickListener(v -> applyFilter("ALL"));
        btnFilterConfirmed.setOnClickListener(v -> applyFilter("CONFIRMED"));
        btnFilterCompleted.setOnClickListener(v -> applyFilter("COMPLETED"));
        btnFilterCancelled.setOnClickListener(v -> applyFilter("CANCELLED"));

        loadUserHistorySafely();
    }

    private void applyFilter(String filter) {
        this.currentFilter = filter;
        updateTabStyles(filter);

        displayedBookingList.clear();
        for (Booking booking : allBookingList) {
            String st = booking.getStatus();
            if (st == null) st = "CONFIRMED";

            if ("ALL".equalsIgnoreCase(filter)) {
                displayedBookingList.add(booking);
            } else if ("CONFIRMED".equalsIgnoreCase(filter)) {
                if ("CONFIRMED".equalsIgnoreCase(st) || "PENDING".equalsIgnoreCase(st)) {
                    displayedBookingList.add(booking);
                }
            } else if ("COMPLETED".equalsIgnoreCase(filter)) {
                if ("COMPLETED".equalsIgnoreCase(st)) {
                    displayedBookingList.add(booking);
                }
            } else if ("CANCELLED".equalsIgnoreCase(filter)) {
                if ("CANCELLED".equalsIgnoreCase(st)) {
                    displayedBookingList.add(booking);
                }
            }
        }

        historyAdapter.notifyDataSetChanged();

        if (layoutEmpty != null) {
            layoutEmpty.setVisibility(displayedBookingList.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void updateTabStyles(String selectedFilter) {
        setTabStyle(btnFilterAll, "ALL".equalsIgnoreCase(selectedFilter));
        setTabStyle(btnFilterConfirmed, "CONFIRMED".equalsIgnoreCase(selectedFilter));
        setTabStyle(btnFilterCompleted, "COMPLETED".equalsIgnoreCase(selectedFilter));
        setTabStyle(btnFilterCancelled, "CANCELLED".equalsIgnoreCase(selectedFilter));
    }

    private void setTabStyle(MaterialButton button, boolean isSelected) {
        if (button == null) return;
        if (isSelected) {
            button.setTextColor(Color.parseColor("#059669"));
            button.setStrokeColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#059669")));
            button.setStrokeWidth(3);
            button.setBackgroundColor(Color.parseColor("#DCFCE7"));
        } else {
            button.setTextColor(Color.parseColor("#64748B"));
            button.setStrokeColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#CBD5E1")));
            button.setStrokeWidth(2);
            button.setBackgroundColor(Color.TRANSPARENT);
        }
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

        bookingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    allBookingList.clear();
                    for (DataSnapshot item : snapshot.getChildren()) {
                        String email = item.child("userEmail").getValue(String.class);

                        if (email != null && userEmail != null && !email.trim().equalsIgnoreCase(userEmail.trim())) {
                            continue;
                        }

                        String bId = item.child("bookingId").getValue(String.class);
                        if (bId == null) bId = item.getKey();

                        String cId = item.child("courtId").getValue(String.class);
                        if (cId == null) cId = "Sân Cầu Lông ĐH Công Nghiệp";

                        Double total = 0.0;
                        try {
                            Object tObj = item.child("totalPrice").getValue();
                            if (tObj instanceof Number) total = ((Number) tObj).doubleValue();
                        } catch (Exception ignored) {}

                        String status = item.child("status").getValue(String.class);
                        if (status == null) status = "CONFIRMED";

                        Booking booking = new Booking(bId, cId, email, total, null, System.currentTimeMillis());
                        booking.setStatus(status);

                        allBookingList.add(0, booking);
                    }

                    applyFilter(currentFilter);

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
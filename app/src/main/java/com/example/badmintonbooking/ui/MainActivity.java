package com.example.badmintonbooking.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.badmintonbooking.R;
import com.example.badmintonbooking.adapter.CourtAdapter;
import com.example.badmintonbooking.model.Court;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.content.Intent;
public class MainActivity extends AppCompatActivity {

    private RecyclerView rvCourts;
    private CourtAdapter courtAdapter;
    private List<Court> courtList;
    private DatabaseReference courtsRef;

    @Override
    protected void onResume() {
        super.onResume();
        // Mỗi khi trang chủ hiện lên màn hình, ép thanh điều hướng sáng nút Home
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Trỏ vào đúng giao diện mới vừa dán
        setContentView(R.layout.activity_main);

        // 1. Ánh xạ RecyclerView
        rvCourts = findViewById(R.id.rvCourts);
        rvCourts.setHasFixedSize(true);
        rvCourts.setLayoutManager(new LinearLayoutManager(this));

        // 2. Khởi tạo danh sách và Adapter
        courtList = new ArrayList<>();
        courtAdapter = new CourtAdapter(this, courtList);
        rvCourts.setAdapter(courtAdapter);

        // 3. Kết nối Firebase
        courtsRef = FirebaseDatabase.getInstance("https://badminton-booking-9e3a2-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("courts");
        loadCourtsFromFirebase();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation); // Đảm bảo ID này khớp với file activity_main.xml

        // Tắt hiệu ứng nháy màu (nếu muốn)
        // bottomNavigationView.setItemIconTintList(null);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                return true;
            }
            else if (itemId == R.id.nav_booking) {
                // Tab 2: "Đặt sân" -> Mở Bản đồ các sân xung quanh (MapActivity)
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            }
            else if (itemId == R.id.nav_match) {
                // Tab 3: "Ghép sân" -> Mở Mạng xã hội ghép kèo (MatchListActivity)
                Intent intent = new Intent(MainActivity.this, MatchListActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            }
            else if (itemId == R.id.nav_profile) {
                // Tab 4: "Hồ sơ" -> Mở Màn hình Hồ sơ cá nhân (ProfileActivity)
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    private void loadCourtsFromFirebase() {
        courtsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                courtList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Court court = dataSnapshot.getValue(Court.class);
                    if (court != null) {
                        courtList.add(court);
                    }
                }
                courtAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Lỗi tải dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showProfileDialog() {
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        String email = user != null ? user.getEmail() : "Chưa đăng nhập";

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Hồ sơ cá nhân")
                .setMessage("Tài khoản: " + email + "\nĐiểm uy tín: 5.0 ⭐")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Đóng", null)
                .show();
    }
}
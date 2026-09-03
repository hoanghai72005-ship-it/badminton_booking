package com.example.badmintonbooking.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.badmintonbooking.R;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileName, tvProfileEmail, tvProfileTrustScore;
    private MaterialCardView cardLichSu, cardEditProfile;
    private DatabaseReference userRef;
    private FirebaseUser currentUser;
    private LinearLayout layoutOwnerSection, layoutAdminSection;
    private MaterialCardView cardDuyetDon, cardQuanLySan, cardKiemDuyetBai;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfileTrustScore = findViewById(R.id.tvProfileTrustScore);
        cardLichSu = findViewById(R.id.cardLichSu);
        cardEditProfile = findViewById(R.id.cardEditProfile);
        layoutOwnerSection = findViewById(R.id.layoutOwnerSection);
        layoutAdminSection = findViewById(R.id.layoutAdminSection);
        cardDuyetDon = findViewById(R.id.cardDuyetDon);
        cardQuanLySan = findViewById(R.id.cardQuanLySan);
        cardKiemDuyetBai = findViewById(R.id.cardKiemDuyetBai);

        if (cardDuyetDon != null) {
            cardDuyetDon.setOnClickListener(v -> startActivity(new Intent(this, QuanLyDonActivity.class)));
        }
        if (cardKiemDuyetBai != null) {
            cardKiemDuyetBai.setOnClickListener(v -> startActivity(new Intent(this, MatchListActivity.class)));
        }

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            tvProfileEmail.setText(currentUser.getEmail());

            // Kết nối vào đúng tài khoản đang đăng nhập: users/{uid}
            userRef = FirebaseDatabase.getInstance("https://badminton-booking-9e3a2-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference("users").child(currentUser.getUid());

            loadCurrentUserData();
        } else {
            tvProfileName.setText("Khách");
            tvProfileEmail.setText("Chưa đăng nhập");
        }

        // 1. Bấm vào Lịch sử đặt sân -> Mở HistoryActivity
        cardLichSu.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, HistoryActivity.class));
        });

        // 2. Bấm vào Sửa thông tin cá nhân
        cardEditProfile.setOnClickListener(v -> showEditNameDialog());

        // 3. Nút Đăng xuất
        findViewById(R.id.btnSignOut).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        if (bottomNav != null) {
            // Ép sáng tab Hồ sơ
            bottomNav.setSelectedItemId(R.id.nav_profile);

            bottomNav.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_booking) {
                    Intent intent = new Intent(ProfileActivity.this, MapActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_match) {
                    Intent intent = new Intent(ProfileActivity.this, MatchListActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    return true;
                }
                return false;
            });
        }
    }

    private void loadCurrentUserData() {
        if (userRef == null) return;

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // 1. ĐOẠN KIỂM TRA ROLE NẰM BÊN TRONG onDataChange ĐỂ HẾT BÁO ĐỎ:
                    String role = snapshot.child("role").getValue(String.class);
                    if ("OWNER".equalsIgnoreCase(role)) {
                        tvProfileTrustScore.setText("Vai trò: CHỦ SÂN 🏆");
                        if (layoutOwnerSection != null) layoutOwnerSection.setVisibility(View.VISIBLE);
                        if (layoutAdminSection != null) layoutAdminSection.setVisibility(View.GONE);
                    } else if ("ADMIN".equalsIgnoreCase(role)) {
                        tvProfileTrustScore.setText("Vai trò: QUẢN TRỊ VIÊN 🛡️");
                        if (layoutAdminSection != null) layoutAdminSection.setVisibility(View.VISIBLE);
                        if (layoutOwnerSection != null) layoutOwnerSection.setVisibility(View.GONE);
                    } else {
                        tvProfileTrustScore.setText("Vai trò: NGƯỜI CHƠI 🏸");
                        if (layoutOwnerSection != null) layoutOwnerSection.setVisibility(View.GONE);
                        if (layoutAdminSection != null) layoutAdminSection.setVisibility(View.GONE);
                    }

                    // 2. Lấy họ tên thực tế từ Firebase
                    String fullName = snapshot.child("fullName").getValue(String.class);
                    if (fullName != null && !fullName.isEmpty()) {
                        tvProfileName.setText(fullName);
                    } else {
                        String email = currentUser != null ? currentUser.getEmail() : null;
                        tvProfileName.setText(email != null ? email.split("@")[0] : "Người chơi");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditNameDialog() {
        EditText edtNewName = new EditText(this);
        edtNewName.setHint("Nhập họ và tên mới");
        edtNewName.setText(tvProfileName.getText());

        new AlertDialog.Builder(this)
                .setTitle("Sửa thông tin cá nhân")
                .setView(edtNewName)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String newName = edtNewName.getText().toString().trim();
                    if (!newName.isEmpty() && userRef != null) {
                        userRef.child("fullName").setValue(newName);
                        Toast.makeText(this, "Đã cập nhật họ tên!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
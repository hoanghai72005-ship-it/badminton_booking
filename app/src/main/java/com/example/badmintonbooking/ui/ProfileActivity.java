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
import com.example.badmintonbooking.ui.owner.ManageCourtsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileName, tvProfileEmail, tvProfileTrustScore;
    private MaterialCardView cardLichSu, cardEditProfile;
    private DatabaseReference userRef;
    private ValueEventListener userListener;
    private FirebaseUser currentUser;
    private LinearLayout layoutOwnerSection, layoutAdminSection;
    private MaterialCardView cardDuyetDon, cardQuanLySan, cardKiemDuyetBai;

    private String currentCourtId = "court_01";
    private String currentCourtName = "Sân Cầu Lông ĐH Công Nghiệp";

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

        if (cardQuanLySan != null) {
            cardQuanLySan.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, ManageCourtsActivity.class);
                intent.putExtra("COURT_ID", currentCourtId);
                intent.putExtra("COURT_NAME", currentCourtName);
                startActivity(intent);
            });
        }

        if (cardKiemDuyetBai != null) {
            cardKiemDuyetBai.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, MatchListActivity.class);
                intent.putExtra("IS_ADMIN", true);
                startActivity(intent);
            });
        }

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            tvProfileEmail.setText(currentUser.getEmail());
            userRef = FirebaseDatabase.getInstance("https://badminton-booking-9e3a2-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference("users").child(currentUser.getUid());
            loadCurrentUserData();
        } else {
            tvProfileName.setText("Khách");
            tvProfileEmail.setText("Chưa đăng nhập");
        }

        cardLichSu.setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, HistoryActivity.class)));
        cardEditProfile.setOnClickListener(v -> showEditNameDialog());

        findViewById(R.id.btnSignOut).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        if (bottomNav == null) return;

        bottomNav.setSelectedItemId(R.id.nav_profile);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_profile) {
                return true;
            }

            Intent intent = null;
            if (itemId == R.id.nav_home) {
                intent = new Intent(ProfileActivity.this, MainActivity.class);
            } else if (itemId == R.id.nav_booking) {
                intent = new Intent(ProfileActivity.this, MapActivity.class);
            } else if (itemId == R.id.nav_match) {
                intent = new Intent(ProfileActivity.this, MatchListActivity.class);
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }

    private void loadCurrentUserData() {
        if (userRef == null) return;

        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                String role = snapshot.child("role").getValue(String.class);

                if ("OWNER".equalsIgnoreCase(role)) {
                    String cName = snapshot.child("courtName").getValue(String.class);
                    if (cName == null || cName.isEmpty()) {
                        cName = snapshot.child("managedCourtName").getValue(String.class);
                    }
                    if (cName != null && !cName.isEmpty()) {
                        currentCourtName = cName;
                    }

                    String cId = snapshot.child("courtId").getValue(String.class);
                    if (cId != null && !cId.isEmpty()) {
                        currentCourtId = cId;
                    }

                    tvProfileTrustScore.setText("CHỦ SÂN: " + currentCourtName + " 🏆");
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

                // 2. Lấy họ tên
                String fullName = snapshot.child("fullName").getValue(String.class);
                if (fullName != null && !fullName.isEmpty()) {
                    tvProfileName.setText(fullName);
                } else {
                    String email = currentUser != null ? currentUser.getEmail() : null;
                    tvProfileName.setText(email != null ? email.split("@")[0] : "Người chơi");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        userRef.addValueEventListener(userListener);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userRef != null && userListener != null) {
            userRef.removeEventListener(userListener);
        }
    }
}
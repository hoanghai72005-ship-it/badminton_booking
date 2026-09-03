package com.example.badmintonbooking.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.badmintonbooking.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvGoToRegister;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private static final String PREF_NAME = "LOGIN_PREFS";
    private static final String KEY_LOGIN_TIME = "LAST_LOGIN_TIMESTAMP";
    private static final long SEVEN_DAYS_MS = 7L * 24 * 60 * 60 * 1000; // 7 ngày tính bằng mili-giây

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://badminton-booking-9e3a2-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();

        // 1. KIỂM TRA ĐĂNG NHẬP TRONG VÒNG 7 NGÀY
        FirebaseUser currentUser = mAuth.getCurrentUser();
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        long lastLoginTime = prefs.getLong(KEY_LOGIN_TIME, 0);
        long now = System.currentTimeMillis();

        if (currentUser != null && (now - lastLoginTime <= SEVEN_DAYS_MS)) {
            // Chưa quá 7 ngày -> Vào trực tiếp theo Role
            checkUserRoleAndRedirect(currentUser.getUid());
            return;
        } else if (currentUser != null) {
            // Quá 7 ngày -> Đăng xuất để yêu cầu đăng nhập lại
            mAuth.signOut();
        }

        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);

        tvGoToRegister.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
        btnLogin.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Đang xác thực...");

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            btnLogin.setEnabled(true);
            btnLogin.setText("ĐĂNG NHẬP");

            if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                // Lưu mốc thời gian đăng nhập hiện tại vào máy
                getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit()
                        .putLong(KEY_LOGIN_TIME, System.currentTimeMillis())
                        .apply();

                String uid = mAuth.getCurrentUser().getUid();
                checkUserRoleAndRedirect(uid);
            } else {
                Toast.makeText(LoginActivity.this, "Đăng nhập thất bại: " + (task.getException() != null ? task.getException().getMessage() : ""), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void checkUserRoleAndRedirect(String uid) {
        mDatabase.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Luôn vào MainActivity, phân quyền tính năng hiển thị động bên trong Hồ sơ
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        });
    }
}
package com.example.badmintonbooking.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.badmintonbooking.R;
import com.example.badmintonbooking.adapter.MatchPostAdapter;
import com.example.badmintonbooking.model.MatchPost;
import com.example.badmintonbooking.post.AppDatabase;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MatchListActivity extends AppCompatActivity {

    private RecyclerView rvMatchPosts;
    private MatchPostAdapter adapter;
    private List<MatchPost> allPostsList = new ArrayList<>();
    private boolean isAdmin = false;
    private TextView tvListTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_list);

        tvListTitle = findViewById(R.id.tv_list_title);
        rvMatchPosts = findViewById(R.id.rv_match_posts);
        rvMatchPosts.setLayoutManager(new LinearLayoutManager(this));

        // 1. Kiểm tra quyền Admin từ Intent (khi bấm từ mục Kiểm duyệt trong Hồ sơ)
        isAdmin = getIntent().getBooleanExtra("IS_ADMIN", false);
        if (isAdmin && tvListTitle != null) {
            tvListTitle.setText("KIỂM DUYỆT BÀI ĐĂNG 🛡️");
        }

        // 2. Khởi tạo adapter truyền cờ isAdmin
        adapter = new MatchPostAdapter(this, allPostsList, allPostsList, isAdmin);
        rvMatchPosts.setAdapter(adapter);

        // 3. Đồng thời kiểm tra lại role ADMIN trực tiếp từ Firebase
        checkAdminFromFirebase();

        // Nút "+ Tạo bài"
        MaterialButton btnGoCreate = findViewById(R.id.btn_go_create_match);
        if (btnGoCreate != null) {
            btnGoCreate.setOnClickListener(v -> {
                startActivity(new Intent(MatchListActivity.this, CreateMatchActivity.class));
            });
        }

        // Thanh Bottom Navigation điều hướng chống lag RAM
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_match);
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_match) return true;

                Intent intent = null;
                if (id == R.id.nav_home) {
                    intent = new Intent(this, MainActivity.class);
                } else if (id == R.id.nav_booking) {
                    intent = new Intent(this, MapActivity.class);
                } else if (id == R.id.nav_profile) {
                    intent = new Intent(this, ProfileActivity.class);
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPostsFromRoom();
    }

    public void loadPostsFromRoom() {
        allPostsList.clear();
        List<MatchPost> posts = AppDatabase.getDatabase(this).matchPostDao().getAllPosts();
        if (posts != null) {
            allPostsList.addAll(posts);
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void checkAdminFromFirebase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance("https://badminton-booking-9e3a2-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference("users").child(currentUser.getUid());

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String role = snapshot.child("role").getValue(String.class);
                        if ("ADMIN".equalsIgnoreCase(role)) {
                            isAdmin = true;
                            if (tvListTitle != null) {
                                tvListTitle.setText("KIỂM DUYỆT BÀI ĐĂNG 🛡️");
                            }
                            if (adapter != null) {
                                adapter.setAdmin(true);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }
}
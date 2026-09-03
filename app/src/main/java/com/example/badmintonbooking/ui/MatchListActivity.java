package com.example.badmintonbooking.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.badmintonbooking.R;
import com.example.badmintonbooking.adapter.MatchPostAdapter;
import com.example.badmintonbooking.model.MatchPost;
import com.example.badmintonbooking.post.AppDatabase;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class MatchListActivity extends AppCompatActivity {

    private RecyclerView rvMatchPosts;
    private MatchPostAdapter adapter;
    private List<MatchPost> allPostsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_list);

        rvMatchPosts = findViewById(R.id.rv_match_posts);
        rvMatchPosts.setLayoutManager(new LinearLayoutManager(this));

        // Nạp toàn bộ danh sách để cuộn dạng List tự nhiên
        adapter = new MatchPostAdapter(this, allPostsList, allPostsList);
        rvMatchPosts.setAdapter(adapter);

        // Nút "+ Tạo bài" mở màn hình tạo bài ghép mới
        MaterialButton btnGoCreate = findViewById(R.id.btn_go_create_match);
        if (btnGoCreate != null) {
            btnGoCreate.setOnClickListener(v -> {
                startActivity(new Intent(MatchListActivity.this, CreateMatchActivity.class));
            });
        }

        // Thanh điều hướng đáy Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_match);
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    startActivity(new Intent(this, MainActivity.class));
                    finish(); overridePendingTransition(0, 0); return true;
                } else if (id == R.id.nav_booking) {
                    startActivity(new Intent(this, MapActivity.class));
                    finish(); overridePendingTransition(0, 0); return true;
                } else if (id == R.id.nav_profile) {
                    startActivity(new Intent(this, ProfileActivity.class));
                    finish(); overridePendingTransition(0, 0); return true;
                }
                return id == R.id.nav_match;
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
}
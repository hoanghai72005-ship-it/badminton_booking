package com.example.badmintonbooking.ui; // Đổi lại theo đúng tên package của bạn nếu cần

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

public class HomeFragment extends Fragment {

    private RecyclerView rvCourts;
    private CourtAdapter courtAdapter;
    private List<Court> courtList;

    // Khai báo biến Firebase
    private DatabaseReference courtsRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Ánh xạ file giao diện XML vào Fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 1. Khởi tạo RecyclerView và Danh sách
        rvCourts = view.findViewById(R.id.rvCourts);
        rvCourts.setHasFixedSize(true);
        // Thiết lập danh sách dạng cuộn dọc
        rvCourts.setLayoutManager(new LinearLayoutManager(getContext()));

        courtList = new ArrayList<>();
        courtAdapter = new CourtAdapter(getContext(), courtList);
        rvCourts.setAdapter(courtAdapter);

        // 2. Kết nối tới nhánh "courts" trên Firebase Realtime Database
        courtsRef = FirebaseDatabase.getInstance().getReference("courts");

        // 3. Tải dữ liệu
        loadCourtsFromFirebase();

        return view;
    }

    private void loadCourtsFromFirebase() {
        // ValueEventListener sẽ tự động lắng nghe và tải lại dữ liệu mỗi khi trên Firebase có sự thay đổi (Thêm/Sửa/Xóa sân)
        courtsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Xóa danh sách cũ trước khi tải mới để tránh bị trùng lặp dữ liệu
                courtList.clear();

                // Vòng lặp quét qua từng sân trong nhánh "courts"
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    // Ép kiểu dữ liệu từ Firebase về đúng khuôn đúc Court.java
                    Court court = dataSnapshot.getValue(Court.class);
                    if (court != null) {
                        courtList.add(court);
                    }
                }

                // Báo cho Adapter biết dữ liệu đã thay đổi để vẽ lại lên màn hình
                courtAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý khi có lỗi mạng hoặc lỗi quyền truy cập
                Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
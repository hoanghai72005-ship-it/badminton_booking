package com.example.badmintonbooking.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.badmintonbooking.R;
import com.example.badmintonbooking.adapter.MatchAdapter;
import com.example.badmintonbooking.data.MatchModel;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;

public class MatchListFragment extends Fragment {

    private RecyclerView rvMatchList;
    private MatchAdapter adapter;
    private ArrayList<MatchModel> matchList;
    private ExtendedFloatingActionButton btnCreateFab;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_match_list, container, false);

        rvMatchList = view.findViewById(R.id.rv_match_list);
        btnCreateFab = view.findViewById(R.id.btn_create_match_fab);

        initMockData();

        adapter = new MatchAdapter(matchList);
        rvMatchList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMatchList.setAdapter(adapter);

        btnCreateFab.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreateMatchActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void initMockData() {
        matchList = new ArrayList<>();
        matchList.add(new MatchModel("1", "Nguyễn Văn Anh", "Sân Cầu Lông Pro Badminton - Sân 2", "18:00 - 20:00 (Hôm nay)", "Trung bình", 3, 4, "Chia đều tiền sân + cầu, giao lưu vui vẻ.", false));
        matchList.add(new MatchModel("2", "Trần Văn Minh", "Sân Cầu Lông Star Arena - Sân 1", "19:00 - 21:00 (Tối nay)", "Khá / Tốt", 2, 4, "Tuyển tay cứng đánh đôi nam nữ.", false));
        matchList.add(new MatchModel("3", "Nguyễn Văn Giang", "Sân Cầu Lông Smash Club - Sân 4", "17:00 - 19:00 (Ngày mai)", "Mới chơi", 1, 4, "Tuyển thêm bạn nữ giao lưu học hỏi.", false));
    }
}
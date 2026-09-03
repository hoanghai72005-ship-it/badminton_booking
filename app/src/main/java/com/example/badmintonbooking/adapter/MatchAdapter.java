package com.example.badmintonbooking.adapter;

import com.example.badmintonbooking.R;
import com.example.badmintonbooking.data.MatchModel;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.MatchViewHolder> {

    private ArrayList<MatchModel> matchList;

    public MatchAdapter(ArrayList<MatchModel> matchList) {
        this.matchList = matchList;
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_match, parent, false);
        return new MatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        MatchModel match = matchList.get(position);

        holder.tvHostName.setText(match.getHostName() + " (Chủ kèo)");
        holder.tvLevel.setText("🎯 Trình độ: " + match.getLevel());
        holder.tvTimeAndCourt.setText("⏰ " + match.getMatchTime() + "\n📍 " + match.getCourtName());
        holder.tvNote.setText("Ghi chú: " + match.getNote());

        updateSlotAndButtonState(holder, match);

        holder.btnJoinMatch.setOnClickListener(v -> {
            if (match.isJoined()) {
                match.setJoined(false);
                match.setCurrentSlots(match.getCurrentSlots() - 1);
                Toast.makeText(v.getContext(), "Đã hủy tham gia kèo!", Toast.LENGTH_SHORT).show();
            } else {
                if (match.getCurrentSlots() < match.getMaxSlots()) {
                    match.setJoined(true);
                    match.setCurrentSlots(match.getCurrentSlots() + 1);
                    Toast.makeText(v.getContext(), "Đăng ký tham gia thành công!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(v.getContext(), "Kèo này đã đủ người!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            updateSlotAndButtonState(holder, match);
        });
    }

    private void updateSlotAndButtonState(MatchViewHolder holder, MatchModel match) {
        int needed = match.getMaxSlots() - match.getCurrentSlots();
        if (needed > 0) {
            holder.tvSlotStatus.setText("Cần tuyển " + needed + "/" + match.getMaxSlots());
            holder.tvSlotStatus.setTextColor(Color.parseColor("#F97316"));
        } else {
            holder.tvSlotStatus.setText("Đã đủ người");
            holder.tvSlotStatus.setTextColor(Color.parseColor("#16A34A"));
        }

        if (match.isJoined()) {
            holder.btnJoinMatch.setText("HỦY THAM GIA");
            holder.btnJoinMatch.setBackgroundColor(Color.parseColor("#EF4444"));
        } else {
            holder.btnJoinMatch.setText("ĐĂNG KÝ THAM GIA");
            holder.btnJoinMatch.setBackgroundColor(Color.parseColor("#16A34A"));
        }
    }

    @Override
    public int getItemCount() {
        return matchList != null ? matchList.size() : 0;
    }

    public static class MatchViewHolder extends RecyclerView.ViewHolder {
        TextView tvHostName, tvSlotStatus, tvLevel, tvTimeAndCourt, tvNote;
        MaterialButton btnJoinMatch;

        public MatchViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHostName = itemView.findViewById(R.id.tv_host_name);
            tvSlotStatus = itemView.findViewById(R.id.tv_slot_status);
            tvLevel = itemView.findViewById(R.id.tv_level);
            tvTimeAndCourt = itemView.findViewById(R.id.tv_time_and_court);
            tvNote = itemView.findViewById(R.id.tv_note);
            btnJoinMatch = itemView.findViewById(R.id.btn_join_match);
        }
    }
}
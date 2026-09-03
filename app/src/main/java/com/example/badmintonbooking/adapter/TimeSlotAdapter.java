package com.example.badmintonbooking.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.badmintonbooking.R;
import com.example.badmintonbooking.data.TimeSlot;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder> {

    // 1. KHAI BÁO INTERFACE NGAY TẠI ĐÂY (Bên trong class Adapter, bên ngoài ViewHolder)
    public interface OnSlotClickListener {
        void onSlotClicked();
    }

    private Context context;
    private List<TimeSlot> timeSlotList;
    private OnSlotClickListener listener;

    // 2. CONSTRUCTOR NHẬN LISTENER
    public TimeSlotAdapter(Context context, List<TimeSlot> timeSlotList, OnSlotClickListener listener) {
        this.context = context;
        this.timeSlotList = timeSlotList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TimeSlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_time_slot, parent, false);
        return new TimeSlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeSlotViewHolder holder, int position) {
        TimeSlot slot = timeSlotList.get(position);
        if (slot == null) return;

        holder.tvTime.setText((slot.getStartTime() != null ? slot.getStartTime() : "00:00")
                + " - " + (slot.getEndTime() != null ? slot.getEndTime() : "00:00"));

        String status = slot.getStatus() != null ? slot.getStatus() : "AVAILABLE";

        switch (status) {
            case "BOOKED":
            case "HELD":
                holder.cardView.setCardBackgroundColor(Color.parseColor("#F1F5F9"));
                holder.cardView.setStrokeWidth(0);
                holder.tvTime.setTextColor(Color.parseColor("#94A3B8"));
                holder.tvStatus.setText("Đã đặt");
                holder.tvStatus.setTextColor(Color.parseColor("#94A3B8"));
                holder.itemView.setEnabled(false);
                break;

            case "SELECTED":
                holder.cardView.setCardBackgroundColor(Color.parseColor("#059669"));
                holder.cardView.setStrokeWidth(0);
                holder.tvTime.setTextColor(Color.parseColor("#FFFFFF"));
                holder.tvStatus.setText("Đang chọn");
                holder.tvStatus.setTextColor(Color.parseColor("#FFFFFF"));
                holder.itemView.setEnabled(true);
                break;

            case "AVAILABLE":
            default:
                holder.cardView.setCardBackgroundColor(Color.parseColor("#DCFCE7"));
                holder.cardView.setStrokeColor(Color.parseColor("#16A34A"));
                holder.cardView.setStrokeWidth(2);
                holder.tvTime.setTextColor(Color.parseColor("#0F172A"));
                holder.tvStatus.setText("Còn trống");
                holder.tvStatus.setTextColor(Color.parseColor("#16A34A"));
                holder.itemView.setEnabled(true);
                break;
        }

        holder.itemView.setOnClickListener(v -> {
            String currentSt = slot.getStatus() != null ? slot.getStatus() : "AVAILABLE";
            if ("AVAILABLE".equals(currentSt)) {
                slot.setStatus("SELECTED");
            } else if ("SELECTED".equals(currentSt)) {
                slot.setStatus("AVAILABLE");
            }
            notifyItemChanged(position);
            if (listener != null) {
                listener.onSlotClicked();
            }
        });
    }

    @Override
    public int getItemCount() {
        return timeSlotList.size();
    }

    public static class TimeSlotViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView tvTime, tvStatus;

        public TimeSlotViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvStatus); // Thêm dòng này
        }
    }
}
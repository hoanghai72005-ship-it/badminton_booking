package com.example.badmintonbooking.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.badmintonbooking.R;
import com.example.badmintonbooking.data.TimeSlot;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder> {

    public interface OnSlotClickListener {
        void onSlotClicked();
    }

    private Context context;
    private List<TimeSlot> timeSlotList;
    private OnSlotClickListener listener;

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

        String startTime = slot.getStartTime() != null ? slot.getStartTime() : "00:00";
        String endTime = slot.getEndTime() != null ? slot.getEndTime() : "00:00";
        holder.tvTime.setText(startTime + " - " + endTime);

        String status = slot.getStatus() != null ? slot.getStatus() : "AVAILABLE";

        // 1. KIỂM TRA KHUNG GIỜ ĐÃ QUA SO VỚI GIỜ THỰC TẾ
        boolean isPast = isSlotInThePast(slot.getDate(), startTime);

        if (isPast) {
            // Khung giờ đã qua giờ thực tế -> Khóa thẻ, đổi màu xám
            holder.cardView.setCardBackgroundColor(Color.parseColor("#F1F5F9"));
            holder.cardView.setStrokeWidth(0);
            holder.tvTime.setTextColor(Color.parseColor("#94A3B8"));
            holder.tvStatus.setText("Đã qua");
            holder.tvStatus.setTextColor(Color.parseColor("#94A3B8"));
            holder.itemView.setEnabled(false);
            holder.itemView.setOnClickListener(v -> {
                Toast.makeText(context, "Khung giờ này đã qua, vui lòng chọn khung giờ khác!", Toast.LENGTH_SHORT).show();
            });
            return;
        }

        // 2. NẾU CHƯA QUA GIỜ -> HIỂN THỊ THEO TRẠNG THÁI BOOKED / SELECTED / AVAILABLE
        holder.itemView.setEnabled(true);

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

    // Hàm so sánh với ngày và giờ thực tế của thiết bị
    private boolean isSlotInThePast(String slotDate, String startTime) {
        try {
            if (slotDate == null || startTime == null) return false;

            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String todayStr = sdfDate.format(new Date());

            // 1. So sánh ngày
            int dateCompare = slotDate.compareTo(todayStr);
            if (dateCompare < 0) {
                return true; // Ngày quá khứ -> Khóa
            } else if (dateCompare > 0) {
                return false; // Ngày tương lai -> Cho phép đặt
            }

            // 2. Nếu là ngày HÔM NAY -> So sánh giờ và phút thực tế
            Calendar now = Calendar.getInstance();
            int currentHour = now.get(Calendar.HOUR_OF_DAY);
            int currentMinute = now.get(Calendar.MINUTE);
            int currentTotalMinutes = currentHour * 60 + currentMinute;

            // 3. Tách chuỗi lấy giờ và phút chính xác bằng substring
            if (startTime.contains(":")) {
                int colonIndex = startTime.indexOf(":");
                String hourStr = startTime.substring(0, colonIndex).trim();
                String minuteStr = startTime.substring(colonIndex + 1).trim();

                int slotHour = Integer.parseInt(hourStr);
                int slotMinute = Integer.parseInt(minuteStr);
                int slotTotalMinutes = slotHour * 60 + slotMinute;

                return currentTotalMinutes >= slotTotalMinutes;
            }
        } catch (Exception ignored) {}
        return false;
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
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
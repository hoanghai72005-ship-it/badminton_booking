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
import com.example.badmintonbooking.data.Booking;
import java.text.DecimalFormat;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    public interface OnHistoryClickListener {
        void onBookingClick(Booking booking);
    }

    private Context context;
    private List<Booking> bookingList;
    private OnHistoryClickListener listener;

    public HistoryAdapter(Context context, List<Booking> bookingList, OnHistoryClickListener listener) {
        this.context = context;
        this.bookingList = bookingList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        Booking booking = bookingList.get(position);
        if (booking == null) return;

        if (holder.tvHistoryCourtId != null) {
            holder.tvHistoryCourtId.setText(booking.getCourtId() != null ? booking.getCourtId() : "Sân Cầu Lông ĐH Công Nghiệp");
        }

        if (holder.tvHistoryTotal != null) {
            DecimalFormat formatter = new DecimalFormat("###,###,###");
            holder.tvHistoryTotal.setText("Tổng tiền: " + formatter.format(booking.getTotalPrice()) + "đ");
        }

        // Gán Badge màu theo 4 trạng thái
        String st = booking.getStatus() != null ? booking.getStatus() : "CONFIRMED";
        if (holder.tvHistoryStatus != null) {
            if ("CANCELLED".equalsIgnoreCase(st)) {
                holder.tvHistoryStatus.setText("Đã hủy");
                holder.tvHistoryStatus.setTextColor(Color.parseColor("#EF4444"));
                holder.tvHistoryStatus.setBackgroundColor(Color.parseColor("#FEE2E2"));
            } else if ("COMPLETED".equalsIgnoreCase(st)) {
                holder.tvHistoryStatus.setText("Đã hoàn thành");
                holder.tvHistoryStatus.setTextColor(Color.parseColor("#0284C7"));
                holder.tvHistoryStatus.setBackgroundColor(Color.parseColor("#E0F2FE"));
            } else if ("PENDING".equalsIgnoreCase(st)) {
                holder.tvHistoryStatus.setText("Chờ duyệt");
                holder.tvHistoryStatus.setTextColor(Color.parseColor("#D97706"));
                holder.tvHistoryStatus.setBackgroundColor(Color.parseColor("#FEF3C7"));
            } else {
                holder.tvHistoryStatus.setText("Đã xác nhận");
                holder.tvHistoryStatus.setTextColor(Color.parseColor("#16A34A"));
                holder.tvHistoryStatus.setBackgroundColor(Color.parseColor("#DCFCE7"));
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookingClick(booking);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookingList != null ? bookingList.size() : 0;
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvHistoryCourtId, tvHistoryTimes, tvHistoryTotal, tvHistoryStatus;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHistoryCourtId = itemView.findViewById(R.id.tvHistoryCourtId);
            tvHistoryTimes = itemView.findViewById(R.id.tvHistoryTimes);
            tvHistoryTotal = itemView.findViewById(R.id.tvHistoryTotal);
            tvHistoryStatus = itemView.findViewById(R.id.tvHistoryStatus);
        }
    }
}
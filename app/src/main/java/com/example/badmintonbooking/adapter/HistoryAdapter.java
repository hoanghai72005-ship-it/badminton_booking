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

        // 1. Gán Tên cụm sân & Sân con
        if (holder.tvHistoryCourtId != null) {
            holder.tvHistoryCourtId.setText(booking.getCourtId() != null ? booking.getCourtId() : "Sân Cầu Lông");
        }

        // 2. Gán Giá tiền (Định dạng kiểu 160.000đ)
        if (holder.tvHistoryTotal != null) {
            DecimalFormat formatter = new DecimalFormat("###,###,###");
            holder.tvHistoryTotal.setText("Tổng tiền: " + formatter.format(booking.getTotalPrice()) + "đ");
        }

        // 3. Gán Trạng thái đơn (Đổi màu badge xanh lá hoặc đỏ nếu bị hủy)
        String st = booking.getStatus() != null ? booking.getStatus() : "CONFIRMED";
        if (holder.tvHistoryStatus != null) {
            if ("CANCELLED".equalsIgnoreCase(st)) {
                holder.tvHistoryStatus.setText("Đã hủy");
                holder.tvHistoryStatus.setTextColor(Color.parseColor("#EF4444"));
                holder.tvHistoryStatus.setBackgroundColor(Color.parseColor("#FEE2E2"));
            } else {
                holder.tvHistoryStatus.setText("Đã xác nhận");
                holder.tvHistoryStatus.setTextColor(Color.parseColor("#16A34A"));
                holder.tvHistoryStatus.setBackgroundColor(Color.parseColor("#DCFCE7"));
            }
        }

        // 4. Bắt sự kiện click vào thẻ để mở trang chi tiết đơn
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
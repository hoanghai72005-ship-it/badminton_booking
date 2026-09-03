package com.example.badmintonbooking.ui.owner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.badmintonbooking.R;

public class OwnerDashboardActivity extends AppCompatActivity {

    // Khai báo các thành phần giao diện mới
    private LinearLayout btnActionAddCourt, btnActionManageSchedule, btnActionScanQr;
    private TextView tvTotalBookings, tvTotalRevenue, tvViewAllBookings;
    private RecyclerView rvUpcomingBookings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_dashboard);

        // 1. Ánh xạ dữ liệu
        tvTotalBookings = findViewById(R.id.tv_total_bookings);
        tvTotalRevenue = findViewById(R.id.tv_total_revenue);
        btnActionAddCourt = findViewById(R.id.btn_action_add_court);
        btnActionManageSchedule = findViewById(R.id.btn_action_manage_schedule);
        btnActionScanQr = findViewById(R.id.btn_action_scan_qr);
        tvViewAllBookings = findViewById(R.id.tv_view_all_bookings);
        rvUpcomingBookings = findViewById(R.id.rv_upcoming_bookings);

        // 2. Thiết lập sự kiện chuyển trang

        // Mở trang Quản lý Sân
        btnActionAddCourt.setOnClickListener(v -> {
            Intent intent = new Intent(OwnerDashboardActivity.this, ManageCourtsActivity.class);
            startActivity(intent);
        });

        // Mở trang Đóng/Mở Lịch (Khung giờ)
        btnActionManageSchedule.setOnClickListener(v -> {
            Intent intent = new Intent(OwnerDashboardActivity.this, ManageTimeSlotsActivity.class);
            startActivity(intent);
        });

        // Tính năng quét QR (Dự phòng cho tương lai)
        btnActionScanQr.setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng Quét QR Check-in đang được phát triển!", Toast.LENGTH_SHORT).show();
        });

        // Nút xem tất cả đơn đặt sân
        tvViewAllBookings.setOnClickListener(v -> {
            Toast.makeText(this, "Chuyển đến danh sách toàn bộ đơn hàng...", Toast.LENGTH_SHORT).show();
            // Tương lai bạn có thể Intent sang HistoryActivity ở đây
        });
    }
}
package com.example.badmintonbooking.ui.owner;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.badmintonbooking.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ManageTimeSlotsActivity extends AppCompatActivity {

    private EditText etSubCourtId, etTimeSlot;
    private Switch switchLockStatus;
    private Button btnUpdateStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Lưu ý: Bạn sẽ cần tạo file activity_manage_time_slots.xml cho giao diện này
        setContentView(R.layout.activity_manage_time_slots);

        etSubCourtId = findViewById(R.id.etSubCourtId);
        etTimeSlot = findViewById(R.id.etTimeSlot);
        switchLockStatus = findViewById(R.id.switchLockStatus);
        btnUpdateStatus = findViewById(R.id.btnUpdateStatus);

        btnUpdateStatus.setOnClickListener(v -> {
            String subCourtId = etSubCourtId.getText().toString().trim();
            String timeSlot = etTimeSlot.getText().toString().trim();
            boolean isLocked = switchLockStatus.isChecked();

            if (subCourtId.isEmpty() || timeSlot.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mã sân và khung giờ!", Toast.LENGTH_SHORT).show();
                return;
            }
            toggleTimeSlotStatus(subCourtId, timeSlot, isLocked);
        });
    }

    // Logic khóa/mở khung giờ linh hoạt trên Firebase
    private void toggleTimeSlotStatus(String subCourtId, String timeSlot, boolean isLocked) {
        DatabaseReference slotRef = FirebaseDatabase.getInstance()
                .getReference("TimeSlots")
                .child(subCourtId)
                .child(timeSlot);

        slotRef.child("isLocked").setValue(isLocked)
                .addOnSuccessListener(aVoid -> {
                    String msg = isLocked ? "Đã khóa khung giờ: " + timeSlot : "Đã mở khung giờ: " + timeSlot;
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
package com.example.badmintonbooking.ui.owner;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.badmintonbooking.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class ManageCourtsActivity extends AppCompatActivity {

    private EditText etCourtName, etComplexId;
    private Button btnAddCourt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Lưu ý: Bạn sẽ cần tạo file activity_manage_courts.xml cho giao diện này
        setContentView(R.layout.activity_manage_courts);

        etCourtName = findViewById(R.id.etCourtName);
        etComplexId = findViewById(R.id.etComplexId);
        btnAddCourt = findViewById(R.id.btnAddCourt);

        btnAddCourt.setOnClickListener(v -> {
            String complexId = etComplexId.getText().toString().trim();
            String courtName = etCourtName.getText().toString().trim();

            if (complexId.isEmpty() || courtName.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }
            addSubCourt(complexId, courtName);
        });
    }

    // Logic tạo Sân con mới gắn với Cụm sân đẩy lên Firebase
    private void addSubCourt(String complexId, String subCourtName) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("SubCourts");
        String subCourtId = ref.push().getKey();

        if (subCourtId != null) {
            Map<String, Object> courtData = new HashMap<>();
            courtData.put("complexId", complexId);
            courtData.put("name", subCourtName);
            courtData.put("isActive", true);

            ref.child(subCourtId).setValue(courtData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Thêm sân thành công!", Toast.LENGTH_SHORT).show();
                        etCourtName.setText(""); // Xóa trắng ô nhập
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}
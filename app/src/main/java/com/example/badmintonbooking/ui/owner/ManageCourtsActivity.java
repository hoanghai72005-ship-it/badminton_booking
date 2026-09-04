package com.example.badmintonbooking.ui.owner;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.badmintonbooking.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.HashMap;
import java.util.Map;

public class ManageCourtsActivity extends AppCompatActivity {

    private EditText etCourtName, etCourtAddress, etCourtCount, etCourtPrice;
    private MaterialButton btnSaveCourtInfo;
    private DatabaseReference courtRef, userRef;
    private String courtId = "court_01";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_courts);

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        etCourtName = findViewById(R.id.etCourtName);
        etCourtAddress = findViewById(R.id.etCourtAddress);
        etCourtCount = findViewById(R.id.etCourtCount);
        etCourtPrice = findViewById(R.id.etCourtPrice);
        btnSaveCourtInfo = findViewById(R.id.btnSaveCourtInfo);

        String passedCourtId = getIntent().getStringExtra("COURT_ID");
        if (passedCourtId != null && !passedCourtId.isEmpty()) {
            courtId = passedCourtId;
        }

        courtRef = FirebaseDatabase.getInstance("https://badminton-booking-9e3a2-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("courts").child(courtId);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userRef = FirebaseDatabase.getInstance("https://badminton-booking-9e3a2-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        }

        loadCourtInfo();

        btnSaveCourtInfo.setOnClickListener(v -> saveCourtInfo());
    }

    private void loadCourtInfo() {
        if (userRef != null) {
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot userSnap) {
                    if (userSnap.hasChild("courtName")) {
                        etCourtName.setText(userSnap.child("courtName").getValue(String.class));
                        etCourtAddress.setText(userSnap.child("courtAddress").getValue(String.class));
                        etCourtCount.setText(String.valueOf(userSnap.child("totalCourts").getValue()));
                        etCourtPrice.setText(String.valueOf(userSnap.child("courtPrice").getValue()));
                    } else {
                        loadFromCourtsNode();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    loadFromCourtsNode();
                }
            });
        } else {
            loadFromCourtsNode();
        }
    }

    private void loadFromCourtsNode() {
        courtRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("courtName").getValue(String.class);
                    String address = snapshot.child("address").getValue(String.class);
                    Object countObj = snapshot.child("totalCourts").getValue();
                    Object priceObj = snapshot.child("price").getValue();

                    if (name != null) etCourtName.setText(name);
                    if (address != null) etCourtAddress.setText(address);
                    if (countObj != null) etCourtCount.setText(String.valueOf(countObj));
                    if (priceObj != null) etCourtPrice.setText(String.valueOf(priceObj));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void saveCourtInfo() {
        String name = etCourtName.getText().toString().trim();
        String address = etCourtAddress.getText().toString().trim();
        String countStr = etCourtCount.getText().toString().trim();
        String priceStr = etCourtPrice.getText().toString().trim();

        if (name.isEmpty() || address.isEmpty() || countStr.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ cả 4 thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        int count;
        double price;
        try {
            count = Integer.parseInt(countStr);
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số sân và giá tiền phải là chữ số hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSaveCourtInfo.setEnabled(false);
        btnSaveCourtInfo.setText("Đang lưu...");

        Map<String, Object> courtMap = new HashMap<>();
        courtMap.put("courtId", courtId);
        courtMap.put("courtName", name);
        courtMap.put("address", address);
        courtMap.put("totalCourts", count);
        courtMap.put("price", price);

        courtRef.updateChildren(courtMap);

        if (userRef != null) {
            Map<String, Object> userCourtMap = new HashMap<>();
            userCourtMap.put("courtId", courtId);
            userCourtMap.put("courtName", name);
            userCourtMap.put("courtAddress", address);
            userCourtMap.put("totalCourts", count);
            userCourtMap.put("courtPrice", price);
            userRef.updateChildren(userCourtMap);
        }

        btnSaveCourtInfo.setEnabled(true);
        btnSaveCourtInfo.setText("LƯU THAY ĐỔI");
        Toast.makeText(this, "🎉 Cập nhật thông tin sân & bảng giá thành công!", Toast.LENGTH_SHORT).show();
    }
}
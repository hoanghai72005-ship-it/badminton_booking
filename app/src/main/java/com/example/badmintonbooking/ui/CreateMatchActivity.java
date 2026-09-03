package com.example.badmintonbooking.ui;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.badmintonbooking.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class CreateMatchActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextInputEditText edtCourtName, edtMatchTime, edtRequiredLevel, edtNeededSlots, edtMatchNote;
    private MaterialButton btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_match);

        btnBack = findViewById(R.id.btn_create_back);
        edtCourtName = findViewById(R.id.edt_court_name);
        edtMatchTime = findViewById(R.id.edt_match_time);
        edtRequiredLevel = findViewById(R.id.edt_required_level);
        edtNeededSlots = findViewById(R.id.edt_needed_slots);
        edtMatchNote = findViewById(R.id.edt_match_note);
        btnSubmit = findViewById(R.id.btn_submit_match);

        btnBack.setOnClickListener(v -> finish());

        btnSubmit.setOnClickListener(v -> {
            String court = edtCourtName.getText().toString().trim();
            String time = edtMatchTime.getText().toString().trim();
            String level = edtRequiredLevel.getText().toString().trim();
            String slots = edtNeededSlots.getText().toString().trim();

            if (court.isEmpty() || time.isEmpty() || level.isEmpty() || slots.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin bắt buộc!", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "Đăng bài tìm đối thủ thành công!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
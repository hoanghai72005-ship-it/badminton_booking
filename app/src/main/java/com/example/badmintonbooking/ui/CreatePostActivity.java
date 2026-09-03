package com.example.badmintonbooking.ui; // ĐÃ ĐỔI TÊN PACKAGE

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

// CÁC ĐƯỜNG DẪN IMPORT ĐÃ ĐƯỢC CẬP NHẬT
import com.example.badmintonbooking.R;
import com.example.badmintonbooking.model.MatchPost;
import com.example.badmintonbooking.post.AppDatabase;

public class CreatePostActivity extends AppCompatActivity {

    private TextInputEditText edtCourtName, edtDate, edtTimeSlot, edtLevel, edtPrice;
    private MaterialButton btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        edtCourtName = findViewById(R.id.edt_court_name);
        edtDate = findViewById(R.id.edt_date);
        edtTimeSlot = findViewById(R.id.edt_time_slot);
        edtLevel = findViewById(R.id.edt_level);
        edtPrice = findViewById(R.id.edt_price);
        btnSubmit = findViewById(R.id.btn_submit_post);

        btnSubmit.setOnClickListener(v -> createPost());
    }

    private void createPost() {
        String court = edtCourtName.getText().toString().trim();
        String date = edtDate.getText().toString().trim();
        String time = edtTimeSlot.getText().toString().trim();
        String level = edtLevel.getText().toString().trim();
        String price = edtPrice.getText().toString().trim();

        if (court.isEmpty() || date.isEmpty() || time.isEmpty() || level.isEmpty() || price.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        MatchPost newPost = new MatchPost(
                "POST_" + System.currentTimeMillis(),
                "Tôi (Chủ kèo)",
                court,
                date,
                time,
                level,
                price,
                4,
                1,
                true
        );

        AppDatabase.getDatabase(this).matchPostDao().insertPost(newPost);

        Toast.makeText(this, "Đăng bài ghép kèo thành công!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
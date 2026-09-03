package com.example.badmintonbooking.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.badmintonbooking.R;

import java.util.Calendar;

public class FilterActivity extends AppCompatActivity {

    private Button btnBack;
    private Button btnChonNgay;
    private Button btnApDung;

    private RadioGroup radioStatus;

    private String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_filter);

        btnBack = findViewById(R.id.btnBack);
        btnChonNgay = findViewById(R.id.btnChonNgay);
        btnApDung = findViewById(R.id.btnApDung);

        radioStatus = findViewById(R.id.radioStatus);

        // QUAY LẠI

        btnBack.setOnClickListener(v -> {
            finish();
        });

        // CHỌN NGÀY

        btnChonNgay.setOnClickListener(v -> {
            showDatePicker();
        });

        // ÁP DỤNG

        btnApDung.setOnClickListener(v -> {

            int checkedId =
                    radioStatus.getCheckedRadioButtonId();

            String status;

            if (checkedId == R.id.radioPending) {

                status = "Chờ xác nhận";

            } else if (checkedId == R.id.radioConfirmed) {

                status = "Đã xác nhận";

            } else if (checkedId == R.id.radioPlaying) {

                status = "Đang diễn ra";

            } else if (checkedId == R.id.radioCompleted) {

                status = "Hoàn thành";

            } else if (checkedId == R.id.radioCancelled) {

                status = "Đã hủy";

            } else {

                status = "Tất cả";
            }

            Toast.makeText(
                    FilterActivity.this,
                    "Ngày: " + selectedDate +
                            "\nTrạng thái: " + status,
                    Toast.LENGTH_LONG
            ).show();

            finish();
        });
    }

    private void showDatePicker() {

        Calendar calendar =
                Calendar.getInstance();

        int year =
                calendar.get(Calendar.YEAR);

        int month =
                calendar.get(Calendar.MONTH);

        int day =
                calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog =
                new DatePickerDialog(
                        this,

                        (view,
                         selectedYear,
                         selectedMonth,
                         selectedDay) -> {

                            selectedDate =
                                    selectedDay +
                                            "/" +
                                            (selectedMonth + 1) +
                                            "/" +
                                            selectedYear;

                            btnChonNgay.setText(
                                    "📅  " + selectedDate
                            );
                        },

                        year,
                        month,
                        day
                );

        dialog.show();
    }
}
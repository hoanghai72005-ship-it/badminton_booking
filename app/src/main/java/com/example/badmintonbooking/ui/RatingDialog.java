package com.example.badmintonbooking.ui;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.badmintonbooking.R;
import com.google.android.material.button.MaterialButton;

public class RatingDialog extends DialogFragment {

    private String courtName;
    private RatingListener listener;

    public interface RatingListener {
        void onSubmitRating(float rating, String comment);
    }

    public RatingDialog(String courtName, RatingListener listener) {
        this.courtName = courtName;
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_rating, container, false);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvCourtName = view.findViewById(R.id.tv_court_name_rating);
        RatingBar ratingBar = view.findViewById(R.id.rating_bar);
        EditText edtComment = view.findViewById(R.id.edt_review_comment);
        MaterialButton btnCancel = view.findViewById(R.id.btn_cancel_rating);
        MaterialButton btnSubmit = view.findViewById(R.id.btn_submit_rating);

        if (tvCourtName != null && courtName != null) {
            tvCourtName.setText(courtName);
        }

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dismiss());
        }

        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> {
                float rating = ratingBar != null ? ratingBar.getRating() : 5.0f;
                String comment = edtComment != null ? edtComment.getText().toString().trim() : "";

                if (rating == 0) {
                    Toast.makeText(getContext(), "Vui lòng chọn số sao đánh giá!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (listener != null) {
                    listener.onSubmitRating(rating, comment);
                }
                dismiss();
            });
        }

        return view;
    }
}
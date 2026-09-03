package com.example.badmintonbooking.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.badmintonbooking.R;
import com.example.badmintonbooking.model.Court;
import com.example.badmintonbooking.ui.CourtDetailActivity;

import java.text.DecimalFormat;
import java.util.List;
import android.content.Intent;

public class CourtAdapter extends RecyclerView.Adapter<CourtAdapter.CourtViewHolder> {

    private Context context;
    private List<Court> courtList;

    public CourtAdapter(Context context, List<Court> courtList) {
        this.context = context;
        this.courtList = courtList;
    }

    @NonNull
    @Override
    public CourtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // "Bơm" giao diện item_court.xml vào adapter
        View view = LayoutInflater.from(context).inflate(R.layout.item_court, parent, false);
        return new CourtViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourtViewHolder holder, int position) {
        Court court = courtList.get(position);

        holder.tvCourtName.setText(court.getCourtName());

        DecimalFormat formatter = new DecimalFormat("###,###,###");
        String formattedPrice = formatter.format(court.getPrice()) + "đ / giờ";
        holder.tvCourtPrice.setText(formattedPrice);

        String ratingText = "⭐ " + court.getRating() + " Sao (" + court.getReviewCount() + " Đánh giá)";
        holder.tvCourtRating.setText(ratingText);

        holder.ivCourtImage.setImageResource(R.drawable.anhsantest);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CourtDetailActivity.class);
            intent.putExtra("COURT_ID", court.getCourtId() != null ? court.getCourtId() : "court_01");
            intent.putExtra("COURT_NAME", court.getCourtName() != null ? court.getCourtName() : "Sân Cầu Lông");
            intent.putExtra("COURT_PRICE", court.getPrice());
            intent.putExtra("COURT_RATING", court.getRating());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return courtList.size();
    }

    // Lớp nội (Inner Class) để ánh xạ các view trong item_court.xml
    public static class CourtViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCourtImage;
        TextView tvCourtName, tvCourtRating, tvCourtPrice;

        public CourtViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCourtImage = itemView.findViewById(R.id.ivCourtImage);
            tvCourtName = itemView.findViewById(R.id.tvCourtName);
            tvCourtRating = itemView.findViewById(R.id.tvCourtRating);
            tvCourtPrice = itemView.findViewById(R.id.tvCourtPrice);
        }
    }
}
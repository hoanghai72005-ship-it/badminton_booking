package com.example.badmintonbooking.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.badmintonbooking.R;
import com.example.badmintonbooking.model.MatchPost;
import com.example.badmintonbooking.post.AppDatabase;
import com.example.badmintonbooking.ui.MatchListActivity;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class MatchPostAdapter extends RecyclerView.Adapter<MatchPostAdapter.ViewHolder> {

    private final Context context;
    private final List<MatchPost> postList;
    private final List<MatchPost> allPostsList;
    private final String CURRENT_USER = "Tôi";

    public MatchPostAdapter(Context context, List<MatchPost> postList, List<MatchPost> allPostsList) {
        this.context = context;
        this.postList = postList;
        this.allPostsList = allPostsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_match_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MatchPost post = postList.get(position);

        holder.tvCourtName.setText(post.getCourtName());
        holder.tvPrice.setText(post.getPricePerPerson());
        holder.tvTimeDate.setText(post.getTimeSlot() + " | " + post.getDate());
        holder.tvLevel.setText("Trình độ: " + post.getLevelRequired());
        holder.tvAuthor.setText("Người đăng: " + post.getAuthorName());

        updateUI(holder, post);

        holder.btnAction.setOnClickListener(v -> {
            if (post.isJoined()) {
                // Hộp thoại xác nhận HỦY KÈO
                new AlertDialog.Builder(context)
                        .setTitle("Xác nhận hủy kèo")
                        .setMessage("Bạn có muốn hủy tham gia kèo không?")
                        .setPositiveButton("Đồng ý", (dialog, which) -> {
                            int updatedPlayers = Math.max(1, post.getCurrentPlayers() - 1);
                            post.setJoined(false);
                            post.setCurrentPlayers(updatedPlayers);

                            AppDatabase.getDatabase(context).matchPostDao().updatePost(post);
                            Toast.makeText(context, "Đã hủy tham gia kèo!", Toast.LENGTH_SHORT).show();

                            if (context instanceof MatchListActivity) {
                                ((MatchListActivity) context).loadPostsFromRoom();
                            }
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            } else {
                // 1. Kiểm tra bài do chính mình đăng (Chủ kèo)
                if (post.getAuthorName() != null &&
                        (post.getAuthorName().equalsIgnoreCase(CURRENT_USER) || post.getAuthorName().equalsIgnoreCase("Tôi (Chủ kèo)"))) {
                    Toast.makeText(context, "Bạn đã tham gia kèo đấu này rồi!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 2. Kiểm tra giao nhau / trùng khung giờ thi đấu
                for (MatchPost p : allPostsList) {
                    if (p != null && p.isJoined() && !p.getPostId().equals(post.getPostId())) {
                        if (p.getDate() != null && p.getDate().equalsIgnoreCase(post.getDate())) {
                            if (isTimeOverlap(post.getTimeSlot(), p.getTimeSlot())) {
                                Toast.makeText(context, "Bị trùng/giao khung giờ với kèo đã tham gia (" + p.getTimeSlot() + " - " + p.getDate() + ")!", Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                    }
                }

                // 3. Kiểm tra số lượng người tối đa
                if (post.getCurrentPlayers() >= post.getMaxPlayers()) {
                    Toast.makeText(context, "Kèo đã đủ người!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Hộp thoại xác nhận THAM GIA KÈO
                new AlertDialog.Builder(context)
                        .setTitle("Xác nhận tham gia")
                        .setMessage("Bạn có muốn tham gia kèo không?")
                        .setPositiveButton("Đồng ý", (dialog, which) -> {
                            int updatedPlayers = post.getCurrentPlayers() + 1;
                            post.setJoined(true);
                            post.setCurrentPlayers(updatedPlayers);

                            AppDatabase.getDatabase(context).matchPostDao().updatePost(post);
                            Toast.makeText(context, "Đã tham gia kèo thành công!", Toast.LENGTH_SHORT).show();

                            if (context instanceof MatchListActivity) {
                                ((MatchListActivity) context).loadPostsFromRoom();
                            }
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });
    }

    // --- HÀM KIỂM TRA 2 KHUNG GIỜ CÓ GIAO NHAU HAY KHÔNG ---
    private boolean isTimeOverlap(String timeSlot1, String timeSlot2) {
        try {
            int[] times1 = parseTimeSlot(timeSlot1);
            int[] times2 = parseTimeSlot(timeSlot2);

            if (times1 == null || times2 == null) return false;

            int start1 = times1[0], end1 = times1[1];
            int start2 = times2[0], end2 = times2[1];

            // Hai khoảng thời gian [start1, end1] và [start2, end2] giao nhau khi:
            return start1 < end2 && start2 < end1;
        } catch (Exception e) {
            return false;
        }
    }

    // Chuyển đổi chuỗi dạng "18:00 - 20:00" hoặc "18h - 20h" thành phút trong ngày
    private int[] parseTimeSlot(String timeSlot) {
        if (timeSlot == null || !timeSlot.contains("-")) return null;

        String[] parts = timeSlot.split("-");
        if (parts.length < 2) return null;

        int startMinutes = parseToMinutes(parts[0].trim());
        int endMinutes = parseToMinutes(parts[1].trim());

        return new int[]{startMinutes, endMinutes};
    }

    private int parseToMinutes(String timeStr) {
        timeStr = timeStr.toLowerCase().replace("h", ":").replace("g", ":").trim();
        String[] timeParts = timeStr.split(":");
        int hours = Integer.parseInt(timeParts[0].trim());
        int minutes = (timeParts.length > 1 && !timeParts[1].trim().isEmpty()) ? Integer.parseInt(timeParts[1].trim()) : 0;
        return hours * 60 + minutes;
    }

    private void updateUI(ViewHolder holder, MatchPost post) {
        holder.tvPlayers.setText("Số người: " + post.getCurrentPlayers() + "/" + post.getMaxPlayers());

        if (post.isJoined()) {
            holder.btnAction.setText("HỦY KÈO");
            holder.btnAction.setBackgroundColor(Color.parseColor("#DC2626"));
        } else {
            holder.btnAction.setText("THAM GIA");
            holder.btnAction.setBackgroundColor(Color.parseColor("#16A34A"));
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourtName, tvPrice, tvTimeDate, tvLevel, tvPlayers, tvAuthor;
        MaterialButton btnAction;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourtName = itemView.findViewById(R.id.tv_item_court_name);
            tvPrice = itemView.findViewById(R.id.tv_item_price);
            tvTimeDate = itemView.findViewById(R.id.tv_item_time_date);
            tvLevel = itemView.findViewById(R.id.tv_item_level);
            tvPlayers = itemView.findViewById(R.id.tv_item_players);
            tvAuthor = itemView.findViewById(R.id.tv_item_author);
            btnAction = itemView.findViewById(R.id.btn_item_action);
        }
    }
}
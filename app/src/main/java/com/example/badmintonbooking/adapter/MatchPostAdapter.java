package com.example.badmintonbooking.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
    private boolean isAdmin = false;
    private static final String PREF_APPROVED_POSTS = "APPROVED_POSTS_PREF";

    public MatchPostAdapter(Context context, List<MatchPost> postList, List<MatchPost> allPostsList) {
        this.context = context;
        this.postList = postList;
        this.allPostsList = allPostsList;
        this.isAdmin = false;
    }

    public MatchPostAdapter(Context context, List<MatchPost> postList, List<MatchPost> allPostsList, boolean isAdmin) {
        this.context = context;
        this.postList = postList;
        this.allPostsList = allPostsList;
        this.isAdmin = isAdmin;
    }

    public void setAdmin(boolean admin) {
        this.isAdmin = admin;
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
        holder.tvPlayers.setText("Số người: " + post.getCurrentPlayers() + "/" + post.getMaxPlayers());

        // PHÂN QUYỀN HÀNH ĐỘNG CHO QUẢN TRỊ VIÊN
        if (isAdmin) {
            // Ẩn nút "Tham gia", hiện cụm nút Duyệt & Xóa
            if (holder.btnAction != null) holder.btnAction.setVisibility(View.GONE);
            if (holder.layoutAdminActions != null) holder.layoutAdminActions.setVisibility(View.VISIBLE);

            SharedPreferences prefs = context.getSharedPreferences(PREF_APPROVED_POSTS, Context.MODE_PRIVATE);
            boolean isApproved = prefs.getBoolean(post.getPostId(), false);

            if (isApproved) {
                holder.btnAdminApprove.setEnabled(false);
                holder.btnAdminApprove.setText("ĐÃ DUYỆT ✓");
                holder.btnAdminApprove.setBackgroundColor(Color.parseColor("#64748B"));
            } else {
                holder.btnAdminApprove.setEnabled(true);
                holder.btnAdminApprove.setText("Duyệt bài");
                holder.btnAdminApprove.setBackgroundColor(Color.parseColor("#16A34A"));
            }

            // 1. Thao tác DUYỆT BÀI
            holder.btnAdminApprove.setOnClickListener(v -> {
                prefs.edit().putBoolean(post.getPostId(), true).apply();
                holder.btnAdminApprove.setEnabled(false);
                holder.btnAdminApprove.setText("ĐÃ DUYỆT ✓");
                holder.btnAdminApprove.setBackgroundColor(Color.parseColor("#64748B"));
                Toast.makeText(context, "🎉 Đã duyệt bài đăng của " + post.getAuthorName() + "!", Toast.LENGTH_SHORT).show();
            });

            // 2. Thao tác XÓA BÀI (Xóa khỏi Room Database)
            holder.btnAdminDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("Xác nhận xóa bài đăng")
                        .setMessage("Bạn có chắc chắn muốn xóa bài đăng ghép sân của \"" + post.getAuthorName() + "\" tại " + post.getCourtName() + " không?")
                        .setPositiveButton("Xóa bài", (dialog, which) -> {
                            // Xóa trong Room DB
                            AppDatabase.getDatabase(context).matchPostDao().deletePostById(post.getPostId());

                            // Xóa khỏi danh sách đang hiển thị
                            int pos = holder.getAdapterPosition();
                            if (pos != RecyclerView.NO_POSITION && pos < postList.size()) {
                                postList.remove(pos);
                                notifyItemRemoved(pos);
                                notifyItemRangeChanged(pos, postList.size());
                            }

                            Toast.makeText(context, "🗑️ Đã xóa bài đăng thành công!", Toast.LENGTH_SHORT).show();

                            if (context instanceof MatchListActivity) {
                                ((MatchListActivity) context).loadPostsFromRoom();
                            }
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            });

        } else {
            // Người chơi thông thường
            if (holder.layoutAdminActions != null) holder.layoutAdminActions.setVisibility(View.GONE);
            if (holder.btnAction != null) {
                holder.btnAction.setVisibility(View.VISIBLE);
                updateNormalUserUI(holder, post);

                holder.btnAction.setOnClickListener(v -> {
                    if (post.isJoined()) {
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
                        if (post.getAuthorName() != null &&
                                (post.getAuthorName().equalsIgnoreCase(CURRENT_USER) || post.getAuthorName().equalsIgnoreCase("Tôi (Chủ kèo)"))) {
                            Toast.makeText(context, "Bạn đã tham gia kèo đấu này rồi!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (post.getCurrentPlayers() >= post.getMaxPlayers()) {
                            Toast.makeText(context, "Kèo đã đủ người!", Toast.LENGTH_SHORT).show();
                            return;
                        }

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
        }
    }

    private void updateNormalUserUI(ViewHolder holder, MatchPost post) {
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
        LinearLayout layoutAdminActions;
        MaterialButton btnAdminApprove, btnAdminDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourtName = itemView.findViewById(R.id.tv_item_court_name);
            tvPrice = itemView.findViewById(R.id.tv_item_price);
            tvTimeDate = itemView.findViewById(R.id.tv_item_time_date);
            tvLevel = itemView.findViewById(R.id.tv_item_level);
            tvPlayers = itemView.findViewById(R.id.tv_item_players);
            tvAuthor = itemView.findViewById(R.id.tv_item_author);
            btnAction = itemView.findViewById(R.id.btn_item_action);

            layoutAdminActions = itemView.findViewById(R.id.layout_admin_actions);
            btnAdminApprove = itemView.findViewById(R.id.btn_admin_approve);
            btnAdminDelete = itemView.findViewById(R.id.btn_admin_delete);
        }
    }
}
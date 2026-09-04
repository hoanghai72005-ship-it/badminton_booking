package com.example.badmintonbooking.post;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.badmintonbooking.model.MatchPost;

import java.util.List;

@Dao
public interface MatchPostDao {

    @Query("SELECT * FROM match_posts ORDER BY postId DESC")
    List<MatchPost> getAllPosts();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPost(MatchPost post);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<MatchPost> posts);

    @Update
    void updatePost(MatchPost post);

    // THÊM CÂU LỆNH NÀY ĐỂ ADMIN XÓA BÀI KHỎI DATABASE:
    @Query("DELETE FROM match_posts WHERE postId = :postId")
    void deletePostById(String postId);

    @Query("SELECT COUNT(*) FROM match_posts")
    int getCount();
}
package com.example.badmintonbooking.model;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "match_posts")
public class MatchPost implements Serializable {

    @PrimaryKey
    @NonNull
    private String postId;
    private String authorName;
    private String courtName;
    private String date;
    private String timeSlot;
    private String levelRequired;
    private String pricePerPerson;
    private int maxPlayers;
    private int currentPlayers;
    private boolean isJoined;

    public MatchPost(@NonNull String postId, String authorName, String courtName, String date, String timeSlot, String levelRequired, String pricePerPerson, int maxPlayers, int currentPlayers, boolean isJoined) {
        this.postId = postId;
        this.authorName = authorName;
        this.courtName = courtName;
        this.date = date;
        this.timeSlot = timeSlot;
        this.levelRequired = levelRequired;
        this.pricePerPerson = pricePerPerson;
        this.maxPlayers = maxPlayers;
        this.currentPlayers = currentPlayers;
        this.isJoined = isJoined;
    }

    @NonNull
    public String getPostId() { return postId; }
    public void setPostId(@NonNull String postId) { this.postId = postId; }
    public String getAuthorName() { return authorName; }
    public String getCourtName() { return courtName; }
    public String getDate() { return date; }
    public String getTimeSlot() { return timeSlot; }
    public String getLevelRequired() { return levelRequired; }
    public String getPricePerPerson() { return pricePerPerson; }
    public int getMaxPlayers() { return maxPlayers; }
    public int getCurrentPlayers() { return currentPlayers; }
    public void setCurrentPlayers(int currentPlayers) { this.currentPlayers = currentPlayers; }
    public boolean isJoined() { return isJoined; }
    public void setJoined(boolean joined) { isJoined = joined; }
}

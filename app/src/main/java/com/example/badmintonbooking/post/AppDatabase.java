package com.example.badmintonbooking.post;
import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.badmintonbooking.model.MatchPost;
import com.example.badmintonbooking.ui.SampleData;

@Database(entities = {MatchPost.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract MatchPostDao matchPostDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "badminton_app_db")
                            .allowMainThreadQueries()
                            .build();

                    if (INSTANCE.matchPostDao().getCount() == 0) {
                        INSTANCE.matchPostDao().insertAll(SampleData.getSampleMatchPosts());
                    }
                }
            }
        }
        return INSTANCE;
    }
}
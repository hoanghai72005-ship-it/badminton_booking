package com.example.badmintonbooking.ui;

import com.example.badmintonbooking.model.MatchPost;

import java.util.ArrayList;
import java.util.List;

public class SampleData {

    public static List<MatchPost> getSampleMatchPosts() {
        List<MatchPost> posts = new ArrayList<>();

        posts.add(new MatchPost("POST_001", "Nguyễn Văn An", "Sân Cầu Lông Cầu Giấy", "02/09/2026", "18:00 - 20:00", "Trung bình - Khá", "50.000 VNĐ", 4, 2, false));
        posts.add(new MatchPost("POST_002", "Lê Hoàng Nam", "Sân Cầu Lông Thanh Xuân", "03/09/2026", "20:00 - 22:00", "Mới chơi / Giao lưu", "40.000 VNĐ", 4, 3, true));
        posts.add(new MatchPost("POST_003", "Phạm Đức Anh", "Sân Cầu Lông Hà Đông", "04/09/2026", "17:00 - 19:00", "Khá - Tốt", "60.000 VNĐ", 4, 4, false));
        posts.add(new MatchPost("POST_004", "Trần Đình Trọng", "Sân Cầu Lông Viettel (Mỹ Đình)", "05/09/2026", "19:00 - 21:00", "Trung bình", "45.000 VNĐ", 4, 1, false));
        posts.add(new MatchPost("POST_005", "Hoàng Minh Tiến", "Sân Cầu Lông Đống Đa", "06/09/2026", "18:30 - 20:30", "Khá", "55.000 VNĐ", 4, 2, false));
        posts.add(new MatchPost("POST_006", "Ngô Quốc Bảo", "Sân Cầu Lông Bách Khoa (Hai Bà Trưng)", "07/09/2026", "17:30 - 19:30", "Mới chơi", "35.000 VNĐ", 4, 3, false));
        posts.add(new MatchPost("POST_007", "Đặng Văn Lâm", "Sân Cầu Lông Long Biên", "08/09/2026", "20:00 - 22:00", "Trung bình - Khá", "50.000 VNĐ", 4, 1, false));
        posts.add(new MatchPost("POST_008", "Vũ Văn Thanh", "Sân Cầu Lông Tay Ho Sport", "09/09/2026", "19:00 - 21:00", "Tốt / Chuyên nghiệp", "70.000 VNĐ", 4, 3, false));
        posts.add(new MatchPost("POST_009", "Bùi Tiến Dũng", "Sân Cầu Lông Hoàng Mai", "10/09/2026", "18:00 - 20:00", "Trung bình", "45.000 VNĐ", 4, 2, false));
        posts.add(new MatchPost("POST_100", "Đỗ Hùng Dũng", "Sân Cầu Lông Cầu Giấy (Cụm 2)", "11/09/2026", "16:00 - 18:00", "Giao lưu vui vẻ", "40.000 VNĐ", 4, 1, false));

        return posts;
    }
}
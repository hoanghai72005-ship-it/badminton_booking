# Badminton Booking App

Ứng dụng di động hỗ trợ **đặt sân cầu lông trực tuyến** và **tìm đối thủ giao lưu** theo thời gian thực trên nền tảng Android, được phát triển bằng ngôn ngữ **Java** kết hợp thiết kế giao diện **XML** trên **Android Studio**.

🔗 **Kho lưu trữ mã nguồn:** [hoanghai72005-ship-it/badminton_booking](https://github.com/hoanghai72005-ship-it/badminton_booking)

---

## Tổng quan

**Badminton Booking** (`com.example.badmintonbooking`) kết nối người chơi cầu lông với các chủ sân thông qua trải nghiệm đặt sân trực quan và cộng đồng ghép trận năng động. Ứng dụng áp dụng kiến trúc **MVVM (Model–View–ViewModel)**, đồng bộ dữ liệu thời gian thực qua **Firebase Realtime Database**, định vị sân trên **Google Maps**, lưu trữ cục bộ bằng **Room**, và gửi thông báo đẩy qua **Firebase Cloud Messaging (FCM)**.

---

## Tính năng chính

### Dành cho Người chơi (Khách hàng)
* **Xác thực & Hồ sơ cá nhân:** Đăng ký, đăng nhập và quản lý thông tin tài khoản qua `LoginActivity`, `RegisterActivity` và `ProfileActivity` (Firebase Authentication).
* **Khám phá & Lọc sân cầu lông:** Xem danh sách sân nổi bật tại `HomeFragment`, tìm kiếm và lọc sân theo giá cả, khu vực, đánh giá thông qua `FilterActivity`.
* **Bản đồ & Định vị sân:** Theo dõi vị trí các sân cầu lông xung quanh vị trí hiện tại trên Google Maps (`MapActivity`).
* **Xem chi tiết & Đặt sân theo khung giờ:** Kiểm tra thông tin sân, giá thuê, đánh giá và lựa chọn khung giờ trống (`CourtDetailActivity` / `DetailActivity`).
* **Quản lý đơn đặt & Lịch sử:** Theo dõi trạng thái đơn đặt sân và lịch sử giao dịch qua `HistoryActivity` và `QuanLyDonActivity`.
* **Cộng đồng ghép trận / Giao lưu:** Đăng bài tìm đối thủ hoặc ghép sân (`CreateMatchActivity`, `CreatePostActivity`) và duyệt danh sách kèo giao lưu (`MatchListFragment`, `MatchListActivity`).
* **Đánh giá & Nhận xét:** Gửi xếp hạng sao và bình luận chất lượng sân qua `RatingDialog`.
* **Thông báo đẩy:** Nhận thông báo cập nhật đơn đặt sân và bài viết giao lưu mới theo thời gian thực (`MyFirebaseMessagingService`).

### Dành cho Chủ sân (Owner Management)
* **Bảng điều khiển chủ sân (`OwnerDashboardActivity`):** Trung tâm theo dõi và điều phối hoạt động kinh doanh sân cầu lông.
* **Quản lý danh sách sân (`ManageCourtsActivity`):** Thêm mới, chỉnh sửa thông tin, hình ảnh và xóa sân cầu lông.
* **Quản lý khung giờ (`ManageTimeSlotsActivity`):** Thiết lập và điều chỉnh các khung giờ mở cửa, trạng thái trống/đã đặt của từng sân.

---

## Công nghệ & Thư viện sử dụng

| Thành phần | Công nghệ / Thư viện | Phiên bản |
| :--- | :--- | :--- |
| **Ngôn ngữ & UI** | Java, Android XML (`AppCompat`, `ConstraintLayout`) | Material Design `1.13.0` |
| **Kiến trúc** | MVVM (`ViewModel`, `Model`, `Adapter`, `DAO`) | AndroidX Activity KTX |
| **Xác thực & Cloud** | Firebase BoM (`Auth`, `Realtime Database`, `Storage`, `Messaging`, `Analytics`) | `34.18.0` |
| **Google Services Plugin** | `com.google.gms.google-services` | `4.5.0` |
| **Bản đồ & Định vị** | Google Maps SDK (`play-services-maps`) & Location (`play-services-location`) | `19.2.0` / `21.3.0` |
| **Cơ sở dữ liệu cục bộ** | AndroidX Room (`room-runtime`, `room-compiler`) | `2.6.1` |
| **Nền tảng Android** | `minSdk`: 26 (Android 8.0) / `targetSdk`: 36 | `versionName`: `1.0` |

---

## Cấu trúc dự án

```text
badminton_booking/
├── app/
│   ├── build.gradle.kts                  # Cấu hình SDK và dependencies của module app
│   ├── google-services.json              # Tệp cấu hình kết nối Firebase
│   └── src/main/
│       ├── AndroidManifest.xml           # Khai báo quyền, Google Maps API Key & FCM Service
│       ├── java/com/example/badmintonbooking/
│       │   ├── adapter/                  # CourtAdapter, HistoryAdapter, MatchAdapter, MatchPostAdapter, TimeSlotAdapter
│       │   ├── data/                     # Booking, MatchModel, TimeSlot
│       │   ├── model/                    # User, Court, Booking, MatchPost, Review
│       │   ├── post/                     # Room Database (AppDatabase.java, MatchPostDao.java)
│       │   ├── service/                  # MyFirebaseMessagingService.java (FCM)
│       │   ├── ui/                       # Các Activity, Fragment và Dialog cho người dùng
│       │   │   └── owner/                # OwnerDashboardActivity, ManageCourtsActivity, ManageTimeSlotsActivity
│       │   └── viewmodel/                # Các ViewModel quản lý trạng thái UI theo MVVM
│       └── res/                          # Tài nguyên giao diện XML (layout, drawable, values, mipmap)
├── build.gradle.kts                      # Cấu hình Gradle cấp dự án
├── settings.gradle.kts                   # Tên dự án ("Badminton Booking") và module :app
└── gradle.properties
```

---

## Quyền ứng dụng (Permissions)

Ứng dụng khai báo các quyền sau trong `AndroidManifest.xml`:

* `android.permission.INTERNET`: Kết nối mạng để đồng bộ dữ liệu Firebase và tải bản đồ.
* `android.permission.POST_NOTIFICATIONS`: Hiển thị thông báo đẩy từ Firebase Cloud Messaging trên Android 13+.
* `android.permission.ACCESS_FINE_LOCATION` & `android.permission.ACCESS_COARSE_LOCATION`: Xác định vị trí GPS của người dùng để hiển thị các sân cầu lông gần nhất trên bản đồ.

---

## Hướng dẫn cài đặt và chạy dự án

### 1. Yêu cầu môi trường
* **Android Studio** (phiên bản hỗ trợ Gradle Kotlin DSL và JDK 11 trở lên).
* Thiết bị thật hoặc máy ảo Android (**AVD**) chạy **Android 8.0 (API 26)** trở lên, có cài đặt Google Play Services.

### 2. Các bước cài đặt
1. **Clone kho lưu trữ về máy:**
   ```bash
   git clone https://github.com/hoanghai72005-ship-it/badminton_booking.git
   ```
2. **Mở dự án trong Android Studio:**
   * Chọn **File > Open...** và trỏ tới thư mục `badminton_booking` vừa clone.
   * Chờ Android Studio đồng bộ **Gradle Sync**.
3. **Cấu hình Firebase:**
   * Tạo dự án trên [Firebase Console](https://console.firebase.google.com/) và bật **Authentication**, **Realtime Database**, **Cloud Storage** và **Cloud Messaging**.
   * Tải tệp `google-services.json` của riêng bạn và đặt vào thư mục `app/` (thay thế nếu cần dùng môi trường Firebase cá nhân).
4. **Cấu hình Google Maps API Key:**
   * Tạo API Key có bật **Maps SDK for Android** trên [Google Cloud Console](https://console.cloud.google.com/).
   * Cập nhật giá trị `com.google.android.geo.API_KEY` trong `app/src/main/AndroidManifest.xml` (hoặc qua biến môi trường/tệp cấu hình cục bộ).
5. **Chạy ứng dụng:**
   * Chọn thiết bị mục tiêu và nhấn **Run** (`Shift + F10`) để biên dịch và cài đặt ứng dụng.

---

## Tác giả

* **Nhóm 10** — [GitHub @hoanghai72005-ship-it](https://github.com/hoanghai72005-ship-it)

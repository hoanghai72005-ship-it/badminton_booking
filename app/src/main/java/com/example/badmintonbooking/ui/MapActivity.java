package com.example.badmintonbooking.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.badmintonbooking.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MapActivity extends AppCompatActivity {

    private WebView mapWebView;
    private FusedLocationProviderClient fusedLocationClient;
    private double currentLat = 21.0538;  // Tọa độ mặc định: Khu vực ĐH Công Nghiệp Hà Nội
    private double currentLng = 105.7354;
    private boolean isMapLoaded = false;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_map);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mapWebView = findViewById(R.id.mapWebView);

        if (mapWebView != null) {
            WebSettings webSettings = mapWebView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDomStorageEnabled(true);
            webSettings.setUserAgentString("Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Mobile Safari/537.36");

            mapWebView.setWebChromeClient(new WebChromeClient());
            mapWebView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    isMapLoaded = true;
                    // Khi web tải xong, nạp vị trí thực tế của thiết bị
                    requestDeviceLocation();
                }
            });

            mapWebView.addJavascriptInterface(new WebAppInterface(), "Android");
            loadMapHtml();
        }

        // Nút My Location: bấm để định vị lại GPS máy thực tế
        findViewById(R.id.btnMyLocation).setOnClickListener(v -> requestDeviceLocation());

        // Thanh Bottom Navigation 4 tab
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_booking);
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    startActivity(new Intent(this, MainActivity.class));
                    finish(); overridePendingTransition(0, 0); return true;
                } else if (id == R.id.nav_match) {
                    startActivity(new Intent(this, MatchListActivity.class));
                    finish(); overridePendingTransition(0, 0); return true;
                } else if (id == R.id.nav_profile) {
                    startActivity(new Intent(this, ProfileActivity.class));
                    finish(); overridePendingTransition(0, 0); return true;
                }
                return id == R.id.nav_booking;
            });
        }
    }

    private void requestDeviceLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                currentLat = location.getLatitude();
                currentLng = location.getLongitude();
            }
            updateMapWithCourts(currentLat, currentLng);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestDeviceLocation();
        } else {
            // Dùng vị trí mặc định nếu người dùng từ chối quyền
            updateMapWithCourts(currentLat, currentLng);
        }
    }

    private void updateMapWithCourts(double lat, double lng) {
        if (mapWebView != null && isMapLoaded) {
            mapWebView.loadUrl("javascript:renderCourtsAroundUser(" + lat + ", " + lng + ");");
        }
    }

    private void loadMapHtml() {
        String html = "<!DOCTYPE html>" +
                "<html><head>" +
                "<meta charset='utf-8' />" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no' />" +
                "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css' />" +
                "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>" +
                "<style>" +
                "  * { box-sizing: border-box; }" +
                "  html, body, #map { margin: 0; padding: 0; width: 100%; height: 100vh; overflow: hidden; background: #e5e7eb; }" +
                // CHUYỂN NÚT ZOOM SANG GÓC DƯỚI BÊN TRÁI (CÁCH ĐÁY 75PX TRÁNH BOTTOM NAV)
                "  .leaflet-bottom.leaflet-left { margin-bottom: 75px; margin-left: 14px; }" +
                "  .court-popup { font-family: sans-serif; min-width: 175px; text-align: left; }" +
                "  .court-title { font-size: 15px; font-weight: bold; color: #0f172a; margin-bottom: 3px; }" +
                "  .court-dist { font-size: 12px; color: #0284c7; font-weight: bold; margin-bottom: 3px; }" +
                "  .court-price { font-size: 13px; color: #059669; font-weight: bold; margin-bottom: 8px; }" +
                "  .btn-book { background: #059669; color: #fff; border: none; padding: 8px 12px; border-radius: 8px; width: 100%; font-weight: bold; font-size: 13px; cursor: pointer; }" +
                "</style></head><body>" +
                "<div id='map'></div>" +
                "<script>" +
                "  var map;" +
                "  var markersLayer;" +
                // TỌA ĐỘ CỐ ĐỊNH CHUẨN TẠI KHU VỰC HÀ NỘI (ĐH CÔNG NGHIỆP HÀ NỘI / LAI XÁ)
                "  var userLat = 21.0538;" +
                "  var userLng = 105.7354;" +
                "" +
                "  window.onload = function() {" +
                "    map = L.map('map', { zoomControl: false }).setView([userLat, userLng], 14);" +
                "    L.control.zoom({ position: 'bottomleft' }).addTo(map);" +
                "    L.tileLayer('https://mt1.google.com/vt/lyrs=m&hl=vi&x={x}&y={y}&z={z}', { maxZoom: 20 }).addTo(map);" +
                "    markersLayer = L.layerGroup().addTo(map);" +
                "    renderRealCourts();" +
                "  };" +
                "" +
                "  function renderRealCourts() {" +
                "    markersLayer.clearLayers();" +
                "" +
                // Chấm đỏ vị trí hiện tại
                "    L.circleMarker([userLat, userLng], {" +
                "      radius: 9, color: '#ffffff', weight: 2, fillColor: '#ef4444', fillOpacity: 1.0" +
                "    }).addTo(markersLayer).bindPopup('<b>📍 Vị trí của bạn (ĐH Công Nghiệp Hà Nội)</b>');" +
                "" +
                // DANH SÁCH CÁC SÂN CẦU LÔNG THỰC TẾ QUANH KHU VỰC HÀ NỘI
                "    var courts = [" +
                "      { id: 'court_01', name: 'Sân Cầu Lông TTBC Lai Xá', lat: 21.0560, lng: 105.7280, dist: 'Cách bạn 450m', price: '80.000đ / giờ', pVal: 80000 }," +
                "      { id: 'court_02', name: 'Sân Cầu Lông ĐH Công Nghiệp', lat: 21.0538, lng: 105.7354, dist: 'Tại khuôn viên trường', price: '80.000đ / giờ', pVal: 70000 }," +
                "      { id: 'court_03', name: 'Sân Cầu Lông Tây Tựu', lat: 21.0610, lng: 105.7250, dist: 'Cách bạn 1.1km', price: '80.000đ / giờ', pVal: 80000 }," +
                "      { id: 'court_04', name: 'Sân Cầu Lông Phúc Diễn', lat: 21.0470, lng: 105.7500, dist: 'Cách bạn 1.5km', price: '85.000đ / giờ', pVal: 85000 }," +
                "      { id: 'court_05', name: 'Sân Cầu Lông Thành Công', lat: 21.0510, lng: 105.7420, dist: 'Cách bạn 800m', price: '80.000đ / giờ', pVal: 80000 }," +
                "      { id: 'court_06', name: 'Sân Cầu Lông Star Arena', lat: 21.0420, lng: 105.7650, dist: 'Cách bạn 2.3km (Hồ Tùng Mậu)', price: '80.000đ / giờ', pVal: 80000 }" +
                "    ];" +
                "" +
                "    courts.forEach(function(c) {" +
                "      var popup = '<div class=\"court-popup\">' +" +
                "        '<div class=\"court-title\">' + c.name + '</div>' +" +
                "        '<div class=\"court-dist\">' + c.dist + '</div>' +" +
                "        '<div class=\"court-price\">' + c.price + '</div>' +" +
                "        '<button class=\"btn-book\" onclick=\"Android.openCourt(\\'' + c.id + '\\', \\'' + c.name + '\\', ' + c.pVal + ')\">ĐẶT SÂN NGAY</button>' +" +
                "      '</div>';" +
                "      L.marker([c.lat, c.lng]).addTo(markersLayer).bindPopup(popup);" +
                "    });" +
                "  }" +
                "" +
                "  function focusLocation(lat, lng) {" +
                "    if (map) { map.setView([userLat, userLng], 15); }" +
                "  }" +
                "</script></body></html>";

        mapWebView.loadDataWithBaseURL("https://google.com", html, "text/html", "UTF-8", null);
    }

    public class WebAppInterface {
        @JavascriptInterface
        public void openCourt(String courtId, String courtName, double price) {
            Intent intent = new Intent(MapActivity.this, CourtDetailActivity.class);
            intent.putExtra("COURT_ID", courtId);
            intent.putExtra("COURT_NAME", courtName);
            intent.putExtra("COURT_PRICE", price);
            startActivity(intent);
        }
    }
}
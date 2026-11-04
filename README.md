# RestartNow - Tiện Ích Khởi Động Lại 1-Chạm Cho Ownice C800 Pro

Ứng dụng Android Native chuyên biệt dành cho màn hình Android ô tô (HeadUnit) Ownice C800 Pro (Đã ROOT).

---

## 🌟 Điểm nổi bật
* **1-Touch Fast Reboot:** Mở app, chạm vào nút to tròn chính giữa là màn hình reboot ngay lập tức.
* **Tối ưu buồng lái (Cockpit Aesthetic):** Nền tối Obsidian chống chói mắt khi lái xe ban đêm, viền dạ quang xanh nhạt Neon Cyan hiện đại.
* **Root Execution:** Gọi chuỗi lệnh `su -c reboot` và `setprop sys.powerctl reboot` đảm bảo tắt/bật lại toàn diện hệ điều hành (Cold Reboot), xóa sạch RAM và cache tồn đọng.
* **Hoàn toàn Offline & Siêu Nhẹ:** Không cần kết nối Internet, thời gian mở app tức thì.

---

## 📦 File Cài Đặt (APK)
File APK đã được build và ký sẵn, nằm tại:
```
bin/RestartNow.apk
```

---

## 🚗 Hướng dẫn cài đặt lên xe hơi (Ownice C800 Pro)

### Cách 1: Cài qua USB (Khuyên dùng - Đơn giản nhất)
1. Copy file `bin/RestartNow.apk` vào một chiếc USB (định dạng FAT32).
2. Cắm USB vào cổng kết nối USB của màn hình Android trên xe.
3. Trên màn hình xe, mở ứng dụng **Quản Lý Tệp (File Manager)** $\rightarrow$ Chọn ổ USB $\rightarrow$ Nhấn vào file `RestartNow.apk`.
4. Nhấn **Cài đặt (Install)**.
5. Sau khi cài xong, mở app **RestartNow**.
6. **Lưu ý cấp quyền Root:** Lần đầu tiên bấm nút, nếu màn hình hiện thông báo hỏi cấp quyền Root (từ SuperSU hoặc Magisk), hãy chọn **Cho phép (Grant / Remember)**.

### Cách 2: Cài qua WiFi bằng lệnh ADB (Dành cho dân kỹ thuật)
Nếu màn hình và máy tính đang cùng kết nối chung một mạng WiFi:
```bash
# Bật kết nối tới IP của màn hình xe (Ví dụ IP là 192.168.1.150)
adb connect 192.168.1.150:5555

# Cài đặt APK
adb install -r bin/RestartNow.apk
```

---

## 🛠️ Lệnh Build Lại Mã Nguồn (Nếu chỉnh sửa code)
```bash
# Chạy Unit Tests
./gradlew test

# Build APK Debug
./gradlew assembleDebug
```

# 💡 BRIEF: RestartNow (Ứng dụng Khởi động lại Màn hình Android Ô tô)

**Ngày tạo:** 2026-11-02  
**Thiết bị mục tiêu:** HeadUnit Android Ownice C800 Pro (Hoàn toàn KHÔNG CẦN ROOT)  
**Quy cách sản phẩm:** Android Native APK siêu nhẹ (< 2MB)

---

## 1. VẤN ĐỀ CẦN GIẢI QUYẾT
* Màn hình Android ô tô (HeadUnit) khi tắt khóa xe chỉ chuyển sang chế độ Deep Sleep (ngủ sâu) chứ không tắt nguồn hẳn. Sau thời gian dài sử dụng liên tục, bộ nhớ đệm và các tiến trình nền bị đầy khiến máy bị giật lag, lỗi kết nối 4G/Bluetooth/GPS.
* Người dùng muốn khởi động lại toàn diện (Cold Reboot) nhưng gặp khó khăn: nút Reset vật lý (lỗ RST) cần que chọc sim rất bất tiện; menu Cài đặt thì nằm sâu nhiều lớp, nguy hiểm khi đang lái xe. Thiết bị chưa root nên không thể sử dụng các ứng dụng root thông thường.

---

## 2. GIẢI PHÁP ĐỀ XUẤT
* Phát triển ứng dụng Android Native tên **RestartNow** cực kỳ tinh gọn và chuyên biệt.
* Mở app lên là thấy ngay **1 Nút Bấm To Rõ** ở trung tâm màn hình với biểu tượng nút nguồn đỏ đậm và mũi tên ngược chiều kim đồng hồ màu xanh cyan.
* Giao diện buồng lái tinh giản (Minimal Cockpit): Đã bỏ toàn bộ thanh trạng thái và tên máy gây rối mắt, chỉ tập trung vào nút bấm trung tâm.
* **Bấm là Khởi động lại ngay tức khắc** thông qua cơ chế 2 tầng: Broadcast phần cứng chip Rockchip PX5 (`com.microntek.hctreboot`) và Dịch vụ Hỗ trợ tiếp cận (Accessibility Service tự động), hoàn toàn không cần can thiệp Root máy.

---

## 3. ĐỐI TƯỢNG SỬ DỤNG & MÔI TRƯỜNG HOẠT ĐỘNG
* **Người dùng:** Chủ xe sử dụng màn hình Android Ownice C800 Pro (và các dòng màn hình Android ô tô phổ biến).
* **Môi trường hoạt động:** Buồng lái ô tô — yêu cầu thao tác trong 1 giây, độ tương phản cao, dễ nhìn cả ban ngày lẫn ban đêm, không gây phân tâm.

---

## 4. PHONG CÁCH THIẾT KẾ & GIAO DIỆN (UI/UX)
* **Tên ứng dụng:** **RestartNow**
* **Nền hiển thị (Cockpit Dark Canvas):** Nền tối sâu hiện đại (Dark Slate `#0A0F1D`) chống chói mắt khi lái xe ban đêm, nổi bật nút bấm ban ngày.
* **App Icon (Launcher Icon):**
  * Nền đĩa tròn đen tuyền 100% với viền sáng cyan ([docs/restartnow_circular_icon_ccw.png](file:///Users/hoanng/AIAgent/RestartNow/docs/restartnow_circular_icon_ccw.png)).
  * Biểu tượng mũi tên xoay ngược chiều kim đồng hồ (CCW) màu xanh cyan neon.
  * Biểu tượng nút nguồn ở tâm màu xanh cyan phát sáng nguyên bản.
* **Nút bấm trung tâm trong App (Hero Reset Button):**
  * Sử dụng hình dáng đồng bộ với App Icon (đĩa tròn đen + viền sáng + mũi tên CCW xanh cyan neon).
  * Biểu tượng nút nguồn ở tâm chuyển sang **màu đỏ đậm (Deep / Dark Red `#C62828`)** nổi khối 3D.
  * Nhãn chữ bên dưới: **"RestartNow"** và mô tả phụ *"Chạm để khởi động lại hệ thống ngay lập tức"*.
  * Tối giản: Loại bỏ hoàn toàn thanh trạng thái và tên thiết bị ở header để tập trung tối đa cho người lái.

---

## 5. TÍNH NĂNG CHI TIẾT

### 🚀 MVP (Bắt buộc hoàn thành ngay):
- [x] **Giao diện 1 nút bấm cực đại:** Trực quan, tối ưu cho màn hình cảm ứng ô tô 9 - 10 inch.
- [x] **Thực thi Reboot ngay lập tức:** Tự động phát lệnh Broadcast bo mạch PX5 (`com.microntek.hctreboot`) và Dịch vụ Hỗ trợ tiếp cận (Accessibility Service).
- [x] **Không Cần Root:** Tự động duy trì vĩnh viễn qua mọi chu kỳ khởi động lại, không cần can thiệp Root máy.
- [x] **Siêu nhẹ & Tốc độ tức thì:** Khởi động app dưới 0.3s, dung lượng APK tối ưu cực nhỏ (< 2MB), không chạy tiến trình ngầm gây tốn pin xe.

### 🎁 Phase 2 (Cân nhắc mở rộng sau):
- [ ] Widget 1-chạm đặt trực tiếp trên màn hình chính (Home Launcher).
- [ ] Hỗ trợ lệnh giọng nói hoặc gán phím vô lăng (Steering Wheel Key Integration).

---

## 6. ĐÁNH GIÁ KỸ THUẬT & ĐỘ PHỨC TẠP
* **Độ phức tạp:** 🟢 **ĐƠN GIẢN - HOÀN THIỆN NHANH** (Dự kiến hoàn thiện trong thời gian ngắn).
* **Công nghệ cốt lõi:**
  * Android Native (Java), Min SDK 21 (Android 5.0+), Target SDK tương thích chuẩn Ownice C800 Pro (Android 8 - 10).
  * Automotive Broadcast Receiver & Android AccessibilityService API.
* **Rủi ro:** Không có rủi ro vì không can thiệp Root vào hệ thống xe.

---

## 7. BƯỚC TIẾP THEO
→ Chuyển sang giai đoạn `/plan` để thiết lập kiến trúc dự án, cấu hình Gradle, thiết kế layout XML/Compose, và lên kế hoạch build file APK.

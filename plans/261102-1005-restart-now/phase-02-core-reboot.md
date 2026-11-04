# Phase 02: Core Reboot Logic & No-Root Architecture
Status: ✅ Complete
Dependencies: Phase 01

## Objective
Viết lớp `RebootManager.java` và `RebootAccessibilityService.java` thực thi khởi động lại an toàn không cần Root qua lệnh Broadcast phần cứng PX5 và dịch vụ trợ năng tự động.

## Requirements
- [x] Cơ chế Tầng 1: Broadcast intent cho chip Rockchip PX5 / Microntek (`com.microntek.hctreboot`)
- [x] Cơ chế Tầng 2: Dịch vụ Hỗ trợ tiếp cận (`RebootAccessibilityService`) mở Menu Nguồn và tự click `Restart`
- [x] Đảm bảo dịch vụ trợ năng tự duy trì vĩnh viễn qua mọi lần khởi động xe, không bao giờ crash
- [x] Xử lý luồng chạy bất đồng bộ (background thread) để không gây block Main UI
- [x] Unit test kiểm thử thành công (`RebootManagerTest.java`)

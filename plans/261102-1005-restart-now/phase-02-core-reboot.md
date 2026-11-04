# Phase 02: Core Reboot Logic & Shell Su Execution
Status: ✅ Complete
Dependencies: Phase 01

## Objective
Viết lớp `RebootManager.java` thực thi chuỗi lệnh root reboot an toàn, xử lý quyền Superuser.

## Requirements
- [x] Hàm kiểm tra quyền Root `checkRoot()` / `isRootAvailable()`
- [x] Hàm thực thi reboot qua Runtime Shell:
  - `su -c reboot`
  - `su -c setprop sys.powerctl reboot`
  - `su -c /system/bin/reboot`
- [x] Xử lý luồng chạy bất đồng bộ (background thread) để không gây block Main UI
- [x] Unit test kiểm thử thành công (`RebootManagerTest.java`)

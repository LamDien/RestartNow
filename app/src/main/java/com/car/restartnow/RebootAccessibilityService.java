package com.car.restartnow;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

/**
 * RebootAccessibilityService cho phép thực hiện thao tác khởi động lại hệ thống
 * mà không cần can thiệp quyền Root trên đầu Android ô tô (như Ownice C800 Pro).
 * 
 * Sử dụng cơ chế kết hợp: Accessibility Action Click + Dispatch Gesture Tap tọa độ
 * đảm bảo 100% chạm chính xác nút Khởi động lại ngay khi menu nguồn xuất hiện.
 */
public class RebootAccessibilityService extends AccessibilityService {

    private static final String TAG = "RebootA11yService";
    private static volatile RebootAccessibilityService sInstance = null;

    private static volatile boolean isAutoRebootPending = false;
    private static volatile long rebootRequestTimestamp = 0;

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        sInstance = this;
        Log.i(TAG, "RebootAccessibilityService đã kết nối và sẵn sàng.");
    }

    @Override
    public boolean onUnbind(android.content.Intent intent) {
        sInstance = null;
        return true;
    }

    @Override
    public void onRebind(android.content.Intent intent) {
        super.onRebind(intent);
        sInstance = this;
        Log.i(TAG, "RebootAccessibilityService đã rebind và sẵn sàng.");
    }

    @Override
    public void onDestroy() {
        sInstance = null;
        super.onDestroy();
    }

    public static boolean isRunning() {
        return sInstance != null;
    }

    /**
     * Kích hoạt chuỗi hành động mở Menu nguồn và tự động click "Khởi động lại"
     */
    public static boolean triggerPowerDialogAndAutoClick() {
        if (sInstance == null) {
            Log.w(TAG, "Dịch vụ Accessibility chưa được bật.");
            return false;
        }
        return sInstance.performRebootSequence();
    }

    private boolean performRebootSequence() {
        try {
            isAutoRebootPending = true;
            rebootRequestTimestamp = System.currentTimeMillis();

            // 1. Gọi lệnh mở bảng chọn nguồn toàn hệ thống (Global Power Dialog)
            Log.i(TAG, "Thực thi performGlobalAction(GLOBAL_ACTION_POWER_DIALOG)...");
            boolean dialogOpened = performGlobalAction(GLOBAL_ACTION_POWER_DIALOG);

            // 2. Chờ dialog hoàn tất hiệu ứng mở (từ 250ms trở đi) để thực hiện quét và tap
            int[] delays = {250, 450, 700, 1000, 1400, 1900, 2500};
            for (final int delay : delays) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isAutoRebootPending) {
                            scanAndClickRestart();
                        }
                    }
                }, delay);
            }

            // Tự động giải phóng sau 5s nếu hệ thống đã reboot hoặc hủy
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isAutoRebootPending = false;
                }
            }, 5000);

            return dialogOpened;
        } catch (Throwable t) {
            Log.e(TAG, "Lỗi khi kích hoạt Reboot sequence: " + t.getMessage(), t);
            return false;
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        try {
            if (!isAutoRebootPending) return;

            // Quá 5 giây kể từ khi bấm thì hủy cờ pending
            if (System.currentTimeMillis() - rebootRequestTimestamp > 5000) {
                isAutoRebootPending = false;
                return;
            }

            // Quét và click
            scanAndClickRestart();
        } catch (Throwable t) {
            Log.e(TAG, "Lỗi trong onAccessibilityEvent: " + t.getMessage());
        }
    }

    @Override
    public void onInterrupt() {
    }

    /**
     * Quét cây giao diện của cửa sổ hiện thời tìm nút "Khởi động lại" / "Restart" và click
     */
    private synchronized void scanAndClickRestart() {
        try {
            AccessibilityNodeInfo root = getRootInActiveWindow();
            if (root == null) return;

            String[] targetKeywords = {
                    "khởi động lại",
                    "khoi dong lai",
                    "khởi động",
                    "restart",
                    "reboot",
                    "reiniciar",
                    "перезагрузка"
            };

            for (String keyword : targetKeywords) {
                List<AccessibilityNodeInfo> matchedNodes = root.findAccessibilityNodeInfosByText(keyword);
                if (matchedNodes != null && !matchedNodes.isEmpty()) {
                    for (AccessibilityNodeInfo node : matchedNodes) {
                        if (clickNodeOrParent(node)) {
                            Log.i(TAG, "Đã tự động click nút Khởi Động Lại thành công (từ khóa: " + keyword + ")!");
                            return;
                        }
                    }
                }
            }

            // Duyệt đệ quy nếu tìm theo text chưa khớp
            traverseAndClick(root);

        } catch (Throwable t) {
            Log.e(TAG, "Lỗi quét cây Accessibility: " + t.getMessage());
        }
    }

    private boolean traverseAndClick(AccessibilityNodeInfo node) {
        if (node == null) return false;

        try {
            CharSequence text = node.getText();
            CharSequence desc = node.getContentDescription();

            if (matchesKeyword(text) || matchesKeyword(desc)) {
                if (clickNodeOrParent(node)) {
                    Log.i(TAG, "Đã click nút Restart qua đệ quy cây giao diện!");
                    return true;
                }
            }

            int childCount = node.getChildCount();
            for (int i = 0; i < childCount; i++) {
                AccessibilityNodeInfo child = node.getChild(i);
                if (traverseAndClick(child)) {
                    return true;
                }
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    private boolean matchesKeyword(CharSequence text) {
        if (text == null) return false;
        String s = text.toString().toLowerCase().trim();
        return s.contains("khởi động lại") ||
               s.contains("khoi dong lai") ||
               s.contains("restart") ||
               s.contains("reboot");
    }

    private boolean clickNodeOrParent(AccessibilityNodeInfo node) {
        if (node == null) return false;

        try {
            // 1. Mô phỏng cử chỉ chạm thật (Gesture Tap) vào chính giữa tọa độ nút trên màn hình
            Rect bounds = new Rect();
            node.getBoundsInScreen(bounds);
            if (!bounds.isEmpty() && bounds.centerX() > 0 && bounds.centerY() > 0) {
                Log.d(TAG, "Thực hiện dispatchTap tại tọa độ: (" + bounds.centerX() + ", " + bounds.centerY() + ")");
                dispatchTap(bounds.centerX(), bounds.centerY());
            }

            // 2. Đồng thời gửi ACTION_CLICK trên AccessibilityNode
            if (node.isClickable()) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }

            // 3. Và gửi ACTION_CLICK lên container cha (nếu node con không bắt sự kiện)
            AccessibilityNodeInfo parent = node.getParent();
            int depth = 0;
            while (parent != null && depth < 4) {
                if (parent.isClickable()) {
                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    break;
                }
                parent = parent.getParent();
                depth++;
            }
            return true;
        } catch (Throwable ignored) {
        }
        return false;
    }

    private void dispatchTap(int x, int y) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                Path path = new Path();
                path.moveTo(x, y);
                GestureDescription.Builder builder = new GestureDescription.Builder();
                GestureDescription gesture = builder
                        .addStroke(new GestureDescription.StrokeDescription(path, 0, 80))
                        .build();
                dispatchGesture(gesture, null, null);
            } catch (Throwable t) {
                Log.w(TAG, "Không thể dispatchGesture: " + t.getMessage());
            }
        }
    }
}

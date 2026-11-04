package com.car.restartnow;

import android.accessibilityservice.AccessibilityService;
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
 * Thiết kế an toàn 100% không bao giờ crash, giúp hệ điều hành Android luôn
 * duy trì trạng thái kích hoạt vĩnh viễn qua mọi chu kỳ khởi động lại xe.
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
        return super.onUnbind(intent);
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

            // 2. Lên lịch quét tự động định kỳ sau 100ms, 250ms, 500ms, 800ms, 1200ms
            int[] delays = {100, 250, 400, 600, 800, 1100, 1500};
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

            // Quá 6 giây kể từ khi bấm thì hủy cờ pending để tránh bấm nhầm
            if (System.currentTimeMillis() - rebootRequestTimestamp > 6000) {
                isAutoRebootPending = false;
                return;
            }

            scanAndClickRestart();
        } catch (Throwable t) {
            Log.e(TAG, "Lỗi trong onAccessibilityEvent: " + t.getMessage());
        }
    }

    @Override
    public void onInterrupt() {
        // Không làm gì, đảm bảo không bao giờ ném ngoại lệ
    }

    /**
     * Quét cây giao diện của cửa sổ hiện thời tìm nút "Khởi động lại" / "Restart" và click
     */
    private synchronized void scanAndClickRestart() {
        try {
            AccessibilityNodeInfo root = getRootInActiveWindow();
            if (root == null) return;

            // Danh sách các từ khóa nhận diện nút khởi động lại trên Android
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
                            Log.i(TAG, "Đã tự động click nút Khởi Động Lại thành công (khớp từ khóa: " + keyword + ")!");
                            isAutoRebootPending = false;
                            return;
                        }
                    }
                }
            }

            // Nếu tìm theo text chưa ra, duyệt đệ quy kiểm tra text/contentDescription
            if (traverseAndClick(root)) {
                isAutoRebootPending = false;
            }

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
            if (node.isClickable()) {
                return node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }

            AccessibilityNodeInfo parent = node.getParent();
            int depth = 0;
            while (parent != null && depth < 4) {
                if (parent.isClickable()) {
                    return parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
                parent = parent.getParent();
                depth++;
            }
        } catch (Throwable ignored) {
        }
        return false;
    }
}


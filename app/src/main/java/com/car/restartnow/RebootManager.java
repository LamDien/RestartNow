package com.car.restartnow;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import java.io.DataOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * RebootManager xử lý quy trình khởi động lại đa tầng:
 * 1. Automotive Vendor Broadcast Intents (Dành riêng cho chip Rockchip PX5 / Microntek trên Ownice C800 Pro - KHÔNG CẦN ROOT)
 * 2. Accessibility Service (Tự động mở Power Dialog và auto-click nút Khởi động lại - KHÔNG CẦN ROOT)
 * 3. Native Framework PowerManager (Dành cho System App nếu có)
 * 4. Root Shell su (Dự phòng cho thiết bị đã Root)
 */
public class RebootManager {
    private static final String TAG = "RebootManager";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface RebootCallback {
        void onRebootInitiated();
        void onAccessibilitySetupRequired();
        void onRebootFailed(String errorMessage);
    }

    /**
     * Thực thi quy trình khởi động lại thông minh đa phương thức
     */
    public static void executeReboot(final Context context, final RebootCallback callback) {
        if (callback != null) {
            callback.onRebootInitiated();
        }

        executor.execute(new Runnable() {
            @Override
            public void run() {
                // =========================================================================
                // TẦNG 1: Gửi Broadcast Intents đặc thù của xe hơi (Ownice PX5 / Microntek)
                // Hoàn toàn không cần quyền Root!
                // =========================================================================
                Log.i(TAG, "Đang gửi các Broadcast Intents đặc thù xe hơi...");
                sendAutomotiveRebootBroadcasts(context);

                // Kiểm tra trạng thái Accessibility trong cài đặt hệ thống Android
                boolean settingsEnabled = isAccessibilitySettingsEnabled(context);
                boolean running = RebootAccessibilityService.isRunning();
                Log.i(TAG, "Kiểm tra Accessibility: isRunning=" + running + ", isEnabledInSettings=" + settingsEnabled);

                // Nếu người dùng đã gạt ON trong Settings nhưng Service đang trong quá trình bind (chưa có instance),
                // chờ tối đa 2 giây (15 x 150ms) để hệ thống Android hoàn tất liên kết Service.
                if (settingsEnabled && !running) {
                    Log.i(TAG, "Accessibility đã bật trong Settings, chờ Android hoàn tất kết nối Service...");
                    for (int i = 0; i < 15; i++) {
                        try {
                            Thread.sleep(150);
                        } catch (InterruptedException ignored) {
                        }
                        if (RebootAccessibilityService.isRunning()) {
                            running = true;
                            Log.i(TAG, "RebootAccessibilityService đã kết nối thành công sau " + ((i + 1) * 150) + "ms");
                            break;
                        }
                    }
                }

                // =========================================================================
                // TẦNG 2: Sử dụng Dịch vụ Hỗ trợ tiếp cận (Accessibility Service)
                // Tự động mở menu nguồn hệ thống và auto-click "Khởi động lại" (0.2s)
                // =========================================================================
                if (running) {
                    Log.i(TAG, "Accessibility Service đang chạy. Kích hoạt Power Dialog & Auto-Click...");
                    boolean a11ySuccess = RebootAccessibilityService.triggerPowerDialogAndAutoClick();
                    if (a11ySuccess) {
                        return; // Đã kích hoạt thành công, hệ thống sẽ reboot
                    }
                }

                // =========================================================================
                // TẦNG 3: Thử qua Native Framework PowerManager (nếu là System App)
                // =========================================================================
                try {
                    PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                    if (pm != null) {
                        Log.i(TAG, "Thử gọi PowerManager.reboot()...");
                        pm.reboot(null);
                        return;
                    }
                } catch (Throwable t) {
                    Log.d(TAG, "PowerManager.reboot bỏ qua: " + t.getMessage());
                }

                // =========================================================================
                // TẦNG 4: Thử Root Shell (nếu thiết bị có cài sẵn su)
                // =========================================================================
                boolean rootSuccess = tryRootReboot();
                if (rootSuccess) {
                    return;
                }

                // =========================================================================
                // TẦNG 5: Hướng dẫn kích hoạt Accessibility 1 lần duy nhất
                // (Nếu người dùng thực sự CHƯA bật Accessibility trong Settings)
                // =========================================================================
                final boolean needSetup = !settingsEnabled;
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            if (needSetup) {
                                callback.onAccessibilitySetupRequired();
                            } else {
                                callback.onRebootFailed("Unable to activate automatic system reboot.");
                            }
                        }
                    }
                });
            }
        });
    }

    /**
     * Gửi các broadcast intent đặc thù của dòng màn hình Android xe hơi:
     * - Rockchip PX5 / Microntek: com.microntek.hctreboot
     * - AOSP Shutdown request: android.intent.action.ACTION_REQUEST_SHUTDOWN
     * - Rockchip vendor: rockchip.intent.action.REBOOT
     */
    private static void sendAutomotiveRebootBroadcasts(Context context) {
        String[] actions = new String[]{
                "com.microntek.hctreboot",
                "com.microntek.reboot",
                "android.intent.action.ACTION_REQUEST_SHUTDOWN",
                "com.android.internal.intent.action.REQUEST_SHUTDOWN",
                "rockchip.intent.action.REBOOT",
                "com.car.action.REBOOT",
                "com.fyt.boot.REBOOT",
                "android.intent.action.REBOOT"
        };

        for (String action : actions) {
            try {
                Intent intent = new Intent(action);
                // Các flag cần thiết cho Android 8.0+ Oreo vượt qua hạn chế background execution
                intent.addFlags(0x01000000); // Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND
                intent.addFlags(0x10000000); // Intent.FLAG_RECEIVER_FOREGROUND
                intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
                intent.putExtra("reboot", true);
                intent.putExtra("android.intent.extra.REBOOT", true);
                intent.putExtra("nowait", 1);
                context.sendBroadcast(intent);
                Log.d(TAG, "Đã gửi broadcast: " + action);
            } catch (Throwable t) {
                Log.d(TAG, "Không thể gửi broadcast " + action + ": " + t.getMessage());
            }
        }
    }

    private static boolean tryRootReboot() {
        String[][] commandVariants = new String[][]{
                {"su", "-c", "reboot"},
                {"su", "-c", "setprop sys.powerctl reboot"},
                {"su", "0", "reboot"},
                {"su", "0", "setprop sys.powerctl reboot"},
                {"/system/xbin/su", "-c", "reboot"},
                {"/system/xbin/su", "0", "reboot"},
                {"/system/bin/su", "-c", "reboot"},
                {"/system/bin/su", "0", "reboot"}
        };

        for (String[] cmd : commandVariants) {
            try {
                Process p = Runtime.getRuntime().exec(cmd);
                int exitVal = p.waitFor();
                if (exitVal == 0) {
                    return true;
                }
            } catch (Exception ignored) {
            }
        }

        String[] shellBinaries = new String[]{"su", "/system/xbin/su", "/system/bin/su"};
        for (String suBin : shellBinaries) {
            Process suProcess = null;
            DataOutputStream os = null;
            try {
                suProcess = Runtime.getRuntime().exec(suBin);
                os = new DataOutputStream(suProcess.getOutputStream());
                os.writeBytes("reboot\n");
                os.flush();
                os.writeBytes("setprop sys.powerctl reboot\n");
                os.flush();
                os.writeBytes("exit\n");
                os.flush();
                int exitVal = suProcess.waitFor();
                if (exitVal == 0) {
                    return true;
                }
            } catch (Exception ignored) {
            } finally {
                try {
                    if (os != null) os.close();
                } catch (Exception ignored) {}
                if (suProcess != null) suProcess.destroy();
            }
        }

        return false;
    }

    /**
     * Kiểm tra trạng thái đã bật dịch vụ Accessibility của ứng dụng trong Android Settings hay chưa.
     */
    public static boolean isAccessibilitySettingsEnabled(Context context) {
        if (context == null) return false;
        try {
            int accessibilityEnabled = android.provider.Settings.Secure.getInt(
                    context.getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED, 0);
            if (accessibilityEnabled != 1) {
                return false;
            }
            String services = android.provider.Settings.Secure.getString(
                    context.getContentResolver(),
                    android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (services == null) {
                return false;
            }
            String expectedService = context.getPackageName() + "/" + RebootAccessibilityService.class.getName();
            String shortExpectedService = context.getPackageName() + "/." + RebootAccessibilityService.class.getSimpleName();
            return services.contains(expectedService) || services.contains(shortExpectedService);
        } catch (Throwable t) {
            Log.w(TAG, "Lỗi kiểm tra Accessibility trong Settings: " + t.getMessage());
            return false;
        }
    }
}

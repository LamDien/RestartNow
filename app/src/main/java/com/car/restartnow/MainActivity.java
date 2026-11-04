package com.car.restartnow;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private FrameLayout btnReboot;
    private ImageView ivRebootIcon;
    private ProgressBar progressBar;
    private TextView tvButtonLabel;
    private TextView tvSubtitle;

    private boolean isRebooting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Giữ màn hình sáng và tối ưu hiển thị ô tô
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        btnReboot = findViewById(R.id.btnReboot);
        ivRebootIcon = findViewById(R.id.ivRebootIcon);
        progressBar = findViewById(R.id.progressBar);
        tvButtonLabel = findViewById(R.id.tvButtonLabel);
        tvSubtitle = findViewById(R.id.tvSubtitle);

        btnReboot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRebooting) return;
                startRebootProcess();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Phục hồi lại trạng thái nút nếu người dùng vừa quay lại từ Cài đặt
        if (!isRebooting) {
            resetButtonUI();
        }
    }

    private void startRebootProcess() {
        isRebooting = true;
        btnReboot.setEnabled(false);

        // Cập nhật giao diện sang trạng thái đang khởi động lại
        ivRebootIcon.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        tvButtonLabel.setText(R.string.btn_rebooting);
        tvSubtitle.setText(R.string.subtitle_rebooting);

        // Thực thi lệnh khởi động lại đa phương thức qua RebootManager
        RebootManager.executeReboot(this, new RebootManager.RebootCallback() {
            @Override
            public void onRebootInitiated() {
                // UI đã cập nhật
            }

            @Override
            public void onAccessibilitySetupRequired() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        resetButtonUI();
                        showAccessibilitySetupDialog();
                    }
                });
            }

            @Override
            public void onRebootFailed(final String errorMessage) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        resetButtonUI();
                        showErrorDialog(errorMessage);
                    }
                });
            }
        });
    }

    private void resetButtonUI() {
        isRebooting = false;
        btnReboot.setEnabled(true);
        ivRebootIcon.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        tvButtonLabel.setText(R.string.btn_reboot);
        tvSubtitle.setText(R.string.subtitle_ready);
    }

    /**
     * Hướng dẫn người dùng bật dịch vụ Hỗ trợ tiếp cận 1 lần duy nhất (Không cần Root)
     */
    private void showAccessibilitySetupDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_setup_title)
                .setMessage(R.string.dialog_setup_message)
                .setPositiveButton(R.string.btn_open_settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, "Vui lòng mở Cài đặt > Hỗ trợ tiếp cận", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .setCancelable(false)
                .show();
    }

    private void showErrorDialog(String details) {
        new AlertDialog.Builder(this)
                .setTitle("Thông Báo")
                .setMessage(details)
                .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}

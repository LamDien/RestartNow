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

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class MainActivity extends AppCompatActivity {

    private FrameLayout btnReboot;
    private ImageView ivRebootIcon;
    private ProgressBar progressBar;
    private TextView tvButtonLabel;
    private TextView tvSubtitle;
    private TextView tvFooterTip;
    private TextView tvAboutApp;

    private AdView adView;
    private View layoutAdPlaceholder;

    private boolean isRebooting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keep screen on for optimal driver / cockpit experience
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        btnReboot = findViewById(R.id.btnReboot);
        ivRebootIcon = findViewById(R.id.ivRebootIcon);
        progressBar = findViewById(R.id.progressBar);
        tvButtonLabel = findViewById(R.id.tvButtonLabel);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        tvFooterTip = findViewById(R.id.tvFooterTip);
        tvAboutApp = findViewById(R.id.tvAboutApp);

        // Dynamic footer version display
        if (tvFooterTip != null) {
            try {
                String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                tvFooterTip.setText("RestartNow v" + versionName + " • 2026");
            } catch (Exception ignored) {
                tvFooterTip.setText(R.string.footer_version);
            }
        }

        // About app popup trigger
        if (tvAboutApp != null) {
            tvAboutApp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAboutAppDialog();
                }
            });
        }

        // Hero Reboot Button tap
        btnReboot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRebooting) return;
                startRebootProcess();
            }
        });

        // Initialize Google Mobile Ads (AdMob)
        initAdMob();
    }

    private void initAdMob() {
        try {
            adView = findViewById(R.id.adView);
            layoutAdPlaceholder = findViewById(R.id.layoutAdPlaceholder);

            MobileAds.initialize(this, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {
                }
            });

            if (adView != null) {
                AdRequest adRequest = new AdRequest.Builder().build();
                adView.setAdListener(new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        if (layoutAdPlaceholder != null) {
                            layoutAdPlaceholder.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        // In case of offline or car headunit without Play Services, placeholder stays visible
                        if (layoutAdPlaceholder != null) {
                            layoutAdPlaceholder.setVisibility(View.VISIBLE);
                        }
                    }
                });
                adView.loadAd(adRequest);
            }
        } catch (Throwable t) {
            // Safe fallback: never crash if ad service encounters issues
            if (layoutAdPlaceholder != null) {
                layoutAdPlaceholder.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
        if (!isRebooting) {
            resetButtonUI();
        }
    }

    @Override
    protected void onPause() {
        if (adView != null) {
            adView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

    private void startRebootProcess() {
        isRebooting = true;
        btnReboot.setEnabled(false);

        // Update UI to rebooting state
        ivRebootIcon.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        tvButtonLabel.setText(R.string.btn_rebooting);
        tvSubtitle.setText(R.string.subtitle_rebooting);

        // Execute multi-tier reboot process via RebootManager
        RebootManager.executeReboot(this, new RebootManager.RebootCallback() {
            @Override
            public void onRebootInitiated() {
                // UI updated
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
     * Shows Application Information & Legal popup
     */
    private void showAboutAppDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_about_title)
                .setMessage(R.string.dialog_about_message)
                .setPositiveButton(R.string.dialog_about_close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(R.mipmap.ic_launcher)
                .show();
    }

    /**
     * Guides user to enable Accessibility Service 1 time (No Root required)
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
                            Toast.makeText(MainActivity.this, "Please open Settings > Accessibility", Toast.LENGTH_LONG).show();
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
                .setTitle(R.string.error_root_title)
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

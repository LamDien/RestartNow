# RestartNow - Universal 1-Touch System Reboot Utility (v2.0)

Universal Android Native application for Android phones, tablets, and Automotive HeadUnits (Fully **NO ROOT REQUIRED**).

---

## 🌟 Key Features (Version 2.0)

* **1-Touch Fast Reboot:** Tap the prominent cockpit button to reboot the operating system immediately.
* **100% English Interface:** Standardized English UI across all dialogs, labels, and notifications.
* **Universal Screen Support (Adaptive Landscape & Portrait):**
  * **Landscape Mode:** Reboot controls aligned to the left, AdMob block container aligned to the right.
  * **Portrait Mode:** Reboot controls aligned to the top, AdMob block container aligned to the bottom.
* **AdMob Integration (Test ID Phase):**
  * Integrated Google Mobile Ads SDK with standard test unit `ca-app-pub-3940256099942544/6300978111`.
  * Cockpit-styled container with adaptive fallback placeholder card for offline environments.
* **App Info & Legal Popup:** Clickable `About RestartNow` link beneath the version footer displaying app specifications and open-source license info.
* **Dual-Action No-Root Architecture:**
  * Automotive hardware broadcasts for Rockchip PX5 / Microntek MTCE boards (`com.microntek.hctreboot`).
  * Non-intrusive Accessibility Service (`RebootAccessibilityService`) with dual gesture dispatch.
  * **1-Time Setup:** Remains permanently enabled across all subsequent reboots without re-activation.

---

## 📦 Release Installation File (APK)

Signed, production-ready APK:
```
bin/RestartNow.apk
```

---

## 🚗 Installation Guide

### Method 1: USB Flash Drive (Recommended)
1. Copy `bin/RestartNow.apk` to a USB drive (FAT32/exFAT).
2. Plug the USB into the vehicle's Android screen or connect to device.
3. Open **File Manager** $\rightarrow$ select USB $\rightarrow$ tap `RestartNow.apk`.
4. Tap **Install**.
5. On first launch, tap the center button. If prompted, tap **Open Settings** and switch **RestartNow** to ON under Accessibility (only done once).

### Method 2: ADB Wireless / USB Sideload
```bash
adb install -r bin/RestartNow.apk
```

---

## 🛠️ Build Commands
```bash
# Assemble Release Signed APK
./gradlew assembleRelease
```

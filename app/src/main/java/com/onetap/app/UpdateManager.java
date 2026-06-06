package com.onetap.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.io.File;

public class UpdateManager {

    private final Activity activity;
    private final FirebaseRemoteConfig remoteConfig;
    private DownloadReceiver downloadReceiver;

    public interface UpdateCheckCallback {
        void onUpdateAvailable(String downloadUrl);
        void onNoUpdate();
        void onError();
        void onUserMadeChoice(boolean shouldNavigate);
    }

    public UpdateManager(Activity activity) {
        this.activity = activity;
        this.remoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)
                .build();
        remoteConfig.setConfigSettingsAsync(configSettings);
    }

    public void checkForUpdate(UpdateCheckCallback callback) {
        remoteConfig.fetchAndActivate()
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        Log.d("UpdateCheck", "Config fetched successfully");
                        processUpdateCheck(callback);
                    } else {
                        Log.w("UpdateCheck", "Config fetch failed: " + task.getException());
                        if (callback != null) callback.onError();
                    }
                });
    }

    private void processUpdateCheck(UpdateCheckCallback callback) {
        long latestVersionCode = remoteConfig.getLong("latest_version_code");
        String updateUrl = remoteConfig.getString("update_url");
        int currentVersionCode = getCurrentVersionCode();

        Log.d("UpdateCheck", "Current: " + currentVersionCode + " | Latest: " + latestVersionCode);

        if (latestVersionCode > currentVersionCode) {
            Log.d("UpdateCheck", "⚠️ UPDATE AVAILABLE!");
            if (callback != null) callback.onUpdateAvailable(updateUrl);
        } else {
            Log.d("UpdateCheck", "✓ No update needed");
            if (callback != null) callback.onNoUpdate();
        }
    }

    private int getCurrentVersionCode() {
        try {
            String packageName = activity.getPackageName();
            PackageManager packageManager = activity.getPackageManager();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                PackageInfo packageInfo = packageManager.getPackageInfo(
                        packageName,
                        PackageManager.PackageInfoFlags.of(0)
                );
                return (int) packageInfo.getLongVersionCode();
            } else {
                PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
                return packageInfo.versionCode;
            }
        } catch (Exception e) {
            Log.e("UpdateCheck", "Error getting version code: " + e.getMessage());
            return 0;
        }
    }

    public void showUpdateDialog(String url, UpdateCheckCallback callback) {
        new AlertDialog.Builder(activity)
                .setTitle("Update Available 🚀")
                .setMessage("A new version of OneTap is available. Please update to continue.")
                .setPositiveButton("Update Now", (dialog, which) -> {
                    if (canInstallPackages(activity)) {
                        startDownload(url, callback);
                    } else {
                        requestInstallPermission(activity);
                        if (callback != null) callback.onUserMadeChoice(false);
                    }
                })
                .setNegativeButton("Later", (dialog, which) -> {
                    dialog.dismiss();
                    if (callback != null) callback.onUserMadeChoice(true);
                })
                .setCancelable(false)
                .show();
    }

    private boolean canInstallPackages(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return context.getPackageManager().canRequestPackageInstalls();
        } else {
            return true;
        }
    }

    private void requestInstallPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
            intent.setData(Uri.parse("package:" + activity.getPackageName()));
            activity.startActivity(intent);
            Toast.makeText(activity, "Please enable permission and try again", Toast.LENGTH_LONG).show();
        }
    }

    // ✅ Modified: Register Receiver & Start Download
    private void startDownload(String url, UpdateCheckCallback callback) {
        downloadReceiver = new DownloadReceiver(activity, callback);

        // Register receiver to listen for download completion
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.registerReceiver(downloadReceiver,
                    new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                    Context.RECEIVER_NOT_EXPORTED);
        } else {
            activity.registerReceiver(downloadReceiver,
                    new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        }

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
                .setTitle("Downloading OneTap Update...")
                .setDescription("New version is being downloaded")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "OneTap-Update.apk")
                .setAllowedOverRoaming(true)
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setMimeType("application/vnd.android.package-archive");

        DownloadManager downloadManager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
        long downloadId = downloadManager.enqueue(request);

        // Store download ID for receiver
        downloadReceiver.setDownloadId(downloadId);

        new AlertDialog.Builder(activity)
                .setTitle("Downloading...")
                .setMessage("Update is downloading. Installation will start automatically when complete.")
                .setPositiveButton("OK", null)
                .show();
    }

    // ✅ Clean up receiver when done
    public void cleanup() {
        if (downloadReceiver != null) {
            try {
                activity.unregisterReceiver(downloadReceiver);
            } catch (IllegalArgumentException e) {
                // Receiver already unregistered
            }
        }
    }

    // ✅ BroadcastReceiver to handle download completion
    public static class DownloadReceiver extends BroadcastReceiver {
        private final Activity activity;
        private final UpdateCheckCallback callback;
        private long downloadId;

        public DownloadReceiver(Activity activity, UpdateCheckCallback callback) {
            this.activity = activity;
            this.callback = callback;
        }

        public void setDownloadId(long downloadId) {
            this.downloadId = downloadId;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            long completedId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            if (completedId == downloadId) {
                Log.d("UpdateCheck", "Download complete! Starting installation...");

                // Unregister receiver
                try {
                    activity.unregisterReceiver(this);
                } catch (IllegalArgumentException e) {
                    // Already unregistered
                }

                // ✅ Trigger Installation Automatically
                installApk(context);

                // Notify callback
                if (callback != null) callback.onUserMadeChoice(false);
            }
        }

        private void installApk(Context context) {
            File apkFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "OneTap-Update.apk");

            if (apkFile.exists()) {
                Uri apkUri;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    // ✅ Android 7.0+: Use FileProvider for security
                    apkUri = FileProvider.getUriForFile(
                            context,
                            context.getPackageName() + ".fileprovider",
                            apkFile
                    );
                } else {
                    apkUri = Uri.fromFile(apkFile);
                }

                Intent installIntent = new Intent(Intent.ACTION_VIEW);
                installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                try {
                    context.startActivity(installIntent);
                } catch (Exception e) {
                    Log.e("UpdateCheck", "Installation failed: " + e.getMessage());
                    Toast.makeText(context, "Please install from notification", Toast.LENGTH_LONG).show();
                }
            } else {
                Log.e("UpdateCheck", "APK file not found!");
                Toast.makeText(context, "Download failed. Please try again.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
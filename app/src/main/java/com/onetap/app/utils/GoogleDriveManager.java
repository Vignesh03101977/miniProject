package com.onetap.app.utils;

import android.content.Context;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import java.util.Collections;

public class GoogleDriveManager {

    public static void uploadToDrive(Context context, java.io.File localFile, String subjectName) {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        if (account == null) return;

        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                context, Collections.singleton(DriveScopes.DRIVE_FILE));
        credential.setSelectedAccount(account.getAccount());

        Drive googleDriveService = new Drive.Builder(
                new NetHttpTransport(),
                new GsonFactory(),
                credential)
                .setApplicationName("OneTap Attendance")
                .build();

        new Thread(() -> {
            try {
                // Define XLSX Metadata
                File fileMetadata = new File();
                fileMetadata.setName(localFile.getName());
                fileMetadata.setDescription("Subject: " + subjectName);

                FileContent mediaContent = new FileContent(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        localFile
                );

                googleDriveService.files().create(fileMetadata, mediaContent).execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
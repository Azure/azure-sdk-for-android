/*
 * Licensed under Creative Commons Attribution-Share Alike: https://creativecommons.org/licenses/by-sa/4.0/.
 *
 * Code taken from:
 *
 * - DarwinLouis' (https://stackoverflow.com/users/1521424/darwinlouis) answer: https://stackoverflow.com/a/33298265/4967034
 * on StackOverflow question: https://stackoverflow.com/questions/33295300/how-to-get-absolute-path-in-android-for-file
 * by Vihang Patel (https://stackoverflow.com/users/5462990/vihang-patel), also edited by Unheilig (https://stackoverflow.com/users/2227834/unheilig).
 *
 * - coltoscosmin's (https://github.com/coltoscosmin) FileUtils class (https://github.com/coltoscosmin/FileUtils/blob/master/FileUtils.java)
 * on the following GitHub repository: https://github.com/coltoscosmin/FileUtils. Found said repository through
 * karanatwal.github.io's (https://stackoverflow.com/users/5996106/karanatwal-github-io) answer: https://stackoverflow.com/a/53021624/4967034
 * on StackOverflow question: https://stackoverflow.com/questions/30671548/unknown-url-content-downloads-my-downloads
 * by nekofar (https://stackoverflow.com/users/184605/nekofar), also edited by Vadim Kotov (https://stackoverflow.com/users/1000551/vadim-kotov).
 *
 * Modified only for compatibility and formatting purposes, core logic remains the same.
 */

package com.azure.android.storage.sample;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

class PathUtil {
    private static final String DOCUMENTS_DIR = "documents";
    private static final String TAG = "PathUtil";
    private static final boolean DEBUG = false; // Set to true to enable logging

    static String getPath(final Context context, final Uri uri) {
        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) { // ExternalStorageProvider
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                String storageDefinition;

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                } else {
                    if (Environment.isExternalStorageRemovable()) {
                        storageDefinition = "EXTERNAL_STORAGE";
                    } else {
                        storageDefinition = "SECONDARY_STORAGE";
                    }

                    return System.getenv(storageDefinition) + "/" + split[1];
                }
            } else if (isDownloadsDocument(uri)) { // DownloadsProvider
                final String id = DocumentsContract.getDocumentId(uri);

                if (id != null && id.startsWith("raw:")) {
                    return id.substring(4);
                }

                String[] contentUriPrefixesToTry = new String[]{
                    "content://downloads/public_downloads",
                    "content://downloads/my_downloads",
                    "content://downloads/all_downloads"
                };

                for (String contentUriPrefix : contentUriPrefixesToTry) {
                    Uri contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix),
                        Long.parseLong(id));

                    try {
                        String path = getDataColumn(context, contentUri, null, null);

                        if (path != null) {
                            return path;
                        }
                    } catch (Exception ex) {
                        // Ignored
                    }
                }

                // Path could not be retrieved using ContentResolver, therefore copy file to accessible cache using
                // streams
                String fileName = getFileName(context, uri);
                File cacheDir = getDocumentCacheDir(context);
                File file = generateFileName(fileName, cacheDir);
                String destinationPath = null;

                if (file != null) {
                    destinationPath = file.getAbsolutePath();

                    saveFileFromUri(context, uri, destinationPath);
                }

                return destinationPath;
            } else if (isMediaDocument(uri)) { // MediaProvider
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;

                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) { // MediaStore (and general)
            // Return the remote address
            if (isGooglePhotosUri(uri)) {
                return uri.getLastPathSegment();
            }

            return getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) { // File
            return uri.getPath();
        }

        return null;
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        final String column = "_data";
        final String[] projection = {column};

        try (Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);

                return cursor.getString(column_index);
            }
        }

        return null;
    }

    public static String getFileName(@NonNull Context context, Uri uri) {
        String mimeType = context.getContentResolver().getType(uri);
        String filename = null;

        if (mimeType == null && context != null) {
            String path = getPath(context, uri);
            if (path == null) {
                filename = getName(uri.toString());
            } else {
                File file = new File(path);
                filename = file.getName();
            }
        } else {
            Cursor returnCursor = context.getContentResolver().query(uri, null,
                null, null, null);
            if (returnCursor != null) {
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                returnCursor.moveToFirst();
                filename = returnCursor.getString(nameIndex);
                returnCursor.close();
            }
        }

        return filename;
    }

    private static String getName(String filename) {
        if (filename == null) {
            return null;
        }
        int index = filename.lastIndexOf('/');
        return filename.substring(index + 1);
    }

    private static File getDocumentCacheDir(@NonNull Context context) {
        File dir = new File(context.getCacheDir(), DOCUMENTS_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        logDir(context.getCacheDir());
        logDir(dir);

        return dir;
    }

    private static void logDir(File dir) {
        if (!DEBUG) {
            return;
        }

        Log.d(TAG, "Dir=" + dir);

        File[] files = dir.listFiles();

        for (File file : files) {
            Log.d(TAG, "File=" + file.getPath());
        }
    }

    @Nullable
    private static File generateFileName(@Nullable String name, File directory) {
        if (name == null) {
            return null;
        }

        File file = new File(directory, name);

        if (file.exists()) {
            String fileName = name;
            String extension = "";
            int dotIndex = name.lastIndexOf('.');

            if (dotIndex > 0) {
                fileName = name.substring(0, dotIndex);
                extension = name.substring(dotIndex);
            }

            int index = 0;

            while (file.exists()) {
                index++;
                name = fileName + '(' + index + ')' + extension;
                file = new File(directory, name);
            }
        }

        try {
            if (!file.createNewFile()) {
                return null;
            }
        } catch (IOException e) {
            Log.w(TAG, e);

            return null;
        }

        logDir(directory);

        return file;
    }

    private static void saveFileFromUri(Context context, Uri uri, String destinationPath) {
        InputStream is = null;
        BufferedOutputStream bos = null;

        try {
            is = context.getContentResolver().openInputStream(uri);
            bos = new BufferedOutputStream(new FileOutputStream(destinationPath, false));
            byte[] buf = new byte[1024];
            is.read(buf);

            do {
                bos.write(buf);
            } while (is.read(buf) != -1);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }

                if (bos != null) {
                    bos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}

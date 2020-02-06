package com.azure.andorid.storage.blob.upload;

import android.content.Context;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.azure.android.core.http.Callback;
import com.azure.android.storage.blob.StorageBlobClient;
import com.azure.android.storage.blob.credentials.SasTokenCredential;
import com.azure.android.storage.blob.implementation.Constants;
import com.azure.android.storage.blob.interceptor.SasTokenCredentialInterceptor;
import com.azure.android.storage.blob.models.BlockBlobItem;
import com.azure.android.storage.blob.upload.BlobUploadRecord;
import com.azure.android.storage.blob.upload.BlobUploader;
import com.azure.android.storage.blob.upload.BlockUploadRecord;
import com.azure.android.storage.blob.upload.UploadListener;
import com.azure.android.storage.blob.upload.UploadManager;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class StorageBlobClientTest {
    private final static String STORAGE_URL = "https://anustorageandroid.blob.core.windows.net/";
    private final static String DEFAULT_CONTAINER_NAME = "firstcontainer";
    private final static String SAS_TOKEN = "<SAS>";

    @Test
    public void uploadFileAsBlockBlob() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        AndroidThreeTen.init(appContext);

        final String fileName = "myFile.txt";
        final String blobName = "myFile.txt";
        File fileToUpload = createLocalFile(appContext, fileName, 22 * Constants.MB);
        StorageBlobClient blobClient = createStorageBlobClient();
        BlobUploadRecord blobUploadRecord
                = BlobUploadRecord.create(DEFAULT_CONTAINER_NAME, blobName, fileToUpload);
        List<BlockUploadRecord> blockUploadRecords = blobUploadRecord.getBlockUploadRecords();
        List<String> base64BlockIds = new ArrayList<>();
        //
        // Stage blocks in blob
        for (BlockUploadRecord record : blockUploadRecords) {
            byte[] content = record.getBlockContent();
            blobClient.stageBlock(blobUploadRecord.getContainerName(),
                    blobUploadRecord.getBlobName(),
                    record.getBlockId(),
                    content,
                    null);
            base64BlockIds.add(record.getBlockId());
        }
        //
        // Commit staged blocks
        blobClient.commitBlockList(blobUploadRecord.getContainerName(),
                blobUploadRecord.getBlobName(),
                base64BlockIds,
                true);
    }

    @Test
    public void uploadFileAsBlockBlobAsync() throws InterruptedException {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        AndroidThreeTen.init(appContext);

        final String fileName = "myFile.txt";
        final String blobName = "myFile.txt";
        File fileToUpload = createLocalFile(appContext, fileName, 22 * Constants.MB);
        StorageBlobClient blobClient = createStorageBlobClient();
        BlobUploadRecord blobUploadRecord
                = BlobUploadRecord.create(DEFAULT_CONTAINER_NAME, blobName, fileToUpload);
        List<BlockUploadRecord> blockUploadRecords = blobUploadRecord.getBlockUploadRecords();
        CountDownLatch blockUploadLatch = new CountDownLatch(blockUploadRecords.size());
        ConcurrentLinkedDeque<Throwable> blockUploadErrors = new ConcurrentLinkedDeque<>();
        List<String> base64BlockIds = new ArrayList<>();
        //
        // Stage blocks in blob
        for (BlockUploadRecord record : blockUploadRecords) {
            byte[] content = record.getBlockContent();
            blobClient.stageBlock(blobUploadRecord.getContainerName(),
                    blobUploadRecord.getBlobName(),
                    record.getBlockId(),
                    content,
                    null, new Callback<Void>() {
                        @Override
                        public void onResponse(Void response) {
                            Log.v("uploadFileAsBlockBlobAsync", "block upload succeeded. id:" + record.getBlockId());
                            base64BlockIds.add(record.getBlockId());
                            blockUploadLatch.countDown();
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            Log.e("uploadFileAsBlockBlobAsync",  "block upload failed.", t);
                            blockUploadErrors.add(t);
                            blockUploadLatch.countDown();
                        }
                    });
        }
        blockUploadLatch.await();
        if (blockUploadErrors.size() > 0) {
            StringBuilder builder = new StringBuilder();
            while (!blockUploadErrors.isEmpty()) {
                builder.append(blockUploadErrors.pop().getMessage());
                builder.append("\n");
            }
            assertTrue(builder.toString(), false);
        }
        assertEquals(3, base64BlockIds.size());

        CountDownLatch blocksCommitLatch = new CountDownLatch(1);
        Throwable[] blockCommitError = new Throwable[1];
        //
        // Commit staged blocks
        blobClient.commitBlockList(blobUploadRecord.getContainerName(),
                blobUploadRecord.getBlobName(),
                base64BlockIds,
                true, new Callback<BlockBlobItem>() {
                    @Override
                    public void onResponse(BlockBlobItem response) {
                        Log.v("uploadFileAsBlockBlobAsync", "blocks commit succeeded.");
                        blocksCommitLatch.countDown();
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.e("uploadFileAsBlockBlobAsync",  "blocks commit failed.", t);
                        blockCommitError[0] = t;
                        blocksCommitLatch.countDown();
                    }
                });
        blocksCommitLatch.await();
        if (blockCommitError[0] != null) {
            assertTrue(blockCommitError[0].getMessage(), false);
        }
    }

    @Test
    public void uploadManager() throws InterruptedException {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        AndroidThreeTen.init(appContext);

        final String fileName = "myFile.txt";
        final String blobName = "myFile.txt";

        File fileToUpload = createLocalFile(appContext, fileName, 22 * Constants.MB);
        UploadManager uploadManager = new UploadManager(createStorageBlobClient());
        CountDownLatch latch = new CountDownLatch(1);
        uploadManager.upload(DEFAULT_CONTAINER_NAME,
                blobName,
                fileToUpload, new UploadListener() {
                    @Override
                    public void onUploadProgress(int totalBytes, int bytesUploaded) {
                        Log.v("uploadManager", Thread.currentThread().getName());
                        Log.v("uploadManager", String.format("TotalBytes %d BytesUploaded: %d", totalBytes, bytesUploaded));
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.v("uploadManager", Thread.currentThread().getName());
                        Log.e("uploadManager", "Uploaded Failed", t);
                        latch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        Log.v("uploadManager", Thread.currentThread().getName());
                        Log.v("uploadManager", "Uploaded Completed");
                        latch.countDown();
                    }
                });
        latch.await();
    }

    @Test
    public void blobUploader() throws InterruptedException {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        AndroidThreeTen.init(appContext);

        final String fileName = "myFile.txt";
        final String blobName = "myFile.txt";
        File fileToUpload = createLocalFile(appContext, fileName, 22 * Constants.MB);
        BlobUploadRecord blobUploadRecord
                = BlobUploadRecord.create(DEFAULT_CONTAINER_NAME,
                blobName,
                fileToUpload);
        BlobUploader blobUploader = new BlobUploader(createStorageBlobClient(), 3);
        CountDownLatch latch = new CountDownLatch(1);
        blobUploader.upload(blobUploadRecord, new BlobUploader.Listener() {
            @Override
            public void onUploadProgress(int totalBytes, int bytesUploaded) {
                Log.v("blobUploader", String.format("TotalBytes %d BytesUploaded: %d", totalBytes, bytesUploaded));
            }

            @Override
            public void onError(Throwable t) {
                Log.e("blobUploader", "Uploaded Failed", t);
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                Log.v("blobUploader", "Uploaded Completed");
                latch.countDown();
            }
        });
        latch.await();
    }


    @Test
    public void blobUploadRecordsFromFile() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        byte[] fileContent = generateRandomBytes(22 * Constants.MB);
        final String fileName = "myFile.txt";
        File myFile = createLocalFile(appContext, fileName, fileContent);
        assertNotNull(myFile);
        assertEquals(fileContent.length, myFile.length());
        BlobUploadRecord blobUploadRecord
                = BlobUploadRecord.create("foo", "bar", myFile);
        List<BlockUploadRecord> blockRecords  = blobUploadRecord.getBlockUploadRecords();
        assertEquals(3, blockRecords.size());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (BlockUploadRecord record : blockRecords) {
            byte[] b = record.getBlockContent();
            outputStream.write(b, 0, b.length);
        }
        try {
            outputStream.flush();
        } catch (IOException e) {
            assertTrue(e.getMessage(), false);
        }
        assertArrayEquals(fileContent, outputStream.toByteArray());
    }

    private static StorageBlobClient createStorageBlobClient() {
        return new StorageBlobClient.Builder()
                .setBlobServiceUrl(STORAGE_URL)
                .setCredentialInterceptor(new SasTokenCredentialInterceptor(new SasTokenCredential(SAS_TOKEN)))
                .build();
    }

    private static File createLocalFile(Context appContext, String fileName, int fileSize) {
        return createLocalFile(appContext, fileName, generateRandomBytes(fileSize));
    }

    private static File createLocalFile(Context appContext, String fileName, byte [] fileContent) {
        String myFilePath = appContext.getExternalFilesDir(null).getAbsolutePath() + fileName;
        File myFile = null;
        try (FileOutputStream fileOutputStream = new FileOutputStream(myFilePath)) {
            fileOutputStream.write(fileContent, 0, fileContent.length);
            fileOutputStream.flush();
            myFile = new File(myFilePath);
        } catch (FileNotFoundException e) {
            assertTrue(e.getMessage(), false);
        } catch (IOException e) {
            assertTrue(e.getMessage(), false);
        }
        return myFile;
    }


    private static byte[] generateRandomBytes(int size) {
        long seed = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
        Random rand = new Random(seed);
        byte[] data = new byte[size];
        rand.nextBytes(data);
        return data;
    }
}

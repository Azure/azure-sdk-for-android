package com.azure.android.storage.blob.models;

import org.threeten.bp.OffsetDateTime;

/**
 * This class contains the properties about a block blob.
 */
public class BlockBlobItem {
    private final String eTag;
    private final OffsetDateTime lastModified;
    private final byte[] contentMd5;
    private final Boolean isServerEncrypted;
    private final String encryptionKeySha256;

    /**
     * Constructs a {@link BlockBlobItem}.
     *
     * @param eTag ETag of the block blob.
     * @param lastModified Last modified time of the block blob.
     * @param contentMd5 Content MD5 of the block blob.
     * @param isServerEncrypted Flag indicating if the page blob is encrypted on the server.
     * @param encryptionKeySha256 The encryption key used to encrypt the page blob.
     */
    public BlockBlobItem(final String eTag,
                         final OffsetDateTime lastModified,
                         final byte[] contentMd5,
                         final boolean isServerEncrypted,
                         final String encryptionKeySha256) {
        this.eTag = eTag;
        this.lastModified = lastModified;
        this.contentMd5 = clone(contentMd5);
        this.isServerEncrypted = isServerEncrypted;
        this.encryptionKeySha256 = encryptionKeySha256;
    }

    /**
     * @return the eTag of the block blob
     */
    public String getETag() {
        return eTag;
    }

    /**
     * @return the last time the block blob was modified
     */
    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    /**
     * @return the encryption status of the block blob on the server
     */
    public Boolean isServerEncrypted() {
        return isServerEncrypted;
    }

    /**
     * @return the key used to encrypt the block blob
     */
    public String getEncryptionKeySha256() {
        return encryptionKeySha256;
    }

    /**
     * @return the MD5 of the block blob's comment
     */
    public byte[] getContentMd5() {
        return clone(contentMd5);
    }

    /**
     * Creates a copy of the source byte array.
     * @param source Array to make copy of
     * @return A copy of the array, or null if source was null.
     */
    private static byte[] clone(byte[] source) {
        if (source == null) {
            return null;
        }
        byte[] copy = new byte[source.length];
        System.arraycopy(source, 0, copy, 0, source.length);
        return copy;
    }
}

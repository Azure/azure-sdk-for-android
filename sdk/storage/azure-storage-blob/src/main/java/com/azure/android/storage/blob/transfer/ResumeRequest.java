package com.azure.android.storage.blob.transfer;

import com.azure.android.core.util.CoreUtil;

public final class ResumeRequest {
    private final String transferId;

    private ResumeRequest(String transferId) {
        this.transferId = transferId;
    }

    String getTransferId() {
        return this.transferId;
    }

    public static final class Builder {
        private String transferId;

        public Builder() {
        }

        public Builder transferId(String transferId) {
            this.transferId = transferId;
            return this;
        }

        public ResumeRequest build() {
            if (CoreUtil.isNullOrEmpty(this.transferId)) {
                throw new IllegalArgumentException("'transferId' is required and cannot be null or empty.");
            }
            return new ResumeRequest(this.transferId);
        }
    }
}

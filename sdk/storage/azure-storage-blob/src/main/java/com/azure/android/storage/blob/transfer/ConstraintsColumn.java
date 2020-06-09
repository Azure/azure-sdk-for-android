// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

import android.os.Build;

import androidx.room.ColumnInfo;
import androidx.room.Ignore;
import androidx.work.NetworkType;

import java.util.Objects;

import static androidx.work.NetworkType.NOT_REQUIRED;

/**
 * Type representing constraints embedded in the {@link BlobUploadEntity} and {@link BlobDownloadEntity}.
 */
final class ConstraintsColumn {
    /**
     * Represents a Constraints column with no requirements.
     */
    @Ignore
    static final ConstraintsColumn NONE = new ConstraintsColumn(androidx.work.Constraints.NONE);

    @ColumnInfo(name = "required_network_type")
    public NetworkType requiredNetworkType = NOT_REQUIRED;

    @ColumnInfo(name = "requires_charging")
    public boolean requiresCharging;

    @ColumnInfo(name = "requires_device_idle")
    public boolean requiresDeviceIdle;

    @ColumnInfo(name = "requires_battery_not_low")
    public boolean requiresBatteryNotLow;

    @ColumnInfo(name = "requires_storage_not_low")
    public boolean requiresStorageNotLow;

    /**
     * Creates ConstraintsEntity, this constructor is used by Room library when re-hydrating metadata from local
     * store.
     */
    public ConstraintsColumn() {}

    /**
     * Create a new ConstraintsColumn to persist in local store.
     *
     * @param constraints the androidx work constraints
     */
    private ConstraintsColumn(androidx.work.Constraints constraints) {
        Objects.requireNonNull(constraints);

        this.requiredNetworkType = constraints.getRequiredNetworkType();
        this.requiresCharging = constraints.requiresCharging();
        if (Build.VERSION.SDK_INT >= 23) {
            this.requiresDeviceIdle = constraints.requiresDeviceIdle();
        }
        this.requiresBatteryNotLow = constraints.requiresBatteryNotLow();
        this.requiresStorageNotLow = constraints.requiresStorageNotLow();
    }

    /**
     * Create a {@link ConstraintsColumn} from the given constraints.
     *
     * @param constraints the androidx work constraints
     * @return the {@link ConstraintsColumn}
     */
    static ConstraintsColumn fromConstraints(androidx.work.Constraints constraints) {
        return new ConstraintsColumn(constraints);
    }

    /**
     * Creates {@link androidx.work.Constraints} from this {@link ConstraintsColumn}.
     *
     * @return the {@link androidx.work.Constraints}
     */
    androidx.work.Constraints toConstraints() {
        androidx.work.Constraints.Builder builder = new androidx.work.Constraints.Builder();
        builder.setRequiredNetworkType(this.requiredNetworkType);
        builder.setRequiresCharging(this.requiresCharging);
        if (Build.VERSION.SDK_INT >= 23) {
            builder.setRequiresDeviceIdle(this.requiresDeviceIdle);
        }
        builder.setRequiresBatteryNotLow(this.requiresBatteryNotLow);
        builder.setRequiresStorageNotLow(this.requiresStorageNotLow);
        return builder.build();
    }
}

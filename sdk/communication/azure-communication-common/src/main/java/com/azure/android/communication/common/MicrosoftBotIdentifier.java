// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.common;

/**
 * Communication identifier for Microsoft bots
 */
public final class MicrosoftBotIdentifier extends CommunicationIdentifier {
    private final String microsoftBotId;
    private final boolean isGlobal;
    private boolean rawIdSet = false;
    private CommunicationCloudEnvironment cloudEnvironment = CommunicationCloudEnvironment.PUBLIC;

    /**
     * Creates a MicrosoftBotIdentifier object
     *
     * @param microsoftBotId The unique Microsoft app ID for the bot as registered with the Bot Framework.
     * @param isGlobal Set this to true if the bot is global and false if the bot is tenantized.
     * @throws IllegalArgumentException thrown if microsoftBotId parameter fail the validation.
     */
    public MicrosoftBotIdentifier(String microsoftBotId, boolean isGlobal) {
        if (microsoftBotId == null || microsoftBotId.trim().length() == 0) {
            String error = "The initialization parameter [microsoftBotId] cannot be null or empty.";
            throw new IllegalArgumentException(error);
        }
        this.microsoftBotId = microsoftBotId;
        this.isGlobal = isGlobal;
        generateRawId();
    }

    /**
     * Creates a MicrosoftBotIdentifier object
     *
     * @param microsoftBotId The unique Microsoft app ID for the bot as registered with the Bot Framework.
     * @throws IllegalArgumentException thrown if microsoftBotId parameter fail the validation.
     */
    public MicrosoftBotIdentifier(String microsoftBotId) {
        this(microsoftBotId, false);
    }

    /**
     * Get the Microsoft app ID for the bot.
     * @return microsoftBotId Id of the Microsoft app ID for the bot.
     */
    public String getMicrosoftBotId() {
        return this.microsoftBotId;
    }

    /**
     * @return True if the bot is global and false if the bot is tenantized.
     */
    public boolean isGlobal() {
        return this.isGlobal;
    }

    /**
     * Set cloud environment of the Microsoft bot identifier, by default it is the Public cloud.
     * @param cloudEnvironment the cloud environment in which this identifier is created.
     * @return this object
     */
    public MicrosoftBotIdentifier setCloudEnvironment(CommunicationCloudEnvironment cloudEnvironment) {
        this.cloudEnvironment = cloudEnvironment;
        generateRawId();
        return this;
    }

    /**
     * Get cloud environment of the Microsoft bot identifier.
     * @return cloud environment in which this identifier is created.
     */
    public CommunicationCloudEnvironment getCloudEnvironment() {
        return cloudEnvironment;
    }

    /**
     * Set full id of the identifier
     * RawId is the encoded format for identifiers to store in databases or as stable keys in general.
     *
     * @param rawId full id of the identifier.
     * @return MicrosoftBotIdentifier object itself.
     */
    @Override
    public MicrosoftBotIdentifier setRawId(String rawId) {
        super.setRawId(rawId);
        rawIdSet = true;
        return this;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }

        if (!(that instanceof MicrosoftBotIdentifier)) {
            return false;
        }

        MicrosoftBotIdentifier thatId = (MicrosoftBotIdentifier) that;

        if (cloudEnvironment != null && !cloudEnvironment.equals(thatId.cloudEnvironment)) {
            return false;
        }

        if (thatId.cloudEnvironment != null && !thatId.cloudEnvironment.equals(this.cloudEnvironment)) {
            return false;
        }

        return getRawId() == null
            || thatId.getRawId() == null
            || thatId.getRawId().equals(this.getRawId());
    }


    @Override
    public int hashCode() {
        return getRawId().hashCode();
    }

    private void generateRawId() {
        if (!rawIdSet) {
            if (this.isGlobal) {
                if (cloudEnvironment.equals(CommunicationCloudEnvironment.DOD)) {
                    super.setRawId(BOT_DOD_GLOBAL_PREFIX + this.microsoftBotId);
                } else if (cloudEnvironment.equals(CommunicationCloudEnvironment.GCCH)) {
                    super.setRawId(BOT_GCCH_GLOBAL_PREFIX + this.microsoftBotId);
                } else {
                    super.setRawId(BOT_PREFIX + this.microsoftBotId);
                }
            } else {
                if (cloudEnvironment.equals(CommunicationCloudEnvironment.DOD)) {
                    super.setRawId(BOT_DOD_PREFIX + this.microsoftBotId);
                } else if (cloudEnvironment.equals(CommunicationCloudEnvironment.GCCH)) {
                    super.setRawId(BOT_GCCH_PREFIX + this.microsoftBotId);
                } else {
                    super.setRawId(BOT_PUBLIC_PREFIX + this.microsoftBotId);
                }
            }
        }
    }
}

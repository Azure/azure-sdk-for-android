// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.implementation.notifications.signaling;

public class RealtimeNotificationConfig {
    private String trouterServiceUrl;

    private String registrarServiceUrl;

    private String cloudType;

    // Default constructor needed for Jackson deserialization
    public RealtimeNotificationConfig() {
    }

    // Constructor to initialize all fields
    public RealtimeNotificationConfig(String trouterServiceUrl, String registrarServiceUrl, String cloudType) {
        this.trouterServiceUrl = trouterServiceUrl;
        this.registrarServiceUrl = registrarServiceUrl;
        this.cloudType = cloudType;
    }

    // Getter for trouterServiceUrl
    public String getTrouterServiceUrl() {
        return trouterServiceUrl;
    }

    // Getter for registrarServiceUrl
    public String getRegistrarServiceUrl() {
        return registrarServiceUrl;
    }

    // Getter for cloudType
    public String getCloudType() {
        return cloudType;
    }
}

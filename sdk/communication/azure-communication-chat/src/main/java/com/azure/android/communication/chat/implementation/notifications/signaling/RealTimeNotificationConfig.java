package com.azure.android.communication.chat.implementation.notifications.signaling;

public class RealTimeNotificationConfig {
    public String trouterServiceUrl;

    public String registrarServiceUrl;

    public String cloudType;

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

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.implementation.converters;

import android.text.TextUtils;

import com.azure.android.communication.chat.implementation.models.PhoneNumberIdentifierModel;
import com.azure.android.communication.chat.implementation.models.CommunicationCloudEnvironmentModel;
import com.azure.android.communication.chat.implementation.models.CommunicationIdentifierModel;
import com.azure.android.communication.chat.implementation.models.CommunicationUserIdentifierModel;
import com.azure.android.communication.chat.implementation.models.MicrosoftTeamsUserIdentifierModel;

import com.azure.android.communication.common.CommunicationCloudEnvironment;
import com.azure.android.communication.common.CommunicationIdentifier;
import com.azure.android.communication.common.CommunicationUserIdentifier;
import com.azure.android.communication.common.MicrosoftTeamsUserIdentifier;
import com.azure.android.communication.common.PhoneNumberIdentifier;
import com.azure.android.communication.common.UnknownIdentifier;
import com.azure.android.core.logging.ClientLogger;

import java.util.ArrayList;

public class CommunicationIdentifierConverter {
    /**
     * Convert CommunicationIdentifierModel into CommunicationIdentifier
     * @param identifier CommunicationIdentifierModel to be converted
     * @return CommunicationIdentifier
     */
    public static CommunicationIdentifier convert(CommunicationIdentifierModel identifier, ClientLogger logger) {
        assertSingleType(identifier);
        String rawId = identifier.getRawId();

        if (identifier.getCommunicationUser() != null) {
            final String userId = identifier.getCommunicationUser().getId();
            if (userId == null) {
                throw logger.logExceptionAsError(
                    new NullPointerException("CommunicationIdentifierModel.CommunicationUserIdentifierModel.id"));
            }
            return new CommunicationUserIdentifier(userId);
        }

        if (identifier.getPhoneNumber() != null) {
            PhoneNumberIdentifierModel phoneNumberModel = identifier.getPhoneNumber();
            if (phoneNumberModel.getValue() == null) {
                throw logger.logExceptionAsError(
                    new NullPointerException("CommunicationIdentifierModel.PhoneNumberIdentifierModel.value"));
            }
            return new PhoneNumberIdentifier(phoneNumberModel.getValue()).setRawId(rawId);
        }

        if (identifier.getMicrosoftTeamsUser() != null) {
            MicrosoftTeamsUserIdentifierModel teamsUserIdentifierModel = identifier.getMicrosoftTeamsUser();
            final String userId = teamsUserIdentifierModel.getUserId();
            if (userId == null) {
                throw logger.logExceptionAsError(
                    new NullPointerException("CommunicationIdentifierModel.MicrosoftTeamsUserIdentifierModel.userId"));
            }
            final CommunicationCloudEnvironmentModel cloud = teamsUserIdentifierModel.getCloud();
            if (cloud == null) {
                throw logger.logExceptionAsError(
                    new NullPointerException("CommunicationIdentifierModel.MicrosoftTeamsUserIdentifierModel.cloud"));
            }
            if (rawId == null) {
                throw logger.logExceptionAsError(
                    new NullPointerException("CommunicationIdentifierModel.rawId"));
            }
            return new MicrosoftTeamsUserIdentifier(userId,
                teamsUserIdentifierModel.isAnonymous())
                .setRawId(rawId)
                .setCloudEnvironment(CommunicationCloudEnvironment.fromString(cloud.toString()));
        }

        if (rawId == null) {
            throw logger.logExceptionAsError(
                new NullPointerException("CommunicationIdentifierModel.rawId"));
        }
        return new UnknownIdentifier(rawId);
    }

    private static void assertSingleType(CommunicationIdentifierModel identifier) {
        CommunicationUserIdentifierModel communicationUser = identifier.getCommunicationUser();
        PhoneNumberIdentifierModel phoneNumber = identifier.getPhoneNumber();
        MicrosoftTeamsUserIdentifierModel microsoftTeamsUser = identifier.getMicrosoftTeamsUser();

        ArrayList<String> presentProperties = new ArrayList<String>();
        if (communicationUser != null) {
            presentProperties.add(communicationUser.getClass().getName());
        }
        if (phoneNumber != null) {
            presentProperties.add(phoneNumber.getClass().getName());
        }
        if (microsoftTeamsUser != null) {
            presentProperties.add(microsoftTeamsUser.getClass().getName());
        }

        if (presentProperties.size() > 1) {
            throw new IllegalArgumentException(String.format("Only one of the identifier models in %s should be present.",
                TextUtils.join(", ", presentProperties)));
        }
    }

    /**
     * Convert CommunicationIdentifier into CommunicationIdentifierModel
     * @param identifier CommunicationIdentifier object to be converted
     * @return CommunicationIdentifierModel
     * @throws IllegalArgumentException when identifier is an unknown class derived from
     *          CommunicationIdentifier
     */
    public static CommunicationIdentifierModel convert(CommunicationIdentifier identifier, ClientLogger logger)
        throws IllegalArgumentException {

        if (identifier instanceof CommunicationUserIdentifier) {
            return new CommunicationIdentifierModel()
                .setCommunicationUser(
                    new CommunicationUserIdentifierModel().setId(((CommunicationUserIdentifier) identifier).getId()));
        }

        if (identifier instanceof PhoneNumberIdentifier) {
            PhoneNumberIdentifier phoneNumberIdentifier = (PhoneNumberIdentifier) identifier;
            return new CommunicationIdentifierModel()
                .setRawId(phoneNumberIdentifier.getRawId())
                .setPhoneNumber(new PhoneNumberIdentifierModel().setValue(phoneNumberIdentifier.getPhoneNumber()));
        }

        if (identifier instanceof MicrosoftTeamsUserIdentifier) {
            MicrosoftTeamsUserIdentifier teamsUserIdentifier = (MicrosoftTeamsUserIdentifier) identifier;
            return new CommunicationIdentifierModel()
                .setRawId(teamsUserIdentifier.getRawId())
                .setMicrosoftTeamsUser(new MicrosoftTeamsUserIdentifierModel()
                    .setIsAnonymous(teamsUserIdentifier.isAnonymous())
                    .setUserId(teamsUserIdentifier.getUserId())
                    .setCloud(CommunicationCloudEnvironmentModel.fromString(
                        teamsUserIdentifier.getCloudEnvironment().toString())));
        }

        if (identifier instanceof UnknownIdentifier) {
            return new CommunicationIdentifierModel()
                .setRawId(((UnknownIdentifier) identifier).getId());
        }

        throw new IllegalArgumentException(String.format("Unknown identifier class '%s'", identifier.getClass().getName()));
    }

}
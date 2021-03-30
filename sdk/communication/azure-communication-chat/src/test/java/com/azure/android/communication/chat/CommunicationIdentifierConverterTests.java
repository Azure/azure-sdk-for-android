// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat;

import com.azure.android.communication.chat.implementation.converters.CommunicationIdentifierConverter;
import com.azure.android.communication.chat.implementation.models.CommunicationIdentifierModel;
import com.azure.android.communication.chat.implementation.models.CommunicationUserIdentifierModel;
import com.azure.android.communication.chat.implementation.models.MicrosoftTeamsUserIdentifierModel;
import com.azure.android.communication.chat.implementation.models.PhoneNumberIdentifierModel;
import com.azure.android.communication.chat.implementation.models.CommunicationCloudEnvironmentModel;
import com.azure.android.communication.common.CommunicationCloudEnvironment;
import com.azure.android.communication.common.CommunicationIdentifier;
import com.azure.android.communication.common.CommunicationUserIdentifier;
import com.azure.android.communication.common.MicrosoftTeamsUserIdentifier;
import com.azure.android.communication.common.PhoneNumberIdentifier;
import com.azure.android.communication.common.UnknownIdentifier;
import com.azure.android.core.logging.ClientLogger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CommunicationIdentifierConverterTests {
    private ClientLogger logger = new ClientLogger(CommunicationIdentifierConverterTests.class);

    final String someId = "some id";
    final String teamsUserId = "Teams user id";
    final String rawId = "some lengthy id string";
    final String testPhoneNumber = "+12223334444";

    @Test
    public void deserializerThrowsWhenMoreThanOneNestedObjectsSet() {
        CommunicationIdentifierModel[] modelsWithTooManyNestedObjects = new CommunicationIdentifierModel[] {
            new CommunicationIdentifierModel()
                .setRawId(rawId)
                .setCommunicationUser(new CommunicationUserIdentifierModel()
                    .setId(someId))
                .setPhoneNumber(new PhoneNumberIdentifierModel()
                .setValue(testPhoneNumber)),
            new CommunicationIdentifierModel()
                .setRawId(rawId)
                .setCommunicationUser(new CommunicationUserIdentifierModel()
                    .setId(someId))
                .setMicrosoftTeamsUser(new MicrosoftTeamsUserIdentifierModel()
                .setUserId(teamsUserId)
                .setIsAnonymous(true)
                .setCloud(CommunicationCloudEnvironmentModel.PUBLIC)),
            new CommunicationIdentifierModel()
                .setRawId(rawId)
                .setPhoneNumber(new PhoneNumberIdentifierModel()
                    .setValue(testPhoneNumber))
                .setMicrosoftTeamsUser(new MicrosoftTeamsUserIdentifierModel()
                .setUserId(teamsUserId)
                .setIsAnonymous(true)
                .setCloud(CommunicationCloudEnvironmentModel.PUBLIC))
        };

        Arrays.stream(modelsWithTooManyNestedObjects).forEach(identifierModel -> {
            assertThrows(IllegalArgumentException.class,
                () -> {
                    CommunicationIdentifierConverter.convert(identifierModel, logger);
                });
        });
    }

    @Test
    public void deserializerThrowsWhenMissingProperty() {
        CommunicationIdentifierModel[] modelsWithMissingMandatoryProperty = new CommunicationIdentifierModel[] {
            new CommunicationIdentifierModel(), // Missing RawId
            new CommunicationIdentifierModel().setRawId(rawId).setCommunicationUser(new CommunicationUserIdentifierModel()), // Missing Id
            new CommunicationIdentifierModel().setRawId(rawId).setPhoneNumber(new PhoneNumberIdentifierModel()), // Missing PhoneNumber
            new CommunicationIdentifierModel().setRawId(rawId).setMicrosoftTeamsUser(
                new MicrosoftTeamsUserIdentifierModel().setCloud(CommunicationCloudEnvironmentModel.PUBLIC)), // Missing userId
            new CommunicationIdentifierModel().setRawId(rawId).setMicrosoftTeamsUser(
                new MicrosoftTeamsUserIdentifierModel().setIsAnonymous(true).setCloud(CommunicationCloudEnvironmentModel.DOD)), // Missing UserId
            new CommunicationIdentifierModel().setRawId(rawId).setMicrosoftTeamsUser(
                new MicrosoftTeamsUserIdentifierModel().setUserId(teamsUserId).setIsAnonymous(true))
        };

        Arrays.stream(modelsWithMissingMandatoryProperty).forEach(identifierModel -> {
            assertThrows(NullPointerException.class,
                () -> {
                    CommunicationIdentifierConverter.convert(identifierModel, logger);
                });
        });
    }

    @Test
    public void serializeCommunicationUser() {
        CommunicationIdentifierModel model = CommunicationIdentifierConverter.convert(
            new CommunicationUserIdentifier(someId), logger);

        assertNotNull(model.getCommunicationUser());
        assertEquals(someId, model.getCommunicationUser().getId());
    }

    @Test
    public void deserializeCommunicationUser() {
        CommunicationIdentifier identifier = CommunicationIdentifierConverter.convert(
            new CommunicationIdentifierModel()
                .setCommunicationUser(new CommunicationUserIdentifierModel().setId(someId)), logger);

        assertEquals(identifier.getClass(), CommunicationUserIdentifier.class);
        assertEquals(someId, ((CommunicationUserIdentifier) identifier).getId());
    }

    @Test
    public void serializeUnknown() {
        CommunicationIdentifierModel model = CommunicationIdentifierConverter.convert(
            new UnknownIdentifier(someId), logger);

        assertEquals(someId, model.getRawId());
    }

    @Test
    public void deserializeUnknown() {
        CommunicationIdentifier unknownIdentifier = CommunicationIdentifierConverter.convert(
            new CommunicationIdentifierModel()
                .setRawId(rawId), logger);
        assertEquals(UnknownIdentifier.class, unknownIdentifier.getClass());
        assertEquals(rawId, ((UnknownIdentifier) unknownIdentifier).getId());
    }

    @Test
    public void serializeFutureTypeShouldThrow() {
        assertThrows(IllegalArgumentException.class,
            () -> {
                CommunicationIdentifierConverter.convert(
                    new CommunicationIdentifier() {
                        public String getId() {
                            return someId;
                        }
                    }, logger);
            });
    }

    @Test
    public void serializePhoneNumber() {
        final String phoneNumber = "+12223334444";
        CommunicationIdentifierModel model = CommunicationIdentifierConverter.convert(
            new PhoneNumberIdentifier(phoneNumber).setRawId(rawId), logger);

        assertNotNull(model.getPhoneNumber());
        assertEquals(phoneNumber, model.getPhoneNumber().getValue());
        assertEquals(rawId, model.getRawId());
    }

    @Test
    public void deserializePhoneNumber() {
        CommunicationIdentifier identifier = CommunicationIdentifierConverter.convert(
            new CommunicationIdentifierModel()
                .setRawId(rawId)
                .setPhoneNumber(new PhoneNumberIdentifierModel().setValue(testPhoneNumber)), logger);

        assertEquals(PhoneNumberIdentifier.class, identifier.getClass());
        assertEquals(testPhoneNumber, ((PhoneNumberIdentifier) identifier).getPhoneNumber());
        assertEquals(rawId, ((PhoneNumberIdentifier) identifier).getRawId());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void serializeMicrosoftTeamsUser(boolean isAnonymous) {
        CommunicationIdentifierModel model = CommunicationIdentifierConverter.convert(
            new MicrosoftTeamsUserIdentifier(teamsUserId, isAnonymous)
                .setRawId(rawId)
                .setCloudEnvironment(CommunicationCloudEnvironment.DOD), logger);

        assertNotNull(model.getMicrosoftTeamsUser());
        assertEquals(teamsUserId, model.getMicrosoftTeamsUser().getUserId());
        assertEquals(rawId, model.getRawId());
        assertEquals(CommunicationCloudEnvironmentModel.DOD, model.getMicrosoftTeamsUser().getCloud());
        assertEquals(isAnonymous, model.getMicrosoftTeamsUser().isAnonymous());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void deserializerMicrosoftTeamsUser(boolean isAnonymous) {
        MicrosoftTeamsUserIdentifier identifier = (MicrosoftTeamsUserIdentifier) CommunicationIdentifierConverter.convert(
            new CommunicationIdentifierModel()
                .setRawId(rawId)
                .setMicrosoftTeamsUser(new MicrosoftTeamsUserIdentifierModel()
                    .setUserId(teamsUserId).setIsAnonymous(isAnonymous).setCloud(CommunicationCloudEnvironmentModel.GCCH)), logger);

        assertEquals(MicrosoftTeamsUserIdentifier.class, identifier.getClass());
        assertEquals(teamsUserId, identifier.getUserId());
        assertEquals(rawId, identifier.getRawId());
        assertEquals(CommunicationCloudEnvironment.GCCH, identifier.getCloudEnvironment());
        assertEquals(isAnonymous, identifier.isAnonymous());
    }

}


// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.common;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.azure.android.communication.common.CommunicationCloudEnvironmentModel.PUBLIC;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class IdentifierSerialzationExceptionTests {
    static final String teamsUserId = "Teams user id";
    static final String rawId = "some lengthy id string";

    private static Stream<CommunicationIdentifierModel> CommunicationIdentifierModelSupplier() {
        return Stream.of(
            new CommunicationIdentifierModel(), // Missing RawId
            new CommunicationIdentifierModel().setRawId(rawId).setCommunicationUser(new CommunicationUserIdentifierModel()), // Missing Id
            new CommunicationIdentifierModel().setRawId(rawId).setPhoneNumber(new PhoneNumberIdentifierModel()), // Missing PhoneNumber
            new CommunicationIdentifierModel().setRawId(rawId).setMicrosoftTeamsUser(
                new MicrosoftTeamsUserIdentifierModel().setCloud(PUBLIC)), // Missing userId
            new CommunicationIdentifierModel().setRawId(rawId).setMicrosoftTeamsUser(
                new MicrosoftTeamsUserIdentifierModel().setIsAnonymous(true).setCloud(CommunicationCloudEnvironmentModel.DOD)), // Missing UserId
            new CommunicationIdentifierModel().setRawId(rawId).setMicrosoftTeamsUser(
                new MicrosoftTeamsUserIdentifierModel().setUserId(teamsUserId).setIsAnonymous(true)));
    }

    @ParameterizedTest
    @MethodSource("CommunicationIdentifierModelSupplier")
    public void throwsOnMissingProperty(CommunicationIdentifierModel identifierModel) {
        assertThrows(NullPointerException.class,
            () -> {
                CommunicationIdentifierSerializer.deserialize(identifierModel);
            });
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.common;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TeamsUserIdentifierSerializationTests {
    final String someId = "some id";
    final String teamsUserId = "Teams user id";
    final String rawId = "some lengthy id string";

    private static Stream<Boolean> isAnonymousSupplier() {
        return Stream.of(true, false);
    }

    @ParameterizedTest
    @MethodSource("isAnonymousSupplier")
    public void serializeMicrosoftTeamsUser(boolean isAnonymous) {
        CommunicationIdentifierModel model = CommunicationIdentifierSerializer.serialize(
            new MicrosoftTeamsUserIdentifier(teamsUserId, isAnonymous)
                .setRawId(rawId)
                .setCloudEnvironment(CommunicationCloudEnvironment.DOD));

        assertNotNull(model.getMicrosoftTeamsUser());
        assertEquals(teamsUserId, model.getMicrosoftTeamsUser().getUserId());
        assertEquals(rawId, model.getRawId());
        assertEquals(CommunicationCloudEnvironmentModel.DOD, model.getMicrosoftTeamsUser().getCloud());
        assertEquals(isAnonymous, model.getMicrosoftTeamsUser().isAnonymous());
    }

    @ParameterizedTest
    @MethodSource("isAnonymousSupplier")
    public void deserializeMicrosoftTeamsUser(boolean isAnonymous) {
        MicrosoftTeamsUserIdentifier identifier = (MicrosoftTeamsUserIdentifier) CommunicationIdentifierSerializer.deserialize(
            new CommunicationIdentifierModel()
                .setRawId(rawId)
                .setMicrosoftTeamsUser(new MicrosoftTeamsUserIdentifierModel()
                    .setUserId(teamsUserId).setIsAnonymous(isAnonymous).setCloud(CommunicationCloudEnvironmentModel.GCCH)));

        assertEquals(MicrosoftTeamsUserIdentifier.class, identifier.getClass());
        assertEquals(teamsUserId, identifier.getUserId());
        assertEquals(rawId, identifier.getRawId());
        assertEquals(CommunicationCloudEnvironment.GCCH, identifier.getCloudEnvironment());
        assertEquals(isAnonymous, identifier.isAnonymous());
    }
}

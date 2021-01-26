// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.common;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class teamsUserIdentifierSerializationTests {
    final String someId = "some id";
    final String teamsUserId = "Teams user id";
    final String fullId = "some lengthy id string";

    private final boolean isAnonymous;

    public teamsUserIdentifierSerializationTests(boolean isAnonymous) {
        this.isAnonymous = isAnonymous;
    }

    @Parameters
    public static List<Boolean> cases() {
        return Arrays.asList(true, false);
    }

    @Test
    public void serializeMicrosoftTeamsUser() {
        CommunicationIdentifierModel model = CommunicationIdentifierSerializer.serialize(
            new MicrosoftTeamsUserIdentifier(teamsUserId, isAnonymous)
                .setId(someId)
                .setCloudEnvironment(CommunicationCloudEnvironment.DOD));

        assertEquals(CommunicationIdentifierKind.MICROSOFT_TEAMS_USER, model.getKind());
        assertEquals(teamsUserId, model.getMicrosoftTeamsUserId());
        assertEquals(someId, model.getId());
        assertEquals(CommunicationCloudEnvironmentModel.DOD, model.getCloudEnvironmentModel());
        assertEquals(isAnonymous, model.isAnonymous());
    }

    @Test
    public void deserializerMicrosoftTeamsUser() {
        MicrosoftTeamsUserIdentifier identifier = (MicrosoftTeamsUserIdentifier) CommunicationIdentifierSerializer.deserialize(
            new CommunicationIdentifierModel()
                .setMicrosoftTeamsUserId(teamsUserId)
                .setId(someId)
                .setKind(CommunicationIdentifierKind.MICROSOFT_TEAMS_USER)
                .setCloudEnvironmentModel(CommunicationCloudEnvironmentModel.GCCH)
                .setIsAnonymous(isAnonymous));

        assertEquals(MicrosoftTeamsUserIdentifier.class, identifier.getClass());
        assertEquals(teamsUserId, identifier.getUserId());
        assertEquals(someId, identifier.getId());
        assertEquals(CommunicationCloudEnvironment.GCCH, identifier.getCloudEnvironment());
        assertEquals(isAnonymous, identifier.isAnonymous());
    }
}

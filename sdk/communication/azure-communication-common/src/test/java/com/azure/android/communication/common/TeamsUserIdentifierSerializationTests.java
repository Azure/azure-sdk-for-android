// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.common;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class TeamsUserIdentifierSerializationTests {
    final String someId = "some id";
    final String teamsUserId = "Teams user id";
    final String rawId = "some lengthy id string";

    private final boolean isAnonymous;

    public TeamsUserIdentifierSerializationTests(boolean isAnonymous) {
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
                .setRawId(rawId)
                .setCloudEnvironment(CommunicationCloudEnvironment.DOD));

        assertNotNull(model.getMicrosoftTeamsUser());
        assertEquals(teamsUserId, model.getMicrosoftTeamsUser().getUserId());
        assertEquals(rawId, model.getRawId());
        assertEquals(CommunicationCloudEnvironmentModel.DOD, model.getMicrosoftTeamsUser().getCloud());
        assertEquals(isAnonymous, model.getMicrosoftTeamsUser().isAnonymous());
    }

    @Test
    public void deserializeMicrosoftTeamsUser() {
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

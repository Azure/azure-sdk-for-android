// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.common;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

@RunWith(Parameterized.class)
public class IdentifierSerialzationExceptionTests {
    static final String someId = "some id";
    static final String teamsUserId = "Teams user id";
    static final String fullId = "some lengthy id string";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private final CommunicationIdentifierModel identifierModel;
    public IdentifierSerialzationExceptionTests(CommunicationIdentifierModel identifierModel) {
        this.identifierModel = identifierModel;
    }

    @Parameterized.Parameters
    public static List<CommunicationIdentifierModel> cases() {
        return Arrays.asList(new CommunicationIdentifierModel().setKind(CommunicationIdentifierKind.UNKNOWN), // Missing Id
            new CommunicationIdentifierModel().setKind(CommunicationIdentifierKind.COMMUNICATION_USER), // Missing Id
            new CommunicationIdentifierModel().setKind(CommunicationIdentifierKind.CALLING_APPLICATION), // Missing Id
            new CommunicationIdentifierModel().setKind(CommunicationIdentifierKind.PHONE_NUMBER)
                .setId(someId), // Missing PhoneNumber
            new CommunicationIdentifierModel().setKind(CommunicationIdentifierKind.PHONE_NUMBER)
                .setPhoneNumber("+12223334444"), // Missing Id
            new CommunicationIdentifierModel().setKind(CommunicationIdentifierKind.MICROSOFT_TEAMS_USER)
                .setId(someId)
                .setCloudEnvironmentModel(CommunicationCloudEnvironmentModel.PUBLIC), // Missing MicrosoftTeamsUserId
            new CommunicationIdentifierModel().setKind(CommunicationIdentifierKind.MICROSOFT_TEAMS_USER)
                .setId(someId)
                .setMicrosoftTeamsUserId(teamsUserId), // Missing Cloud
            new CommunicationIdentifierModel().setKind(CommunicationIdentifierKind.MICROSOFT_TEAMS_USER)
                .setCloudEnvironmentModel(CommunicationCloudEnvironmentModel.PUBLIC)
                .setMicrosoftTeamsUserId(teamsUserId) // Missing id
        );
    }

    @Test
    public void throwsOnMissingProperty() {
        expectedException.expect(is(instanceOf(NullPointerException.class)));
        CommunicationIdentifierSerializer.deserialize(identifierModel);
    }
}

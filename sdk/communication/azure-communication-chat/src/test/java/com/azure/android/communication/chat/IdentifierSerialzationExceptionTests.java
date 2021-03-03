// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat;

import com.azure.android.communication.chat.models.CommunicationCloudEnvironmentModel;
import com.azure.android.communication.chat.models.CommunicationIdentifierModel;
import com.azure.android.communication.chat.models.CommunicationUserIdentifierModel;
import com.azure.android.communication.chat.models.MicrosoftTeamsUserIdentifierModel;
import com.azure.android.communication.chat.models.PhoneNumberIdentifierModel;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

import static com.azure.android.communication.chat.models.CommunicationCloudEnvironmentModel.PUBLIC;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

@RunWith(Parameterized.class)
public class IdentifierSerialzationExceptionTests {
    static final String teamsUserId = "Teams user id";
    static final String rawId = "some lengthy id string";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private final CommunicationIdentifierModel identifierModel;
    public IdentifierSerialzationExceptionTests(CommunicationIdentifierModel identifierModel) {
        this.identifierModel = identifierModel;
    }

    @Parameterized.Parameters
    public static List<CommunicationIdentifierModel> cases() {
        return Arrays.asList(
            new CommunicationIdentifierModel(), // Missing RawId
            new CommunicationIdentifierModel().setRawId(rawId).setCommunicationUser(new CommunicationUserIdentifierModel()), // Missing Id
            new CommunicationIdentifierModel().setRawId(rawId).setPhoneNumber(new PhoneNumberIdentifierModel()), // Missing PhoneNumber
            new CommunicationIdentifierModel().setRawId(rawId).setMicrosoftTeamsUser(
                new MicrosoftTeamsUserIdentifierModel().setCloud(PUBLIC)), // Missing userId
            new CommunicationIdentifierModel().setRawId(rawId).setMicrosoftTeamsUser(
                new MicrosoftTeamsUserIdentifierModel().setIsAnonymous(true).setCloud(CommunicationCloudEnvironmentModel.DOD)), // Missing UserId
            new CommunicationIdentifierModel().setRawId(rawId).setMicrosoftTeamsUser(
                new MicrosoftTeamsUserIdentifierModel().setUserId(teamsUserId).setIsAnonymous(true))
        );
    }

    @Test
    public void throwsOnMissingProperty() {
        expectedException.expect(is(instanceOf(NullPointerException.class)));
        CommunicationIdentifierSerializer.deserialize(identifierModel);
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.common;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;

import static com.azure.android.communication.common.CommunicationCloudEnvironmentModel.PUBLIC;
import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class CommunicationIdentifierSerializerTests {

    final String someId = "some id";
    final String teamsUserId = "Teams user id";
    final String rawId = "some lengthy id string";
    final String testPhoneNumber = "+12223334444";

    @Test
    public void serializeCommunicationUser() {
        CommunicationIdentifierModel model = CommunicationIdentifierSerializer.serialize(
            new CommunicationUserIdentifier(someId));

        assertNotNull(model.getCommunicationUser());
        assertEquals(someId, model.getCommunicationUser().getId());
    }

    @Test
    public void deserializeCommunicationUser() {
        CommunicationIdentifier identifier = CommunicationIdentifierSerializer.deserialize(
            new CommunicationIdentifierModel()
                .setCommunicationUser(new CommunicationUserIdentifierModel().setId(someId)));

        assertEquals(identifier.getClass(), CommunicationUserIdentifier.class);
        assertEquals(someId, ((CommunicationUserIdentifier) identifier).getId());
    }

    @Test
    public void serializeUnknown() {
        CommunicationIdentifierModel model = CommunicationIdentifierSerializer.serialize(
            new UnknownIdentifier(someId));

        assertEquals(someId, model.getRawId());
    }

    @Test
    public void deserializeUnknown() {
        CommunicationIdentifier unknownIdentifier = CommunicationIdentifierSerializer.deserialize(
            new CommunicationIdentifierModel()
                .setRawId(rawId));
        assertEquals(UnknownIdentifier.class, unknownIdentifier.getClass());
        assertEquals(rawId, ((UnknownIdentifier) unknownIdentifier).getId());
    }

    @Test
    public void serializeFutureTypeShouldThrow() {
        assertThrows(IllegalArgumentException.class,
            () -> {
                CommunicationIdentifierSerializer.serialize(
                    new CommunicationIdentifier() {
                        public String getId() {
                            return someId;
                        }
                    });
            });
    }

    @Test
    public void serializePhoneNumber() {
        final String phoneNumber = "+12223334444";
        CommunicationIdentifierModel model = CommunicationIdentifierSerializer.serialize(
            new PhoneNumberIdentifier(phoneNumber).setRawId(rawId));

        assertNotNull(model.getPhoneNumber());
        assertEquals(phoneNumber, model.getPhoneNumber().getValue());
        assertEquals(rawId, model.getRawId());
    }

    @Test
    public void deserializePhoneNumber() {
        CommunicationIdentifier identifier = CommunicationIdentifierSerializer.deserialize(
            new CommunicationIdentifierModel()
                .setRawId(rawId)
                .setPhoneNumber(new PhoneNumberIdentifierModel().setValue(testPhoneNumber)));

        assertEquals(PhoneNumberIdentifier.class, identifier.getClass());
        assertEquals(testPhoneNumber, ((PhoneNumberIdentifier) identifier).getPhoneNumber());
        assertEquals(rawId, ((PhoneNumberIdentifier) identifier).getRawId());
    }
}

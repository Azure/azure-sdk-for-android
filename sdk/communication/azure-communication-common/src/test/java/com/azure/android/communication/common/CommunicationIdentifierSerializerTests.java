// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.common;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;

public class CommunicationIdentifierSerializerTests {

    final String someId = "some id";
    final String teamsUserId = "Teams user id";
    final String fullId = "some lengthy id string";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void serializeCommunicationUser() {
        CommunicationIdentifierModel model = CommunicationIdentifierSerializer.serialize(
            new CommunicationUserIdentifier(someId));

        assertEquals(CommunicationIdentifierKind.COMMUNICATION_USER, model.getKind());
        assertEquals(someId, model.getId());
    }

    @Test
    public void deserializeCommunicationUser() {
        CommunicationIdentifier identifier = CommunicationIdentifierSerializer.deserialize(
            new CommunicationIdentifierModel().setKind(CommunicationIdentifierKind.COMMUNICATION_USER)
                .setId(someId));

        CommunicationUserIdentifier expectedIdentifier = new CommunicationUserIdentifier(someId);

        assertEquals(identifier.getClass(), CommunicationUserIdentifier.class);
        assertEquals(expectedIdentifier.getId(), ((CommunicationUserIdentifier) identifier).getId());
    }

    @Test
    public void serializeUnknown() {
        CommunicationIdentifierModel model = CommunicationIdentifierSerializer.serialize(new UnknownIdentifier(someId));

        assertEquals(CommunicationIdentifierKind.UNKNOWN, model.getKind());
        assertEquals(someId, model.getId());
    }

    @Test
    public void deserializeUnknown() {
        CommunicationIdentifier unknownIdentifier = CommunicationIdentifierSerializer.deserialize(
            new CommunicationIdentifierModel().setKind(CommunicationIdentifierKind.UNKNOWN)
                .setId(someId));
        assertEquals(UnknownIdentifier.class, unknownIdentifier.getClass());
        assertEquals(someId, ((UnknownIdentifier) unknownIdentifier).getId());
    }

    @Test
    public void serializeFutureTypeShouldThrow() {
        expectedException.expect(is(instanceOf(IllegalArgumentException.class)));
        CommunicationIdentifierSerializer.serialize(
            new CommunicationIdentifier() {
                @Override
                public String getId() {
                    return someId;
                }
            });
    }

    @Test
    public void serializeCallingApplication() {
        CommunicationIdentifierModel model = CommunicationIdentifierSerializer.serialize(
            new CallingApplicationIdentifier(someId));

        assertEquals(CommunicationIdentifierKind.CALLING_APPLICATION, model.getKind());
        assertEquals(someId, model.getId());
    }

    @Test
    public void deserializeCallingApplication() {
        CommunicationIdentifier identifier = CommunicationIdentifierSerializer.deserialize(
            new CommunicationIdentifierModel()
                .setKind(CommunicationIdentifierKind.CALLING_APPLICATION)
                .setId(someId));

        assertEquals(CallingApplicationIdentifier.class, identifier.getClass());
        assertEquals(someId, ((CallingApplicationIdentifier) identifier).getId());
    }

    @Test
    public void serializePhoneNumber() {
        final String phoneNumber = "+12223334444";
        CommunicationIdentifierModel model = CommunicationIdentifierSerializer.serialize(
            new PhoneNumberIdentifier(phoneNumber).setId(someId));

        assertEquals(CommunicationIdentifierKind.PHONE_NUMBER, model.getKind());
        assertEquals(phoneNumber, model.getPhoneNumber());
        assertEquals(someId, model.getId());
    }

    @Test
    public void deserializePhoneNumber() {
        final String phoneNumber = "+12223334444";
        CommunicationIdentifier identifier = CommunicationIdentifierSerializer.deserialize(
            new CommunicationIdentifierModel().setKind(CommunicationIdentifierKind.PHONE_NUMBER)
                .setPhoneNumber(phoneNumber)
                .setId(someId));

        assertEquals(PhoneNumberIdentifier.class, identifier.getClass());
        assertEquals(phoneNumber, ((PhoneNumberIdentifier) identifier).getPhoneNumber());
        assertEquals(someId, identifier.getId());
    }

}

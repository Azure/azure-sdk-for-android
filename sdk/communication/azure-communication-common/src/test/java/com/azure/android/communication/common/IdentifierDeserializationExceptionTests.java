package com.azure.android.communication.common;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

import static com.azure.android.communication.common.CommunicationCloudEnvironmentModel.PUBLIC;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

@RunWith(Parameterized.class)
public class IdentifierDeserializationExceptionTests {
    static final String someId = "some id";
    static final String teamsUserId = "Teams user id";
    static final String rawId = "some lengthy id string";
    static final String testPhoneNumber = "+12223334444";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private final CommunicationIdentifierModel identifierModel;
    public IdentifierDeserializationExceptionTests(CommunicationIdentifierModel identifierModel) {
        this.identifierModel = identifierModel;
    }

    @Parameterized.Parameters
    public static List<CommunicationIdentifierModel> cases() {
        return Arrays.asList(new CommunicationIdentifierModel()
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
                    .setCloud(PUBLIC)),
            new CommunicationIdentifierModel()
                .setRawId(rawId)
                .setPhoneNumber(new PhoneNumberIdentifierModel()
                    .setValue(testPhoneNumber))
                .setMicrosoftTeamsUser(new MicrosoftTeamsUserIdentifierModel()
                    .setUserId(teamsUserId)
                    .setIsAnonymous(true)
                    .setCloud(PUBLIC)));
    }

    @Test
    public void throwsOnMoreThanOneNestedObject() {
        expectedException.expect(is(instanceOf(IllegalArgumentException.class)));
        CommunicationIdentifierSerializer.deserialize(identifierModel);
    }
}

package com.azure.android.communication.common;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.azure.android.communication.common.CommunicationCloudEnvironmentModel.PUBLIC;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class IdentifierDeserializationExceptionTests {
    static final String someId = "some id";
    static final String teamsUserId = "Teams user id";
    static final String rawId = "some lengthy id string";
    static final String testPhoneNumber = "+12223334444";

    private static Stream<CommunicationIdentifierModel> CommunicationIdentifierModelSupplier() {
        return Stream.of(new CommunicationIdentifierModel()
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

    @ParameterizedTest
    @MethodSource("CommunicationIdentifierModelSupplier")
    public void throwsOnMoreThanOneNestedObject(CommunicationIdentifierModel identifierModel) {
        assertThrows(IllegalArgumentException.class,
            () -> {
                CommunicationIdentifierSerializer.deserialize(identifierModel);
            });
    }
}

package com.azure.android.communication.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class CommunicationCloudEnvironmentTests {

    @Test
    public void constructor_defaultCloudIsPublic() {
        assertEquals(CommunicationCloudEnvironment.PUBLIC, new CommunicationCloudEnvironment());
    }

    @Test
    public void fromString_throwsForNullValue() {
        assertThrows(NullPointerException.class, () -> CommunicationCloudEnvironment.fromString(null));
    }

    @Test
    public void fromString_shouldCreateCloudEnvironment() {
        String testEnv = "test";
        CommunicationCloudEnvironment cloudEnvironment = CommunicationCloudEnvironment.fromString(testEnv);
        assertEquals(testEnv, cloudEnvironment.toString());
    }

    @Test
    public void toString_shouldReturnCloudEnvironmentValue() {
        assertEquals("test1", CommunicationCloudEnvironment.fromString("test1").toString());
        assertEquals("test2", CommunicationCloudEnvironment.fromString("test2").toString());
        assertEquals("test3", CommunicationCloudEnvironment.fromString("test3").toString());
    }

    @Test
    public void equals_shouldCompareEnvironmentValues() {
        assertEquals(CommunicationCloudEnvironment.PUBLIC, CommunicationCloudEnvironment.fromString("public"));
        assertEquals(CommunicationCloudEnvironment.DOD, CommunicationCloudEnvironment.fromString("dod"));
        assertEquals(CommunicationCloudEnvironment.GCCH, CommunicationCloudEnvironment.fromString("gcch"));
    }

}

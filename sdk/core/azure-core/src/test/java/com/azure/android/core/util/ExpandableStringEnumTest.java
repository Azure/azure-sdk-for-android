package com.azure.android.core.util;

import org.junit.Test;

import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ExpandableStringEnumTest {
    @Test
    public void createInstance_fromString() {
        assertNotNull(MyExpandableStringEnum.fromString("Test"));
    }

    @Test
    public void createInstance_fromNullString() {
        assertNull(MyExpandableStringEnum.fromString(null));
    }

    @Test
    public void getExistingInstance_fromString() {
        MyExpandableStringEnum myExpandableStringEnum = MyExpandableStringEnum.fromString("Test");

        assertSame(myExpandableStringEnum, MyExpandableStringEnum.fromString("Test"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwException_whenCallingFromString_withNullClass() {
        MyExpandableStringEnum.fromStringWithNullClass();
    }

    @Test
    public void returnNull_whenTryingToGetInstanceOfPrivateClass_fromString() {
        assertNull(MyPrivateExpandableStringEnum.fromString());
    }

    @Test
    public void getExistingInstances() {
        MyExpandableStringEnum myExpandableStringEnum = MyExpandableStringEnum.fromString("Test");
        MyExpandableStringEnum otherMyExpandableStringEnum = MyExpandableStringEnum.fromString("Another");
        Collection instances = MyExpandableStringEnum.values();

        assertFalse(instances.isEmpty());
        assertEquals(2, instances.size());

        Iterator iterator = instances.iterator();

        assertSame(myExpandableStringEnum, iterator.next());
        assertSame(otherMyExpandableStringEnum, iterator.next());
    }

    @Test
    public void convertToString() {
        assertEquals("Test", MyExpandableStringEnum.fromString("Test").toString());
    }

    @Test
    public void getHashCode() {
        assertEquals(-2043209345, MyExpandableStringEnum.fromString("Test").hashCode());
    }

    @SuppressWarnings("SimplifiableJUnitAssertion")
    @Test
    public void equalTo_identicalObject() {
        MyExpandableStringEnum myExpandableStringEnum = MyExpandableStringEnum.fromString("Test");
        MyExpandableStringEnum otherMyExpandableStringEnum = MyExpandableStringEnum.fromString("Test");

        assertTrue(myExpandableStringEnum.equals(otherMyExpandableStringEnum));
    }

    @SuppressWarnings("SimplifiableJUnitAssertion")
    @Test
    public void notEqualTo_differentObject() {
        MyExpandableStringEnum myExpandableStringEnum = MyExpandableStringEnum.fromString("Test");
        MyExpandableStringEnum otherMyExpandableStringEnum = MyExpandableStringEnum.fromString("Another");

        assertFalse(myExpandableStringEnum.equals(otherMyExpandableStringEnum));
    }

    @SuppressWarnings({"SimplifiableJUnitAssertion", "ConstantConditions"})
    @Test
    public void notEqualTo_nullObject() {
        MyExpandableStringEnum myExpandableStringEnum = MyExpandableStringEnum.fromString("Test");

        assertFalse(myExpandableStringEnum.equals(null));
    }

    @SuppressWarnings({"SimplifiableJUnitAssertion", "EqualsBetweenInconvertibleTypes"})
    @Test
    public void notEqualTo_objectFromDifferentClass() {
        MyExpandableStringEnum myExpandableStringEnum = MyExpandableStringEnum.fromString("Test");

        assertFalse(myExpandableStringEnum.equals(""));
    }

    static final class MyExpandableStringEnum extends ExpandableStringEnum<MyExpandableStringEnum> {
        static void fromStringWithNullClass() {
            fromString("Test", null);
        }

        static MyExpandableStringEnum fromString(String name) {
            return fromString(name, MyExpandableStringEnum.class);
        }

        static Collection<MyExpandableStringEnum> values() {
            return values(MyExpandableStringEnum.class);
        }
    }

    private static final class MyPrivateExpandableStringEnum extends ExpandableStringEnum<MyPrivateExpandableStringEnum> {
        static MyPrivateExpandableStringEnum fromString() {
            return fromString("", MyPrivateExpandableStringEnum.class);
        }
    }
}

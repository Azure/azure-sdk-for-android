package com.azure.android.core.internal.util.serializer;

import com.azure.android.core.internal.util.serializer.exception.MalformedValueException;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Headers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class JacksonAdapterTest {
    @Test
    public void test_createDefaultSerializerAdapter() {
        assertNotNull(JacksonAdapter.createDefaultSerializerAdapter());
    }

    @Test
    public void test_serializer() {
        JacksonAdapter jacksonAdapter = (JacksonAdapter) JacksonAdapter.createDefaultSerializerAdapter();

        assertNotNull(jacksonAdapter.serializer());
    }

    @Test
    public void serialize_withNoFormat() throws IOException {
        SerializerAdapter serializerAdapter = JacksonAdapter.createDefaultSerializerAdapter();

        assertEquals("{\"x\":1,\"y\":2}", serializerAdapter.serialize(new TestModel(1, 2), null));
    }

    @Test
    public void serialize_toJson() throws IOException {
        SerializerAdapter serializerAdapter = JacksonAdapter.createDefaultSerializerAdapter();

        assertEquals("{\"x\":1,\"y\":2}", serializerAdapter.serialize(new TestModel(1, 2), SerializerFormat.JSON));
    }

    @Test
    public void serialize_toXml() throws IOException {
        SerializerAdapter serializerAdapter = JacksonAdapter.createDefaultSerializerAdapter();

        assertEquals("<?xml version='1.0' encoding='UTF-8'?><TestModel><x>1</x><y>2</y></TestModel>",
            serializerAdapter.serialize(new TestModel(1, 2), SerializerFormat.XML));
    }

    @Test
    public void serialize_nullObject() throws IOException {
        SerializerAdapter serializerAdapter = JacksonAdapter.createDefaultSerializerAdapter();

        assertNull(serializerAdapter.serialize(null, null));
    }

    @Test
    public void serializeList_withCommaSeparatedValues() {
        SerializerAdapter serializerAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        List<TestModel> list = new ArrayList<>();
        list.add(new TestModel(1, 2));
        list.add(new TestModel(3, 4));
        list.add(new TestModel(5, 6));

        assertEquals("{\"x\":1,\"y\":2},{\"x\":3,\"y\":4},{\"x\":5,\"y\":6}",
            serializerAdapter.serializeList(list, SerializerAdapter.CollectionFormat.CSV));
    }

    @Test
    public void serializeList_withSpaceSeparatedValues() {
        SerializerAdapter serializerAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        List<TestModel> list = new ArrayList<>();
        list.add(new TestModel(1, 2));
        list.add(new TestModel(3, 4));
        list.add(new TestModel(5, 6));

        assertEquals("{\"x\":1,\"y\":2} {\"x\":3,\"y\":4} {\"x\":5,\"y\":6}",
            serializerAdapter.serializeList(list, SerializerAdapter.CollectionFormat.SSV));
    }

    @Test
    public void serializeList_withTabSeparatedValues() {
        SerializerAdapter serializerAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        List<TestModel> list = new ArrayList<>();
        list.add(new TestModel(1, 2));
        list.add(new TestModel(3, 4));
        list.add(new TestModel(5, 6));

        assertEquals("{\"x\":1,\"y\":2} {\"x\":3,\"y\":4} {\"x\":5,\"y\":6}",
            serializerAdapter.serializeList(list, SerializerAdapter.CollectionFormat.SSV));
    }

    @Test
    public void serializeList_withPipeSeparatedValues() {
        SerializerAdapter serializerAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        List<TestModel> list = new ArrayList<>();
        list.add(new TestModel(1, 2));
        list.add(new TestModel(3, 4));
        list.add(new TestModel(5, 6));

        assertEquals("{\"x\":1,\"y\":2}|{\"x\":3,\"y\":4}|{\"x\":5,\"y\":6}",
            serializerAdapter.serializeList(list, SerializerAdapter.CollectionFormat.PIPES));
    }

    @Test
    public void serializeList_asMultipleParameters() {
        SerializerAdapter serializerAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        List<TestModel> list = new ArrayList<>();
        list.add(new TestModel(1, 2));
        list.add(new TestModel(3, 4));
        list.add(new TestModel(5, 6));

        assertEquals("{\"x\":1,\"y\":2}&{\"x\":3,\"y\":4}&{\"x\":5,\"y\":6}",
            serializerAdapter.serializeList(list, SerializerAdapter.CollectionFormat.MULTI));
    }

    @Test
    public void serializeList_nullObject() throws IOException {
        SerializerAdapter serializerAdapter = JacksonAdapter.createDefaultSerializerAdapter();

        assertNull(serializerAdapter.serializeList(null, null));
    }

    @Test
    public void deserialize_withNoFormat() throws IOException {
        SerializerAdapter serializerAdapter = JacksonAdapter.createDefaultSerializerAdapter();

        assertEquals(new TestModel(1, 2), serializerAdapter.deserialize("{\"x\": 1, \"y\": 2}",
            TestModel.class, null));
    }

    @Test
    public void deserialize_json() throws IOException {
        SerializerAdapter serializerAdapter = JacksonAdapter.createDefaultSerializerAdapter();

        assertEquals(new TestModel(1, 2), serializerAdapter.deserialize("{\"x\": 1, \"y\": 2}",
            TestModel.class, SerializerFormat.JSON));
    }

    @Test
    public void deserialize_xml() throws IOException {
        SerializerAdapter serializerAdapter = JacksonAdapter.createDefaultSerializerAdapter();

        assertEquals(new TestModel(1, 2), serializerAdapter.deserialize("<TestModel><x>1</x><y>2</y></TestModel>",
            TestModel.class, SerializerFormat.XML));
    }

    @Test
    public void deserialize_nullObject() throws IOException {
        SerializerAdapter serializerAdapter = JacksonAdapter.createDefaultSerializerAdapter();

        assertNull(serializerAdapter.deserialize(null, TestModel.class, SerializerFormat.XML));
    }

    @Test
    public void deserialize_stringStartingWithByteOrderMark() throws IOException {
        SerializerAdapter serializerAdapter = JacksonAdapter.createDefaultSerializerAdapter();

        assertEquals(new TestModel(1, 2), serializerAdapter.deserialize("\uFEFF{\"x\": 1, \"y\": 2}",
            TestModel.class, null));
    }

    @Test(expected = MalformedValueException.class)
    public void deserialize_malformedJson() throws IOException {
        SerializerAdapter serializerAdapter = JacksonAdapter.createDefaultSerializerAdapter();

        assertEquals(new TestModel(1, 2), serializerAdapter.deserialize("TestModel{\"x\": 1, \"y\": 2}}",
            TestModel.class, null));
    }

    @Test
    public void deserialize_headers() throws IOException {
        SerializerAdapter serializerAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        Headers headers = new Headers.Builder()
            .set("x", "1")
            .set("y", "2")
            .build();

        assertEquals(new TestModel(1, 2), serializerAdapter.deserialize(headers, TestModel.class));
    }

    @Test
    public void deserialize_headersWithHeaderCollection() throws IOException {
        SerializerAdapter serializerAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        Headers headers = new Headers.Builder()
            .set("firstHeader", "first value")
            .set("secondHeader-one", "1")
            .set("secondHeader-two", "2")
            .set("secondHeader-three", "3")
            .build();

        Map<String, String> secondHeaderMap = new HashMap<>();
        secondHeaderMap.put("secondHeader-one", "1");
        secondHeaderMap.put("secondHeader-two", "2");
        secondHeaderMap.put("secondHeader-three", "3");

        assertEquals(new TestHeaders("first value", secondHeaderMap),
            serializerAdapter.deserialize(headers, TestHeaders.class));
    }

    @Test
    public void deserialize_nullHeaders() throws IOException {
        SerializerAdapter serializerAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        Headers headers = new Headers.Builder()
            .set("firstHeader", "first value")
            .set("secondHeader", "second value")
            .build();

        assertNull(serializerAdapter.deserialize(headers, null));
    }
}

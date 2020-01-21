// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.implementation.util.serializer;

import com.azure.android.core.annotation.JsonFlatten;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom serializer for deserializing complex types with wrapped properties. For example, a property with annotation
 * {@code @JsonProperty(value = "properties.name")} will be mapped to a top level "name" property in the POJO model.
 */
final class FlatteningDeserializer extends StdDeserializer<Object> implements ResolvableDeserializer {
    private static final long serialVersionUID = -2133095337545715498L;

    /**
     * The default mapperAdapter for the current type.
     */
    private final JsonDeserializer<?> defaultDeserializer;

    /**
     * The object mapper for default deserializations.
     */
    private final ObjectMapper mapper;

    /**
     * Creates an instance of FlatteningDeserializer.
     *
     * @param vc                  Handled type.
     * @param defaultDeserializer The default JSON mapperAdapter.
     * @param mapper              The object mapper for default deserializations.
     */
    protected FlatteningDeserializer(Class<?> vc, JsonDeserializer<?> defaultDeserializer, ObjectMapper mapper) {
        super(vc);

        this.defaultDeserializer = defaultDeserializer;
        this.mapper = mapper;
    }

    /**
     * Gets a module wrapping this serializer as an adapter for the Jackson {@link ObjectMapper}.
     *
     * @param mapper The object mapper for default deserializations.
     * @return A simple module to be plugged onto Jackson {@link ObjectMapper}.
     */
    public static SimpleModule getModule(final ObjectMapper mapper) {
        SimpleModule module = new SimpleModule();

        module.setDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc,
                                                          JsonDeserializer<?> deserializer) {
                if (beanDesc.getBeanClass().getAnnotation(JsonFlatten.class) != null) {
                    return new FlatteningDeserializer(beanDesc.getBeanClass(), deserializer, mapper);
                }

                return deserializer;
            }
        });

        return module;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
        JsonNode root = mapper.readTree(jsonParser);
        final Class<?> tClass = this.defaultDeserializer.handledType();

        for (Class<?> c : getAllClasses(tClass)) {
            // Ignore checks for Object type.
            if (c.isAssignableFrom(Object.class)) {
                continue;
            }

            for (Field field : c.getDeclaredFields()) {
                JsonNode node = root;
                JsonProperty property = field.getAnnotation(JsonProperty.class);

                if (property != null) {
                    String value = property.value();

                    if (value.matches(".+[^\\\\]\\..+")) {
                        String[] values = value.split("((?<!\\\\))\\.");

                        for (String val : values) {
                            val = val.replace("\\.", ".");
                            node = node.get(val);

                            if (node == null) {
                                break;
                            }
                        }

                        ((ObjectNode) root).put(value, node);
                    }
                }
            }
        }

        JsonParser parser = new JsonFactory().createParser(root.toString());

        parser.nextToken();

        return defaultDeserializer.deserialize(parser, context);
    }

    @Override
    public void resolve(DeserializationContext context) throws JsonMappingException {
        ((ResolvableDeserializer) defaultDeserializer).resolve(context);
    }

    /**
     * Find all super classes including the provided class.
     *
     * @param clazz The raw class to find super types for.
     * @return The list of super classes.
     */
    private static List<Class<?>> getAllClasses(Class<?> clazz) {
        List<Class<?>> types = new ArrayList<>();

        while (clazz != null) {
            types.add(clazz);
            clazz = clazz.getSuperclass();
        }

        return types;
    }
}

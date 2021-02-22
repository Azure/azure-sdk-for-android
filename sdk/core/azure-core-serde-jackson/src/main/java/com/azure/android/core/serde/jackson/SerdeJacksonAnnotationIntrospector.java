// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.serde.jackson;

import com.azure.android.core.serde.SerdeFromPojo;
import com.azure.android.core.serde.SerdeGetKeyValues;
import com.azure.android.core.serde.SerdeIgnoreProperty;
import com.azure.android.core.serde.SerdeProperty;
import com.azure.android.core.serde.SerdePropertyAlias;
import com.azure.android.core.serde.SerdeSetKeyValues;
import com.azure.android.core.serde.SerdeSetter;
import com.azure.android.core.serde.SerdeSubTypes;
import com.azure.android.core.serde.SerdeToPojo;
import com.azure.android.core.serde.SerdeTypeInfo;
import com.azure.android.core.serde.SerdeTypeName;
import com.azure.android.core.serde.SerdeXmlProperty;
import com.azure.android.core.serde.SerdeXmlRootElement;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlAnnotationIntrospector;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link JacksonAnnotationIntrospector} implementation to map serde annotation to
 * Jackson annotations.
 */
final class SerdeJacksonAnnotationIntrospector extends JacksonXmlAnnotationIntrospector {
    private static final long serialVersionUID = 1L;

    @Override
    public String[] findEnumValues(Class<?> enumType, Enum<?>[] enumValues, String[] names) {
        Map<String, String> overrides = new HashMap<>();
        for (Field field : ClassUtil.getDeclaredFields(enumType)) {
            if (field.isEnumConstant() && field.getAnnotation(SerdeProperty.class) != null) {
                overrides.put(field.getName(), field.getAnnotation(SerdeProperty.class).value());
            }
        }

        if (overrides.isEmpty()) {
            return super.findEnumValues(enumType, enumValues, names);
        }

        for (int i = 0; i < enumValues.length; ++i) {
            final String serdeName = overrides.get(enumValues[i].name());
            names[i] = (serdeName != null) ? serdeName : names[i];
        }
        return names;
    }

    @Override
    public PropertyName findNameForSerialization(Annotated a) {
        SerdeProperty fieldName = a.getAnnotation(SerdeProperty.class);
        if (fieldName != null) {
            return PropertyName.construct(fieldName.value());
        }
        return super.findNameForSerialization(a);
    }

    @Override
    protected boolean _isIgnorable(Annotated a) {
        return a.hasAnnotation(SerdeIgnoreProperty.class) || super._isIgnorable(a);
    }

    @Override
    public JsonCreator.Mode findCreatorAnnotation(MapperConfig<?> config, Annotated a) {
        if (a.hasAnnotation(SerdeToPojo.class)) {
            return JsonCreator.Mode.DEFAULT;
        }
        return super.findCreatorAnnotation(config, a);
    }

    @Override
    public Boolean hasAsValue(Annotated a) {
        if (a.hasAnnotation(SerdeFromPojo.class)) {
            return true;
        }
        return super.hasAsValue(a);
    }

    @Override
    public Boolean hasAnyGetter(Annotated a) {
        SerdeGetKeyValues ann = _findAnnotation(a, SerdeGetKeyValues.class);
        if (ann != null) {
            return true;
        }
        return super.hasAnyGetter(a);
    }

    @Override
    public Boolean hasAnySetter(Annotated a) {
        SerdeSetKeyValues ann = _findAnnotation(a, SerdeSetKeyValues.class);
        if (ann != null) {
            return true;
        }
        return super.hasAnySetter(a);
    }

    @Override
    public String findTypeName(AnnotatedClass ac) {
        SerdeTypeName tn = _findAnnotation(ac, SerdeTypeName.class);
        if (tn != null) {
            return tn.value();
        }
        return super.findTypeName(ac);
    }

    @Override
    public List<NamedType> findSubtypes(Annotated a) {
        SerdeSubTypes t = _findAnnotation(a, SerdeSubTypes.class);
        if (t != null) {
            SerdeSubTypes.Type[] types = t.value();
            ArrayList<NamedType> result = new ArrayList<NamedType>(types.length);
            for (SerdeSubTypes.Type type : types) {
                result.add(new NamedType(type.value(), type.name()));
            }
            return result;
        }
        return super.findSubtypes(a);
    }

    @Override
    public PropertyName findNameForDeserialization(Annotated a) {
        SerdeSetter js = _findAnnotation(a, SerdeSetter.class);
        if (js != null) {
            String s = js.value();
            if (!s.isEmpty()) {
                return PropertyName.construct(s);
            }
        }
        SerdeProperty jp = _findAnnotation(a, SerdeProperty.class);
        if (jp != null) {
            return PropertyName.construct(jp.value());
        }
        return super.findNameForDeserialization(a);
    }

    @Override
    public JsonSetter.Value findSetterInfo(Annotated a) {
        SerdeSetter serdeSetter = _findAnnotation(a, SerdeSetter.class);
        if (serdeSetter == null) {
            return super.findSetterInfo(a);
        } else {
            return JsonSetter.Value.empty();
        }
    }

    @SuppressWarnings("deprecation")
    protected TypeResolverBuilder<?> _findTypeResolver(MapperConfig<?> config,
                                                       Annotated ann, JavaType baseType) {
        TypeResolverBuilder<?> b;
        SerdeTypeInfo info = _findAnnotation(ann, SerdeTypeInfo.class);
        if (info == null) {
            return super._findTypeResolver(config, ann, baseType);
        } else {
            if (info.use() == SerdeTypeInfo.Id.NONE) {
                return _constructNoTypeResolverBuilder();
            }
            b = _constructStdTypeResolverBuilder();
            // use
            b = b.init(JsonTypeInfo.Id.NAME, null);
            // property
            b = b.inclusion(JsonTypeInfo.As.PROPERTY);
            b = b.typeProperty(info.property());
            // defaultImpl
            Class<?> defaultImpl = info.defaultImpl();
            if (!defaultImpl.isAnnotation()) {
                b = b.defaultImpl(defaultImpl);
            }
            b = b.typeIdVisibility(info.visible());
            return b;
        }
    }

    @Override
    public List<PropertyName> findPropertyAliases(Annotated m) {
        SerdePropertyAlias ann = _findAnnotation(m, SerdePropertyAlias.class);
        if (ann == null) {
            return super.findPropertyAliases(m);
        } else {
            String[] strs = ann.value();
            final int len = strs.length;
            if (len == 0) {
                return Collections.emptyList();
            }
            List<PropertyName> result = new ArrayList<>(len);
            for (int i = 0; i < len; ++i) {
                result.add(PropertyName.construct(strs[i]));
            }
            return result;
        }
    }

    @Override
    public String findNamespace(Annotated ann) {
        SerdeXmlProperty prop = ann.getAnnotation(SerdeXmlProperty.class);
        if (prop == null) {
            return super.findNamespace(ann);
        } else {
            return prop.namespace();
        }
    }

    @Override
    public Boolean isOutputAsAttribute(Annotated ann) {
        SerdeXmlProperty prop = ann.getAnnotation(SerdeXmlProperty.class);
        if (prop == null) {
            return super.isOutputAsAttribute(ann);
        } else {
            return prop.isAttribute() ? Boolean.TRUE : Boolean.FALSE;
        }
    }

    @Override
    protected PropertyName _findXmlName(Annotated a) {
        SerdeXmlProperty prop = a.getAnnotation(SerdeXmlProperty.class);
        if (prop == null) {
            return super._findXmlName(a);
        } else {
            return PropertyName.construct(prop.localName(), prop.namespace());
        }
    }

    @Override
    public PropertyName findRootName(AnnotatedClass ac) {
        SerdeXmlRootElement root = ac.getAnnotation(SerdeXmlRootElement.class);
        if (root == null) {
            return super.findRootName(ac);
        } else {
            String local = root.localName();
            String ns = root.namespace();
            if (local.length() == 0 && ns.length() == 0) {
                return PropertyName.USE_DEFAULT;
            }
            return new PropertyName(local, ns);
        }
    }

    @Override
    protected StdTypeResolverBuilder _constructStdTypeResolverBuilder() {
        return new StdTypeResolverBuilder();
    }
}

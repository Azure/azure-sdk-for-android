// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.serde.jackson;


import com.azure.android.core.serde.JsonFlatten;
import com.azure.android.core.serde.SerdeEncoding;
import com.azure.android.core.serde.SerdeProperty;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlatteningSerializerTests {
    @Test
    public void canFlatten() throws Exception {
        Foo foo = new Foo();
        foo.bar("hello.world");
        //
        List<String> baz = new ArrayList<>();
        baz.add("hello");
        baz.add("hello.world");
        foo.baz(baz);

        HashMap<String, String> qux = new HashMap<>();
        qux.put("hello", "world");
        qux.put("a.b", "c.d");
        qux.put("bar.a", "ttyy");
        qux.put("bar.b", "uuzz");
        foo.qux(qux);

        JacksonSerderAdapter adapter = new JacksonSerderAdapter();

        // serialization
        String serialized = adapter.serialize(foo, SerdeEncoding.JSON);
        Assertions.assertEquals("{\"$type\":\"foo\",\"properties\":{\"bar\":\"hello.world\",\"props\":{\"baz\":[\"hello\",\"hello.world\"],\"q\":{\"qux\":{\"hello\":\"world\",\"a.b\":\"c.d\",\"bar.b\":\"uuzz\",\"bar.a\":\"ttyy\"}}}}}", serialized);

        // deserialization
        Foo deserialized = adapter.deserialize(serialized, Foo.class, SerdeEncoding.JSON);
        Assertions.assertEquals("hello.world", deserialized.bar());
        Assertions.assertArrayEquals(new String[]{"hello", "hello.world"}, deserialized.baz().toArray());
        Assertions.assertNotNull(deserialized.qux());
        Assertions.assertEquals("world", deserialized.qux().get("hello"));
        Assertions.assertEquals("c.d", deserialized.qux().get("a.b"));
        Assertions.assertEquals("ttyy", deserialized.qux().get("bar.a"));
        Assertions.assertEquals("uuzz", deserialized.qux().get("bar.b"));
    }

    @Test
    public void canSerializeMapKeysWithDotAndSlash() throws Exception {
        String serialized = new JacksonSerderAdapter().serialize(prepareSchoolModel(), SerdeEncoding.JSON);
        Assertions.assertEquals("{\"teacher\":{\"students\":{\"af.B/D\":{},\"af.B/C\":{}}},\"tags\":{\"foo.aa\":\"bar\",\"x.y\":\"zz\"},\"properties\":{\"name\":\"school1\"}}", serialized);
    }

    /**
     * Validates decoding and encoding of a type with type id containing dot and no additional properties
     * For decoding and encoding base type will be used.
     *
     * @throws IOException
     */
    @Test
    public void canHandleTypeWithTypeIdContainingDotAndNoProperties() throws IOException {
        JacksonSerderAdapter adapter = new JacksonSerderAdapter();

        String rabbitSerialized = "{\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\"}";
        String shelterSerialized = "{\"properties\":{\"animalsInfo\":[{\"animal\":{\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\"}},{\"animal\":{\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\"}}]}}";

        AnimalWithTypeIdContainingDot rabbitDeserialized = adapter.deserialize(rabbitSerialized,
            AnimalWithTypeIdContainingDot.class,
            SerdeEncoding.JSON);
        Assertions.assertTrue(rabbitDeserialized instanceof RabbitWithTypeIdContainingDot);
        Assertions.assertNotNull(rabbitDeserialized);

        AnimalShelter shelterDeserialized = adapter.deserialize(shelterSerialized,
            AnimalShelter.class,
            SerdeEncoding.JSON);
        Assertions.assertTrue(shelterDeserialized instanceof AnimalShelter);
        Assertions.assertEquals(2, shelterDeserialized.animalsInfo().size());
        for (FlattenableAnimalInfo animalInfo: shelterDeserialized.animalsInfo()) {
            Assertions.assertTrue(animalInfo.animal() instanceof RabbitWithTypeIdContainingDot);
            Assertions.assertNotNull(animalInfo.animal());
        }
    }

    /**
     * Validates that decoding and encoding of a type with type id containing dot and can be done.
     * For decoding and encoding base type will be used.
     *
     * @throws IOException
     */
    @Test
    public void canHandleTypeWithTypeIdContainingDot0() throws IOException {
        JacksonSerderAdapter adapter = new JacksonSerderAdapter();
        // Serialize
        //
        List<String> meals = new ArrayList<>();
        meals.add("carrot");
        meals.add("apple");
        //
        AnimalWithTypeIdContainingDot animalToSerialize = new RabbitWithTypeIdContainingDot().withMeals(meals);
        String serialized = adapter.serialize(animalToSerialize, SerdeEncoding.JSON);
        //
        String[] results = {
            "{\"meals\":[\"carrot\",\"apple\"],\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\"}",
            "{\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\",\"meals\":[\"carrot\",\"apple\"]}"
        };
        boolean found = false;
        for (String result : results) {
            if (result.equals(serialized)) {
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found);
        // De-Serialize
        //
        AnimalWithTypeIdContainingDot animalDeserialized = adapter.deserialize(serialized,
            AnimalWithTypeIdContainingDot.class,
            SerdeEncoding.JSON);
        Assertions.assertTrue(animalDeserialized instanceof RabbitWithTypeIdContainingDot);
        RabbitWithTypeIdContainingDot rabbit = (RabbitWithTypeIdContainingDot) animalDeserialized;
        Assertions.assertNotNull(rabbit.meals());
        Assertions.assertEquals(rabbit.meals().size(), 2);
    }

    /**
     * Validates that decoding and encoding of a type with type id containing dot and can be done.
     * For decoding and encoding concrete type will be used.
     *
     * @throws IOException
     */
    @Test
    public void canHandleTypeWithTypeIdContainingDot1() throws IOException {
        JacksonSerderAdapter adapter = new JacksonSerderAdapter();
        // Serialize
        //
        List<String> meals = new ArrayList<>();
        meals.add("carrot");
        meals.add("apple");
        //
        RabbitWithTypeIdContainingDot rabbitToSerialize = new RabbitWithTypeIdContainingDot().withMeals(meals);
        String serialized = adapter.serialize(rabbitToSerialize, SerdeEncoding.JSON);
        //
        String[] results = {
            "{\"meals\":[\"carrot\",\"apple\"],\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\"}",
            "{\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\",\"meals\":[\"carrot\",\"apple\"]}"
        };
        boolean found = false;
        for (String result : results) {
            if (result.equals(serialized)) {
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found);
        // De-Serialize
        //
        RabbitWithTypeIdContainingDot rabbitDeserialized = adapter.deserialize(serialized,
            RabbitWithTypeIdContainingDot.class,
            SerdeEncoding.JSON);
        Assertions.assertTrue(rabbitDeserialized instanceof RabbitWithTypeIdContainingDot);
        Assertions.assertNotNull(rabbitDeserialized.meals());
        Assertions.assertEquals(rabbitDeserialized.meals().size(), 2);
    }


    /**
     * Validates that decoding and encoding of a type with flattenable property and type id containing dot and can be done.
     * For decoding and encoding base type will be used.
     *
     * @throws IOException
     */
    @Test
    public void canHandleTypeWithFlattenablePropertyAndTypeIdContainingDot0() throws IOException {
        AnimalWithTypeIdContainingDot animalToSerialize = new DogWithTypeIdContainingDot().withBreed("AKITA").withCuteLevel(10);
        JacksonSerderAdapter adapter = new JacksonSerderAdapter();
        // serialization
        String serialized = adapter.serialize(animalToSerialize, SerdeEncoding.JSON);
        String[] results = {
            "{\"breed\":\"AKITA\",\"@odata.type\":\"#Favourite.Pet.DogWithTypeIdContainingDot\",\"properties\":{\"cuteLevel\":10}}",
            "{\"breed\":\"AKITA\",\"properties\":{\"cuteLevel\":10},\"@odata.type\":\"#Favourite.Pet.DogWithTypeIdContainingDot\"}",
            "{\"@odata.type\":\"#Favourite.Pet.DogWithTypeIdContainingDot\",\"breed\":\"AKITA\",\"properties\":{\"cuteLevel\":10}}",
            "{\"@odata.type\":\"#Favourite.Pet.DogWithTypeIdContainingDot\",\"properties\":{\"cuteLevel\":10},\"breed\":\"AKITA\"}",
            "{\"properties\":{\"cuteLevel\":10},\"@odata.type\":\"#Favourite.Pet.DogWithTypeIdContainingDot\",\"breed\":\"AKITA\"}",
            "{\"properties\":{\"cuteLevel\":10},\"breed\":\"AKITA\",\"@odata.type\":\"#Favourite.Pet.DogWithTypeIdContainingDot\"}",
        };
        boolean found = false;
        for (String result : results) {
            if (result.equals(serialized)) {
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found);
        // de-serialization
        AnimalWithTypeIdContainingDot animalDeserialized = adapter.deserialize(serialized,
            AnimalWithTypeIdContainingDot.class,
            SerdeEncoding.JSON);
        Assertions.assertTrue(animalDeserialized instanceof DogWithTypeIdContainingDot);
        DogWithTypeIdContainingDot dogDeserialized = (DogWithTypeIdContainingDot) animalDeserialized;
        Assertions.assertNotNull(dogDeserialized);
        Assertions.assertEquals(dogDeserialized.breed(), "AKITA");
        Assertions.assertEquals(dogDeserialized.cuteLevel(), (Integer) 10);
    }

    /**
     * Validates that decoding and encoding of a type with flattenable property and type id containing dot and can be done.
     * For decoding and encoding concrete type will be used.
     *
     * @throws IOException
     */
    @Test
    public void canHandleTypeWithFlattenablePropertyAndTypeIdContainingDot1() throws IOException {
        DogWithTypeIdContainingDot dogToSerialize = new DogWithTypeIdContainingDot().withBreed("AKITA").withCuteLevel(10);
        JacksonSerderAdapter adapter = new JacksonSerderAdapter();
        // serialization
        String serialized = adapter.serialize(dogToSerialize, SerdeEncoding.JSON);
        String[] results = {
            "{\"breed\":\"AKITA\",\"@odata.type\":\"#Favourite.Pet.DogWithTypeIdContainingDot\",\"properties\":{\"cuteLevel\":10}}",
            "{\"breed\":\"AKITA\",\"properties\":{\"cuteLevel\":10},\"@odata.type\":\"#Favourite.Pet.DogWithTypeIdContainingDot\"}",
            "{\"@odata.type\":\"#Favourite.Pet.DogWithTypeIdContainingDot\",\"breed\":\"AKITA\",\"properties\":{\"cuteLevel\":10}}",
            "{\"@odata.type\":\"#Favourite.Pet.DogWithTypeIdContainingDot\",\"properties\":{\"cuteLevel\":10},\"breed\":\"AKITA\"}",
            "{\"properties\":{\"cuteLevel\":10},\"@odata.type\":\"#Favourite.Pet.DogWithTypeIdContainingDot\",\"breed\":\"AKITA\"}",
            "{\"properties\":{\"cuteLevel\":10},\"breed\":\"AKITA\",\"@odata.type\":\"#Favourite.Pet.DogWithTypeIdContainingDot\"}",
        };
        boolean found = false;
        for (String result : results) {
            if (result.equals(serialized)) {
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found);
        // de-serialization
        DogWithTypeIdContainingDot dogDeserialized = adapter.deserialize(serialized,
            DogWithTypeIdContainingDot.class,
            SerdeEncoding.JSON);
        Assertions.assertNotNull(dogDeserialized);
        Assertions.assertEquals(dogDeserialized.breed(), "AKITA");
        Assertions.assertEquals(dogDeserialized.cuteLevel(), (Integer) 10);
    }

    /**
     * Validates that decoding and encoding of a array of type with type id containing dot and can be done.
     * For decoding and encoding base type will be used.
     *
     * @throws IOException
     */
    @Test
    public void canHandleArrayOfTypeWithTypeIdContainingDot0() throws IOException {
        JacksonSerderAdapter adapter = new JacksonSerderAdapter();
        // Serialize
        //
        List<String> meals = new ArrayList<>();
        meals.add("carrot");
        meals.add("apple");
        //
        AnimalWithTypeIdContainingDot animalToSerialize = new RabbitWithTypeIdContainingDot().withMeals(meals);
        List<AnimalWithTypeIdContainingDot> animalsToSerialize = new ArrayList<>();
        animalsToSerialize.add(animalToSerialize);
        String serialized = adapter.serialize(animalsToSerialize, SerdeEncoding.JSON);
        String[] results = {
            "[{\"meals\":[\"carrot\",\"apple\"],\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\"}]",
            "[{\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\",\"meals\":[\"carrot\",\"apple\"]}]",
        };
        boolean found = false;
        for (String result : results) {
            if (result.equals(serialized)) {
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found);
        // De-serialize
        //
        List<AnimalWithTypeIdContainingDot> animalsDeserialized = adapter.deserialize(serialized, new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[] { AnimalWithTypeIdContainingDot.class };
            }

            @Override
            public Type getRawType() {
                return List.class;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        }, SerdeEncoding.JSON);
        Assertions.assertNotNull(animalsDeserialized);
        Assertions.assertEquals(1, animalsDeserialized.size());
        AnimalWithTypeIdContainingDot animalDeserialized = animalsDeserialized.get(0);
        Assertions.assertTrue(animalDeserialized instanceof RabbitWithTypeIdContainingDot);
        RabbitWithTypeIdContainingDot rabbitDeserialized = (RabbitWithTypeIdContainingDot) animalDeserialized;
        Assertions.assertNotNull(rabbitDeserialized.meals());
        Assertions.assertEquals(rabbitDeserialized.meals().size(), 2);
    }

    /**
     * Validates that decoding and encoding of a array of type with type id containing dot and can be done.
     * For decoding and encoding concrete type will be used.
     *
     * @throws IOException
     */
    @Test
    public void canHandleArrayOfTypeWithTypeIdContainingDot1() throws IOException {
        JacksonSerderAdapter adapter = new JacksonSerderAdapter();
        // Serialize
        //
        List<String> meals = new ArrayList<>();
        meals.add("carrot");
        meals.add("apple");
        //
        RabbitWithTypeIdContainingDot rabbitToSerialize = new RabbitWithTypeIdContainingDot().withMeals(meals);
        List<RabbitWithTypeIdContainingDot> rabbitsToSerialize = new ArrayList<>();
        rabbitsToSerialize.add(rabbitToSerialize);
        String serialized = adapter.serialize(rabbitsToSerialize, SerdeEncoding.JSON);
        String[] results = {
            "[{\"meals\":[\"carrot\",\"apple\"],\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\"}]",
            "[{\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\",\"meals\":[\"carrot\",\"apple\"]}]",
        };
        boolean found = false;
        for (String result : results) {
            if (result.equals(serialized)) {
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found);
        // De-serialize
        //
        List<RabbitWithTypeIdContainingDot> rabbitsDeserialized = adapter.deserialize(serialized, new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[] { RabbitWithTypeIdContainingDot.class };
            }

            @Override
            public Type getRawType() {
                return List.class;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        }, SerdeEncoding.JSON);
        Assertions.assertNotNull(rabbitsDeserialized);
        Assertions.assertEquals(1, rabbitsDeserialized.size());
        RabbitWithTypeIdContainingDot rabbitDeserialized = rabbitsDeserialized.get(0);
        Assertions.assertNotNull(rabbitDeserialized.meals());
        Assertions.assertEquals(rabbitDeserialized.meals().size(), 2);
    }


    /**
     * Validates that decoding and encoding of a composed type with type id containing dot and can be done.
     *
     * @throws IOException
     */
    @Test
    public void canHandleComposedTypeWithTypeIdContainingDot0() throws IOException {
        JacksonSerderAdapter adapter = new JacksonSerderAdapter();
        // serialization
        //
        List<String> meals = new ArrayList<>();
        meals.add("carrot");
        meals.add("apple");
        AnimalWithTypeIdContainingDot animalToSerialize = new RabbitWithTypeIdContainingDot().withMeals(meals);
        FlattenableAnimalInfo animalInfoToSerialize = new FlattenableAnimalInfo().withAnimal(animalToSerialize);
        ArrayList<FlattenableAnimalInfo> animalsInfoSerialized = new ArrayList<>();
        animalsInfoSerialized.add(animalInfoToSerialize);
        AnimalShelter animalShelterToSerialize = new AnimalShelter().withAnimalsInfo(animalsInfoSerialized);
        String serialized = adapter.serialize(animalShelterToSerialize, SerdeEncoding.JSON);
        String[] results = {
            "{\"properties\":{\"animalsInfo\":[{\"animal\":{\"meals\":[\"carrot\",\"apple\"],\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\"}}]}}",
            "{\"properties\":{\"animalsInfo\":[{\"animal\":{\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\",\"meals\":[\"carrot\",\"apple\"]}}]}}",
        };

        boolean found = false;
        for (String result : results) {
            if (result.equals(serialized)) {
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found);
        // de-serialization
        //
        AnimalShelter shelterDeserialized = adapter.deserialize(serialized,
            AnimalShelter.class,
            SerdeEncoding.JSON);
        Assertions.assertNotNull(shelterDeserialized.animalsInfo());
        Assertions.assertEquals(shelterDeserialized.animalsInfo().size(), 1);
        FlattenableAnimalInfo animalsInfoDeserialized = shelterDeserialized.animalsInfo().get(0);
        Assertions.assertTrue(animalsInfoDeserialized.animal() instanceof RabbitWithTypeIdContainingDot);
        AnimalWithTypeIdContainingDot animalDeserialized = animalsInfoDeserialized.animal();
        Assertions.assertTrue(animalDeserialized instanceof RabbitWithTypeIdContainingDot);
        RabbitWithTypeIdContainingDot rabbitDeserialized = (RabbitWithTypeIdContainingDot) animalDeserialized;
        Assertions.assertNotNull(rabbitDeserialized);
        Assertions.assertNotNull(rabbitDeserialized.meals());
        Assertions.assertEquals(rabbitDeserialized.meals().size(), 2);
    }

    @Test
    public void canHandleComposedSpecificPolymorphicTypeWithTypeId() throws IOException {
        JacksonSerderAdapter adapter = new JacksonSerderAdapter();
        //
        // -- Validate vector property
        //
        String serializedCollectionWithTypeId = "{\"turtlesSet1\":[{\"age\":100,\"size\":10,\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\"},{\"age\":200,\"size\":20,\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\"}]}";
        // de-serialization
        //
        ComposeTurtles composedTurtleDeserialized = adapter.deserialize(serializedCollectionWithTypeId,
            ComposeTurtles.class,
            SerdeEncoding.JSON);
        Assertions.assertNotNull(composedTurtleDeserialized);
        Assertions.assertNotNull(composedTurtleDeserialized.turtlesSet1());
        Assertions.assertEquals(2, composedTurtleDeserialized.turtlesSet1().size());
        //
        adapter.serialize(composedTurtleDeserialized, SerdeEncoding.JSON);
        //
        // -- Validate scalar property
        //
        String serializedScalarWithTypeId = "{\"turtlesSet1Lead\":{\"age\":100,\"size\":10,\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\"}}";
        // de-serialization
        //
        composedTurtleDeserialized = adapter.deserialize(serializedScalarWithTypeId,
            ComposeTurtles.class,
            SerdeEncoding.JSON);
        Assertions.assertNotNull(composedTurtleDeserialized);
        Assertions.assertNotNull(composedTurtleDeserialized.turtlesSet1Lead());
        Assertions.assertEquals(10, (long) composedTurtleDeserialized.turtlesSet1Lead().size());
        Assertions.assertEquals(100, (long) composedTurtleDeserialized.turtlesSet1Lead().age());
        //
        adapter.serialize(composedTurtleDeserialized, SerdeEncoding.JSON);
    }

    @Test
    public void canHandleComposedSpecificPolymorphicTypeWithoutTypeId() throws IOException {
        JacksonSerderAdapter adapter = new JacksonSerderAdapter();
        //
        // -- Validate vector property
        //
        String serializedCollectionWithTypeId = "{\"turtlesSet1\":[{\"age\":100,\"size\":10 },{\"age\":200,\"size\":20 }]}";
        // de-serialization
        //
        ComposeTurtles composedTurtleDeserialized = adapter.deserialize(serializedCollectionWithTypeId,
            ComposeTurtles.class,
            SerdeEncoding.JSON);
        Assertions.assertNotNull(composedTurtleDeserialized);
        Assertions.assertNotNull(composedTurtleDeserialized.turtlesSet1());
        Assertions.assertEquals(2, composedTurtleDeserialized.turtlesSet1().size());
        //
        adapter.serialize(composedTurtleDeserialized, SerdeEncoding.JSON);
        //
        // -- Validate scalar property
        //
        String serializedScalarWithTypeId = "{\"turtlesSet1Lead\":{\"age\":100,\"size\":10 }}";
        // de-serialization
        //
        composedTurtleDeserialized = adapter.deserialize(serializedScalarWithTypeId,
            ComposeTurtles.class,
            SerdeEncoding.JSON);
        Assertions.assertNotNull(composedTurtleDeserialized);
        Assertions.assertNotNull(composedTurtleDeserialized.turtlesSet1Lead());
        Assertions.assertEquals(100, (long) composedTurtleDeserialized.turtlesSet1Lead().age());
        //
        adapter.serialize(composedTurtleDeserialized, SerdeEncoding.JSON);
    }

    @Test
    public void canHandleComposedSpecificPolymorphicTypeWithAndWithoutTypeId() throws IOException {
        JacksonSerderAdapter adapter = new JacksonSerderAdapter();
        //
        // -- Validate vector property
        //
        String serializedCollectionWithTypeId = "{\"turtlesSet1\":[{\"age\":100,\"size\":10,\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\"},{\"age\":200,\"size\":20 }]}";
        // de-serialization
        //
        ComposeTurtles composedTurtleDeserialized = adapter.deserialize(serializedCollectionWithTypeId,
            ComposeTurtles.class,
            SerdeEncoding.JSON);
        Assertions.assertNotNull(composedTurtleDeserialized);
        Assertions.assertNotNull(composedTurtleDeserialized.turtlesSet1());
        Assertions.assertEquals(2, composedTurtleDeserialized.turtlesSet1().size());
        //
        adapter.serialize(composedTurtleDeserialized, SerdeEncoding.JSON);
    }

    @Test
    public void canHandleComposedGenericPolymorphicTypeWithTypeId() throws IOException {
        JacksonSerderAdapter adapter = new JacksonSerderAdapter();
        //
        // -- Validate vector property
        //
        String serializedCollectionWithTypeId = "{\"turtlesSet2\":[{\"age\":100,\"size\":10,\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\"},{\"age\":200,\"size\":20,\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\"}]}";
        // de-serialization
        //
        ComposeTurtles composedTurtleDeserialized = adapter.deserialize(serializedCollectionWithTypeId,
            ComposeTurtles.class,
            SerdeEncoding.JSON);
        Assertions.assertNotNull(composedTurtleDeserialized);
        Assertions.assertNotNull(composedTurtleDeserialized.turtlesSet2());
        Assertions.assertEquals(2, composedTurtleDeserialized.turtlesSet2().size());
        //
        Assertions.assertTrue(composedTurtleDeserialized.turtlesSet2().get(0) instanceof TurtleWithTypeIdContainingDot);
        Assertions.assertTrue(composedTurtleDeserialized.turtlesSet2().get(1) instanceof TurtleWithTypeIdContainingDot);
        //
        adapter.serialize(composedTurtleDeserialized,
            SerdeEncoding.JSON);
        //
        // -- Validate scalar property
        //
        String serializedScalarWithTypeId = "{\"turtlesSet2Lead\":{\"age\":100,\"size\":10,\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\"}}";
        // de-serialization
        //
        composedTurtleDeserialized = adapter.deserialize(serializedScalarWithTypeId,
            ComposeTurtles.class,
            SerdeEncoding.JSON);
        Assertions.assertNotNull(composedTurtleDeserialized);
        Assertions.assertNotNull(composedTurtleDeserialized.turtlesSet2Lead());
        Assertions.assertNotNull(composedTurtleDeserialized.turtlesSet2Lead() instanceof TurtleWithTypeIdContainingDot);
        Assertions.assertEquals(10, (long) ((TurtleWithTypeIdContainingDot) composedTurtleDeserialized.turtlesSet2Lead()).size());
        Assertions.assertEquals(100, (long) composedTurtleDeserialized.turtlesSet2Lead().age());
        //
        adapter.serialize(composedTurtleDeserialized, SerdeEncoding.JSON);
    }

    @Test
    public void canHandleComposedGenericPolymorphicTypeWithoutTypeId() throws IOException {
        JacksonSerderAdapter adapter = new JacksonSerderAdapter();
        //
        // -- Validate vector property
        //
        String serializedCollectionWithTypeId = "{\"turtlesSet2\":[{\"age\":100,\"size\":10 },{\"age\":200,\"size\":20 }]}";
        // de-serialization
        //
        ComposeTurtles composedTurtleDeserialized = adapter.deserialize(serializedCollectionWithTypeId,
            ComposeTurtles.class,
            SerdeEncoding.JSON);
        Assertions.assertNotNull(composedTurtleDeserialized);
        Assertions.assertNotNull(composedTurtleDeserialized.turtlesSet2());
        Assertions.assertEquals(2, composedTurtleDeserialized.turtlesSet2().size());
        //
        Assertions.assertFalse(composedTurtleDeserialized.turtlesSet2().get(0) instanceof TurtleWithTypeIdContainingDot);
        Assertions.assertTrue(composedTurtleDeserialized.turtlesSet2().get(0) instanceof NonEmptyAnimalWithTypeIdContainingDot);
        Assertions.assertFalse(composedTurtleDeserialized.turtlesSet2().get(1) instanceof TurtleWithTypeIdContainingDot);
        Assertions.assertTrue(composedTurtleDeserialized.turtlesSet2().get(1) instanceof NonEmptyAnimalWithTypeIdContainingDot);
        //
        // -- Validate scalar property
        //
        adapter.serialize(composedTurtleDeserialized, SerdeEncoding.JSON);
        //
        String serializedScalarWithTypeId = "{\"turtlesSet2Lead\":{\"age\":100,\"size\":10 }}";
        // de-serialization
        //
        composedTurtleDeserialized = adapter.deserialize(serializedScalarWithTypeId,
            ComposeTurtles.class,
            SerdeEncoding.JSON);
        Assertions.assertNotNull(composedTurtleDeserialized);
        Assertions.assertNotNull(composedTurtleDeserialized.turtlesSet2Lead());
        Assertions.assertNotNull(composedTurtleDeserialized.turtlesSet2Lead() instanceof NonEmptyAnimalWithTypeIdContainingDot);
        //
        adapter.serialize(composedTurtleDeserialized,
            SerdeEncoding.JSON);
    }

    @Test
    public void canHandleComposedGenericPolymorphicTypeWithAndWithoutTypeId() throws IOException {
        JacksonSerderAdapter adapter = new JacksonSerderAdapter();
        //
        // -- Validate vector property
        //
        String serializedCollectionWithTypeId = "{\"turtlesSet2\":[{\"age\":100,\"size\":10,\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\"},{\"age\":200,\"size\":20 }]}";
        // de-serialization
        //
        ComposeTurtles composedTurtleDeserialized = adapter.deserialize(serializedCollectionWithTypeId,
            ComposeTurtles.class,
            SerdeEncoding.JSON);
        Assertions.assertNotNull(composedTurtleDeserialized);
        Assertions.assertNotNull(composedTurtleDeserialized.turtlesSet2());
        Assertions.assertEquals(2, composedTurtleDeserialized.turtlesSet2().size());
        //
        Assertions.assertTrue(composedTurtleDeserialized.turtlesSet2().get(0) instanceof TurtleWithTypeIdContainingDot);
        Assertions.assertTrue(composedTurtleDeserialized.turtlesSet2().get(1) instanceof NonEmptyAnimalWithTypeIdContainingDot);
        //
        adapter.serialize(composedTurtleDeserialized, SerdeEncoding.JSON);
    }

    @Test
    public void canHandleEscapedProperties() throws IOException {
        FlattenedProduct productToSerialize = new FlattenedProduct();
        productToSerialize.withProductName("drink");
        productToSerialize.withPType("chai");
        JacksonSerderAdapter adapter = new JacksonSerderAdapter();
        // serialization
        //
        String serialized = adapter.serialize(productToSerialize,
            SerdeEncoding.JSON);
        String[] results = {
            "{\"properties\":{\"p.name\":\"drink\",\"type\":\"chai\"}}",
            "{\"properties\":{\"type\":\"chai\",\"p.name\":\"drink\"}}",
        };

        boolean found = false;
        for (String result : results) {
            if (result.equals(serialized)) {
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found);
        // de-serialization
        //
        FlattenedProduct productDeserialized = adapter.deserialize(serialized,
            FlattenedProduct.class,
            SerdeEncoding.JSON);
        Assertions.assertNotNull(productDeserialized);
        Assertions.assertEquals(productDeserialized.productName(), "drink");
        Assertions.assertEquals(productDeserialized.productType, "chai");
    }

    @JsonFlatten
    private class School {
        @SerdeProperty(value = "teacher")
        private Teacher teacher;

        @SerdeProperty(value = "properties.name")
        private String name;

        @SerdeProperty(value = "tags")
        private Map<String, String> tags;

        public School withTeacher(Teacher teacher) {
            this.teacher = teacher;
            return this;
        }

        public School withName(String name) {
            this.name = name;
            return this;
        }

        public School withTags(Map<String, String> tags) {
            this.tags = tags;
            return this;
        }
    }

    private class Student {
    }

    private class Teacher {
        @SerdeProperty(value = "students")
        private Map<String, Student> students;

        public Teacher withStudents(Map<String, Student> students) {
            this.students = students;
            return this;
        }
    }

    private School prepareSchoolModel() {
        Teacher teacher = new Teacher();

        Map<String, Student> students = new HashMap<String, Student>();
        students.put("af.B/C", new Student());
        students.put("af.B/D", new Student());

        teacher.withStudents(students);

        School school = new School().withName("school1");
        school.withTeacher(teacher);

        Map<String, String> schoolTags = new HashMap<String, String>();
        schoolTags.put("foo.aa", "bar");
        schoolTags.put("x.y", "zz");

        school.withTags(schoolTags);

        return school;
    }

    @JsonFlatten
    public static class FlattenedProduct {
        // Flattened and escaped property
        @SerdeProperty(value = "properties.p\\.name")
        private String productName;

        @SerdeProperty(value = "properties.type")
        private String productType;

        public String productName() {
            return this.productName;
        }

        public FlattenedProduct withProductName(String productName) {
            this.productName = productName;
            return this;
        }

        public String productType() {
            return this.productType;
        }

        public FlattenedProduct withPType(String productType) {
            this.productType = productType;
            return this;
        }
    }
}


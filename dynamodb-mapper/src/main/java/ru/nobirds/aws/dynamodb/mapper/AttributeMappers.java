package ru.nobirds.aws.dynamodb.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class AttributeMappers {

    private static <T, R> R processIfNotNull(T value, Function<T, R> processor) {
        return value == null ? null : processor.apply(value);
    }

    public static final AttributeMapper<String> STRING = new SimpleAttributeMapper<>(
        AttributeValue::s,
        value -> AttributeValue.builder().s(value).build()
    );

    public static final AttributeMapper<Long> NUMBER = new SimpleAttributeMapper<>(
        attributeValue -> processIfNotNull(attributeValue.n(), Long::parseLong),
        value -> AttributeValue.builder().n(Long.toString(value)).build()
    );

    public static final AttributeMapper<Double> DECIMAL = new SimpleAttributeMapper<>(
        attributeValue -> processIfNotNull(attributeValue.n(), Double::parseDouble),
        value -> AttributeValue.builder().n(Double.toString(value)).build()
    );

    public static <T> AttributeMapper<T> object(AttributesMapper<T> mapper) {
        return new SimpleAttributeMapper<>(
            attributeValue -> attributeValue.hasM() ? mapper.map(attributeValue.m()) : null,
            value -> AttributeValue.builder().m(mapper.map(value)).build());
    }

    public static <T extends Enum<T>> AttributeMapper<T> enumeration(Class<T> enumType) {
        return new SimpleAttributeMapper<>(
            attributeValue -> processIfNotNull(attributeValue.s(), value -> Enum.valueOf(enumType, value)),
            value -> AttributeValue.builder().s(value.name()).build());
    }

    public static <T> AttributeMapper<List<T>> list(AttributeMapper<T> mapper) {
        return new SimpleAttributeMapper<>(
            attributeValue -> attributeValue.hasL()
                ? attributeValue.l().stream().map(mapper::map).collect(Collectors.toList()) : null,
            value -> (value == null) ? AttributeValue.builder().nul(true).build() : AttributeValue.builder()
                .l(value.stream().map(mapper::map).collect(Collectors.toList())).build());
    }

    public static <T> AttributesBuilder<T> builder(Supplier<T> constructor) {
        return new AttributesBuilder<>(constructor);
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class AttributesBuilder<R> {

        private final Supplier<R> constructor;
        private final List<Attribute<R>> attributes = new ArrayList<>();

        public AttributesBuilder<R> attribute(Attribute<R> attribute) {
            attributes.add(attribute);
            return this;
        }

        public <T> AttributesBuilder<R> constant(String name, Supplier<T> value, AttributeMapper<T> mapper) {
            return attribute(new ConstantAttribute<>(name, value, mapper));
        }

        public <T> AttributesBuilder<R> constantValue(String name, T value, AttributeMapper<T> mapper) {
            return constant(name, () -> value, mapper);
        }

        public AttributesBuilder<R> string(String name, Function<R, String> getter, BiConsumer<R, String> setter) {
            return attribute(name, getter, setter, STRING);
        }

        public <T extends Enum<T>> AttributesBuilder<R> enumeration(
            String name, Function<R, T> getter, BiConsumer<R, T> setter, Class<T> enumType) {
            return attribute(name, getter, setter, AttributeMappers.enumeration(enumType));
        }

        public AttributesBuilder<R> number(String name, Function<R, Long> getter, BiConsumer<R, Long> setter) {
            return attribute(name, getter, setter, NUMBER);
        }

        public AttributesBuilder<R> decimal(String name, Function<R, Double> getter, BiConsumer<R, Double> setter) {
            return attribute(name, getter, setter, DECIMAL);
        }

        public <T> AttributesBuilder<R> object(String name, Function<R, T> getter, BiConsumer<R, T> setter,
            AttributesMapper<T> mapper) {
            return attribute(name, getter, setter, AttributeMappers.object(mapper));
        }

        public <T> AttributesBuilder<R> list(String name, Function<R, List<T>> getter, BiConsumer<R, List<T>> setter,
            AttributeMapper<T> mapper) {
            return attribute(name, getter, setter, AttributeMappers.list(mapper));
        }

        public <T> AttributesBuilder<R> attribute(
            String name,
            Function<R, T> getter,
            BiConsumer<R, T> setter,
            AttributeMapper<T> mapper) {
            return attribute(new Property<>(name, getter, setter), mapper);
        }

        public <T> AttributesBuilder<R> attribute(Property<R, T> property, AttributeMapper<T> mapper) {
            return attribute(new SimpleAttribute<>(property, mapper));
        }

        public AttributesMapper<R> build() {
            return new SimpleAttributesMapper<>(constructor, attributes);
        }
    }

}

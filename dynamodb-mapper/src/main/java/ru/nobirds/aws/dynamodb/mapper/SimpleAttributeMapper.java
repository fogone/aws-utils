package ru.nobirds.aws.dynamodb.mapper;

import java.util.function.Function;
import lombok.AllArgsConstructor;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@AllArgsConstructor
public class SimpleAttributeMapper<T> implements AttributeMapper<T> {

    private final Function<AttributeValue, T> reader;
    private final Function<T, AttributeValue> writer;

    @Override
    public T map(AttributeValue attributeValue) {
        return (attributeValue == null) ? null : reader.apply(attributeValue);
    }

    @Override
    public AttributeValue map(T value) {
        return (value == null) ? AttributeValue.builder().nul(true).build() : writer.apply(value);
    }
}

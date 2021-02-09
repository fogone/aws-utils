package ru.nobirds.aws.dynamodb.mapper;

import java.util.function.Function;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public interface AttributeMapper<T> {

    T map(AttributeValue attributeValue);

    AttributeValue map(T value);

    default <R> AttributeMapper<R> map(Function<T, R> reader, Function<R, T> writer) {
        return map(BidirectionalMapper.of(reader, writer));
    }

    default <R> AttributeMapper<R> map(BidirectionalMapper<T, R> mapper) {
        return new SimpleAttributeMapper<>(attributeValue -> mapper.read(map(attributeValue)),
            value -> map(mapper.write(value)));
    }

}

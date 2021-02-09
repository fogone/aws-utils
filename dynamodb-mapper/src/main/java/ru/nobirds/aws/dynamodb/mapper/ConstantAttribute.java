package ru.nobirds.aws.dynamodb.mapper;

import java.util.Map;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@RequiredArgsConstructor
public class ConstantAttribute<R, T> implements Attribute<R> {

    private final String name;
    private final Supplier<T> value;
    private final AttributeMapper<T> mapper;

    @Override
    public void writeFromMapToInstance(Map<String, AttributeValue> attributeValues, R instance) {
        // do nothing
    }

    @Override
    public void writeFromInstanceToMap(R instance, Map<String, AttributeValue> result) {
        result.put(name, mapper.map(value.get()));
    }
}

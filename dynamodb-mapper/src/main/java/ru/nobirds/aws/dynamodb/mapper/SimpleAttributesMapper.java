package ru.nobirds.aws.dynamodb.mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@RequiredArgsConstructor
public class SimpleAttributesMapper<R> implements AttributesMapper<R> {

    private final Supplier<R> constructor;
    private final List<Attribute<R>> attributes;

    @Override
    public R map(Map<String, AttributeValue> attributes) {
        R instance = constructor.get();
        for (Attribute<R> attribute : this.attributes) {
            attribute.writeFromMapToInstance(attributes, instance);
        }
        return instance;
    }

    @Override
    public Map<String, AttributeValue> map(R value) {
        Map<String, AttributeValue> result = new HashMap<>();
        for (Attribute<R> attribute : attributes) {
            attribute.writeFromInstanceToMap(value, result);
        }
        return result;
    }

}

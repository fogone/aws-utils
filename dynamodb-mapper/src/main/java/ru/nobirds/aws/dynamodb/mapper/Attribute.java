package ru.nobirds.aws.dynamodb.mapper;

import java.util.Map;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public interface Attribute<R> {

    void writeFromMapToInstance(Map<String, AttributeValue> attributeValues, R instance);

    void writeFromInstanceToMap(R instance, Map<String, AttributeValue> result);
}

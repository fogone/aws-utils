package ru.nobirds.aws.dynamodb.mapper;

import java.util.Map;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public interface AttributesMapper<T> {

    T map(Map<String, AttributeValue> attributes);

    Map<String, AttributeValue> map(T value);

}


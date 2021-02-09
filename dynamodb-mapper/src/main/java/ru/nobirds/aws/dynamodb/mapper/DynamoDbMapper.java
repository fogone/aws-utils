package ru.nobirds.aws.dynamodb.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemResponse;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.dynamodb.model.WriteRequest;
import software.amazon.awssdk.services.dynamodb.model.WriteRequest.Builder;
import software.amazon.awssdk.services.dynamodb.paginators.QueryIterable;

@RequiredArgsConstructor
public class DynamoDbMapper {

    private final DynamoDbClient client;
    private final NamingStrategy namingStrategy;

    public <T> Stream<T> scan(String tableName, AttributesMapper<T> mapper) {
        return scan(tableName, mapper, builder -> {});
    }

    public <T> Stream<T> scan(String tableName, AttributesMapper<T> mapper,
        Consumer<ScanRequest.Builder> requestBuilder) {

        ScanResponse response = client.scan(requestBuilder
            .andThen(builder -> builder.tableName(namingStrategy.name(tableName))));

        // todo: paging?
        return response.items().stream().map(mapper::map);
    }

    public <T> Stream<T> query(String tableName, AttributesMapper<T> mapper) {
        return query(tableName, mapper, builder -> {});
    }

    public <T> Stream<T> query(String tableName, String indexName, AttributesMapper<T> mapper) {
        return query(tableName, mapper, builder -> builder.indexName(indexName));
    }

    public <T> Stream<T> query(String tableName, String indexName, AttributesMapper<T> mapper,
        Consumer<QueryRequest.Builder> requestBuilder) {
        return query(tableName, mapper, requestBuilder.andThen(b -> b.indexName(indexName)));
    }

    public <T> Stream<T> query(String tableName, AttributesMapper<T> mapper,
        Consumer<QueryRequest.Builder> requestBuilder) {

        QueryIterable queryResponses = client.queryPaginator(requestBuilder
            .andThen(builder -> builder.tableName(namingStrategy.name(tableName))));

        return queryResponses.stream().flatMap(queryResponse -> queryResponse.items().stream()).map(mapper::map);
    }

    public <T> Optional<T> get(String tableName, AttributesMapper<T> mapper, String key, AttributeValue value) {
        return get(tableName, mapper, Map.of(key, value));
    }

    public <T> Optional<T> get(String tableName, AttributesMapper<T> mapper,
        String key, AttributeValue value,
        String sortKey, AttributeValue sortValue
    ) {
        return get(tableName, mapper, Map.of(key, value, sortKey, sortValue));
    }

    public <T> Optional<T> get(String tableName, AttributesMapper<T> mapper, Map<String, AttributeValue> key) {
        GetItemResponse response = client.getItem(builder -> builder
            .tableName(namingStrategy.name(tableName))
            .key(key));

        return response.hasItem() ? Optional.of(mapper.map(response.item())) : Optional.empty();
    }

    public <T> T save(String tableName, AttributesMapper<T> mapper, T value) {
        return save(tableName, mapper, value, builder -> {});
    }

    public <T> T save(String tableName, AttributesMapper<T> mapper, T value,
        Consumer<PutItemRequest.Builder> saveBuilder) {

        client.putItem(saveBuilder
            .andThen(builder -> builder.tableName(namingStrategy.name(tableName)).item(mapper.map(value))));

        // todo: return mapper.map(response.attributes())
        return value;
    }

    public void delete(String tableName, Map<String, AttributeValue> key) {
        client.deleteItem(builder -> builder.tableName(tableName).key(key));
    }

    public void delete(String tableName,
        String key, AttributeValue value,
        String sortKey, AttributeValue sortValue
    ) {
        delete(tableName, Map.of(key, value, sortKey, sortValue));
    }

    public void delete(String tableName, String key, AttributeValue value) {
        delete(tableName, Map.of(key, value));
    }

    public void batchSave(String tableName, Consumer<BatchBuilder> batchBuilder) {
        BatchBuilder batch = new BatchBuilder();
        batchBuilder.accept(batch);
        List<ValueAndMapper<?>> items = batch.build();

        Builder writeItemBuilder = WriteRequest.builder().putRequest(builder -> {
            items.forEach(valueAndMapper -> {
                AttributesMapper<Object> mapper = (AttributesMapper<Object>) valueAndMapper.getMapper();
                builder.item(mapper.map(valueAndMapper.getValue()));
            });
        });

        List<WriteRequest> requests = List.of(writeItemBuilder.build());

        BatchWriteItemRequest request = BatchWriteItemRequest.builder()
            .requestItems(Map.of(tableName, requests)).build();

        client.batchWriteItem(request);
    }

    @Data
    private static class ValueAndMapper<T> {
        private final T value;
        private final AttributesMapper<T> mapper;
    }

    public static class BatchBuilder {
        private final List<ValueAndMapper<?>> values = new ArrayList<>();

        public <T> BatchBuilder item(T value, AttributesMapper<T> mapper) {
            this.values.add(new ValueAndMapper<>(value, mapper));
            return this;
        }

        private List<ValueAndMapper<?>> build() {
            return values;
        }
    }
}

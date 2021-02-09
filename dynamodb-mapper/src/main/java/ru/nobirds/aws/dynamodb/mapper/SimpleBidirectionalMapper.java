package ru.nobirds.aws.dynamodb.mapper;

import java.util.function.Function;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public
class SimpleBidirectionalMapper<T, R> implements BidirectionalMapper<T, R> {

    private final Function<T, R> reader;
    private final Function<R, T> writer;

    @Override
    public R read(T value) {
        return reader.apply(value);
    }

    @Override
    public T write(R value) {
        return writer.apply(value);
    }
}

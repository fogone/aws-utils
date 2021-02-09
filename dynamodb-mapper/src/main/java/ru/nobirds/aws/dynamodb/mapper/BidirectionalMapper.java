package ru.nobirds.aws.dynamodb.mapper;

import java.time.Instant;
import java.time.LocalDate;
import java.util.function.Function;

public interface BidirectionalMapper<T, R> {

    R read(T value);

    T write(R value);

    default <NR> BidirectionalMapper<T, NR> then(BidirectionalMapper<R, NR> mapper) {
        return of(value -> mapper.read(read(value)), value -> write(mapper.write(value)));
    }

    static <T, R> BidirectionalMapper<T, R> of(Function<T, R> reader, Function<R, T> writer) {
        return new SimpleBidirectionalMapper<>(reader, writer);
    }

    static BidirectionalMapper<String, String> hashed(String prefix) {
        return withPrefix(prefix + "#");
    }

    static BidirectionalMapper<String, String> withPrefix(String prefix) {
        return new PrefixedBidirectionalMapper(prefix);
    }

    BidirectionalMapper<String, LocalDate> STRING_TO_DATE = of(LocalDate::parse, LocalDate::toString);
    BidirectionalMapper<String, Instant> STRING_TO_INSTANT = BidirectionalMapper.of(Instant::parse, Instant::toString);
    BidirectionalMapper<String, Long> STRING_TO_NUMERIC = of(Long::parseLong, String::valueOf);
    BidirectionalMapper<String, Double> STRING_TO_DECIMAL = of(Double::parseDouble, String::valueOf);

}

package ru.nobirds.aws.dynamodb.mapper;

import java.util.function.BiConsumer;
import java.util.function.Function;
import lombok.Data;

@Data
public class Property<R, T> {

    private final String name;
    private final Function<R, T> getter;
    private final BiConsumer<R, T> setter;

}

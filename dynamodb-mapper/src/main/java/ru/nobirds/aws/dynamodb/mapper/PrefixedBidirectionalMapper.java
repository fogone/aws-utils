package ru.nobirds.aws.dynamodb.mapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PrefixedBidirectionalMapper implements BidirectionalMapper<String, String> {

    private final String prefix;

    @Override
    public String read(String value) {
        if (value == null) {
            return null;
        }

        int index = value.indexOf(prefix);

        return index > -1 ? value.substring(index + prefix.length()) : value; // todo: throw?
    }

    @Override
    public String write(String value) {
        if (value == null) {
            return null;
        }

        return prefix + value;
    }

}

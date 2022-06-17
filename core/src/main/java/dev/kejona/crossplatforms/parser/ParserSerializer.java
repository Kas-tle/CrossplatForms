package dev.projectg.crossplatforms.parser;

import dev.projectg.crossplatforms.serialize.ValuedTypeSerializer;

public class ParserSerializer extends ValuedTypeSerializer<Parser> {

    public ParserSerializer() {
        registerType(PlaceholderParser.TYPE, PlaceholderParser.class);
        registerType(BlockPlaceholderParser.TYPE, BlockPlaceholderParser.class);
        registerType(ReplacementParser.TYPE, ReplacementParser.class);
    }
}

/*
 * Copyright 2019 Miroslav Pokorny (github.com/mP1)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package walkingkooka.spreadsheet.server.parser;

import org.junit.jupiter.api.Test;
import walkingkooka.ToStringTesting;
import walkingkooka.collect.Range;
import walkingkooka.collect.set.Sets;
import walkingkooka.convert.Converter;
import walkingkooka.convert.ConverterContext;
import walkingkooka.convert.provider.ConverterName;
import walkingkooka.convert.provider.ConverterSelector;
import walkingkooka.net.UrlPath;
import walkingkooka.net.header.CharsetName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHttpEntityHandler;
import walkingkooka.net.http.server.hateos.HateosHttpEntityHandlerTesting;
import walkingkooka.net.http.server.hateos.HateosResourceMappings;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.format.SpreadsheetFormatter;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterProviderSamplesContext;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterSample;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterSelector;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterSelectorToken;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.parser.SpreadsheetParser;
import walkingkooka.spreadsheet.parser.provider.SpreadsheetParserName;
import walkingkooka.spreadsheet.parser.provider.SpreadsheetParserSelector;
import walkingkooka.spreadsheet.parser.provider.SpreadsheetParserSelectorToken;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelNameResolvers;
import walkingkooka.spreadsheet.server.FakeSpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.value.SpreadsheetCell;
import walkingkooka.storage.StoragePath;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContexts;
import walkingkooka.tree.text.TextNode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class SpreadsheetParserSelectorEditHateosHttpEntityHandlerTest implements HateosHttpEntityHandlerTesting<SpreadsheetParserSelectorEditHateosHttpEntityHandler, SpreadsheetParserName, SpreadsheetEngineHateosResourceHandlerContext>,
    SpreadsheetMetadataTesting,
    ToStringTesting<SpreadsheetParserSelectorEditHateosHttpEntityHandler> {

    private final static SpreadsheetParserName PARSER_NAME = SpreadsheetParserName.DATE;

    @Test
    public void testHandleOneFails() {
        this.handleOneFails(
            this.id(),
            this.entity(),
            this.parameters(),
            this.path(),
            this.context(),
            UnsupportedOperationException.class
        );
    }

    @Test
    public void testHandleManyFails() {
        this.handleManyFails(
            this.manyIds(),
            this.entity(),
            this.parameters(),
            this.path(),
            this.context(),
            UnsupportedOperationException.class
        );
    }

    @Test
    public void testHandleNoneFails() {
        this.handleNoneFails(
            this.entity(),
            this.parameters(),
            this.path(),
            this.context(),
            UnsupportedOperationException.class
        );
    }

    @Test
    public void testHandleRangeFails() {
        this.handleRangeFails(
            this.range(),
            this.entity(),
            this.parameters(),
            this.path(),
            this.context(),
            UnsupportedOperationException.class
        );
    }

    @Test
    public void testHandleAllBadAccept() {
        final IllegalArgumentException thrown = this.handleAllFails(
            this.entity()
                .setContentType(MediaType.APPLICATION_JSON)
                .setAccept(
                    MediaType.TEXT_PLAIN.accept()
                ),
            this.parameters(),
            this.path(),
            this.context(),
            IllegalArgumentException.class
        );
        this.checkEquals(
            "Accept: Got application/json require text/plain",
            thrown.getMessage()
        );
    }

    @Test
    public void testHandleAll() {
        this.handleAllAndCheck(
            // two format requests
            HttpEntity.EMPTY.setAccept(
                MediaType.APPLICATION_JSON.accept()
            ),
            this.parameters(),
            UrlPath.parse(
                "/" + SpreadsheetParserName.DATE + " dd/mm/yyyy"
            ),
            new FakeSpreadsheetEngineHateosResourceHandlerContext() {
                @Override
                public MediaType contentType() {
                    return MediaType.APPLICATION_JSON;
                }

                @Override
                public Optional<StoragePath> currentWorkingDirectory() {
                    return SpreadsheetParserSelectorEditHateosHttpEntityHandlerTest.CURRENT_WORKING_DIRECTORY;
                }

                @Override
                public Indentation indentation() {
                    return SpreadsheetParserSelectorEditHateosHttpEntityHandlerTest.INDENTATION;
                }

                @Override
                public LineEnding lineEnding() {
                    return LineEnding.NL;
                }

                @Override
                public SpreadsheetMetadata spreadsheetMetadata() {
                    return SpreadsheetMetadataTesting.METADATA_EN_AU;
                }

                @Override
                public <C extends ConverterContext> Converter<C> converter(final ConverterSelector selector,
                                                                           final ProviderContext context) {
                    return CONVERTER_PROVIDER.converter(
                        selector,
                        context
                    );
                }

                @Override
                public <C extends ConverterContext> Converter<C> converter(final ConverterName converterName,
                                                                           final List<?> values,
                                                                           final ProviderContext context) {
                    return CONVERTER_PROVIDER.converter(
                        converterName,
                        values,
                        context
                    );
                }

                @Override
                public SpreadsheetFormatter spreadsheetFormatter(final SpreadsheetFormatterSelector selector,
                                                                 final ProviderContext context) {
                    return SPREADSHEET_FORMATTER_PROVIDER.spreadsheetFormatter(
                        selector,
                        context
                    );
                }

                @Override
                public Optional<SpreadsheetFormatterSelectorToken> spreadsheetFormatterNextToken(final SpreadsheetFormatterSelector selector) {
                    return SPREADSHEET_FORMATTER_PROVIDER.spreadsheetFormatterNextToken(selector);
                }

                @Override
                public List<SpreadsheetFormatterSample> spreadsheetFormatterSamples(final SpreadsheetFormatterSelector selector,
                                                                                    final boolean includeSamples,
                                                                                    final SpreadsheetFormatterProviderSamplesContext context) {
                    return SPREADSHEET_FORMATTER_PROVIDER.spreadsheetFormatterSamples(
                        selector,
                        includeSamples,
                        context
                    );
                }

                @Override
                public Optional<SpreadsheetFormatterSelector> spreadsheetFormatterSelector(final SpreadsheetParserSelector selector) {
                    return SPREADSHEET_PARSER_PROVIDER.spreadsheetFormatterSelector(selector);
                }

                @Override
                public SpreadsheetParser spreadsheetParser(final SpreadsheetParserSelector selector,
                                                           final ProviderContext context) {
                    return SPREADSHEET_PARSER_PROVIDER.spreadsheetParser(
                        selector,
                        context
                    );
                }

                @Override
                public Optional<SpreadsheetParserSelectorToken> spreadsheetParserNextToken(final SpreadsheetParserSelector selector) {
                    return SPREADSHEET_PARSER_PROVIDER.spreadsheetParserNextToken(selector);
                }

                @Override
                public Optional<TextNode> formatValue(final SpreadsheetCell cell,
                                                      final Optional<Object> value,
                                                      final Optional<SpreadsheetFormatterSelector> formatter) {
                    return SPREADSHEET_PROVIDER.spreadsheetFormatter(
                        formatter.get(),
                        PROVIDER_CONTEXT
                    ).format(
                        value,
                        SpreadsheetMetadataTesting.METADATA_EN_AU.spreadsheetFormatterContext(
                            Optional.of(cell),
                            (final Optional<Object> v) -> {
                                throw new UnsupportedOperationException();
                            },
                            HAS_USER_DIRECTORIES,
                            SpreadsheetParserSelectorEditHateosHttpEntityHandlerTest.INDENTATION,
                            SpreadsheetLabelNameResolvers.fake(),
                            SpreadsheetMetadataTesting.LINE_ENDING,
                            CURRENCY_CONTEXT,
                            LOCALE_CONTEXT,
                            SPREADSHEET_PROVIDER,
                            PROVIDER_CONTEXT
                        )
                    );
                }

                @Override
                public JsonNode marshall(final Object value) {
                    return JsonNodeMarshallContexts.basic()
                        .marshall(value);
                }

                @Override
                public Locale locale() {
                    return SpreadsheetParserSelectorEditHateosHttpEntityHandlerTest.LOCALE;
                }

                @Override
                public LocalDateTime now() {
                    return HAS_NOW.now();
                }

                @Override
                public ProviderContext providerContext() {
                    return PROVIDER_CONTEXT;
                }
            },
            this.httpEntity(
                "{\n" +
                    "  \"selector\": \"date dd/mm/yyyy\",\n" +
                    "  \"message\": \"\",\n" +
                    "  \"tokens\": [\n" +
                    "    {\n" +
                    "      \"label\": \"dd\",\n" +
                    "      \"text\": \"dd\",\n" +
                    "      \"alternatives\": [\n" +
                    "        {\n" +
                    "          \"label\": \"d\",\n" +
                    "          \"text\": \"d\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"label\": \"ddd\",\n" +
                    "          \"text\": \"ddd\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"label\": \"dddd\",\n" +
                    "          \"text\": \"dddd\"\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"/\",\n" +
                    "      \"text\": \"/\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"mm\",\n" +
                    "      \"text\": \"mm\",\n" +
                    "      \"alternatives\": [\n" +
                    "        {\n" +
                    "          \"label\": \"m\",\n" +
                    "          \"text\": \"m\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"label\": \"mmm\",\n" +
                    "          \"text\": \"mmm\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"label\": \"mmmm\",\n" +
                    "          \"text\": \"mmmm\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"label\": \"mmmmm\",\n" +
                    "          \"text\": \"mmmmm\"\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"/\",\n" +
                    "      \"text\": \"/\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"yyyy\",\n" +
                    "      \"text\": \"yyyy\",\n" +
                    "      \"alternatives\": [\n" +
                    "        {\n" +
                    "          \"label\": \"yy\",\n" +
                    "          \"text\": \"yy\"\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"next\": {\n" +
                    "    \"alternatives\": [\n" +
                    "      {\n" +
                    "        \"label\": \"d\",\n" +
                    "        \"text\": \"d\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"label\": \"dd\",\n" +
                    "        \"text\": \"dd\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"label\": \"ddd\",\n" +
                    "        \"text\": \"ddd\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"label\": \"dddd\",\n" +
                    "        \"text\": \"dddd\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"label\": \"m\",\n" +
                    "        \"text\": \"m\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"label\": \"mm\",\n" +
                    "        \"text\": \"mm\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"label\": \"mmm\",\n" +
                    "        \"text\": \"mmm\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"label\": \"mmmm\",\n" +
                    "        \"text\": \"mmmm\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"label\": \"mmmmm\",\n" +
                    "        \"text\": \"mmmmm\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  \"samples\": [\n" +
                    "    {\n" +
                    "      \"label\": \"Short\",\n" +
                    "      \"selector\": \"date d/m/yy\",\n" +
                    "      \"value\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"31/12/99\"\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"Medium\",\n" +
                    "      \"selector\": \"date d mmm yyyy\",\n" +
                    "      \"value\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"31 Dec. 1999\"\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"Long\",\n" +
                    "      \"selector\": \"date d mmmm yyyy\",\n" +
                    "      \"value\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"31 December 1999\"\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"Full\",\n" +
                    "      \"selector\": \"date dddd, d mmmm yyyy\",\n" +
                    "      \"value\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Friday, 31 December 1999\"\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"Sample\",\n" +
                    "      \"selector\": \"date dd/mm/yyyy\",\n" +
                    "      \"value\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"31/12/1999\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}"
            ).addHeader(
                HateosResourceMappings.X_CONTENT_TYPE_NAME,
                SpreadsheetParserSelectorEdit.class.getSimpleName()
            )
        );
    }

    @Override
    public SpreadsheetParserSelectorEditHateosHttpEntityHandler createHandler() {
        return SpreadsheetParserSelectorEditHateosHttpEntityHandler.instance();
    }

    @Override
    public SpreadsheetParserName id() {
        return PARSER_NAME;
    }

    @Override
    public Set<SpreadsheetParserName> manyIds() {
        return Sets.of(
            PARSER_NAME
        );
    }

    @Override
    public Range<SpreadsheetParserName> range() {
        return Range.singleton(PARSER_NAME);
    }

    @Override
    public HttpEntity entity() {
        return HttpEntity.EMPTY;
    }

    @Override
    public Map<HttpRequestAttribute<?>, Object> parameters() {
        return HateosHttpEntityHandler.NO_PARAMETERS;
    }

    @Override
    public UrlPath path() {
        return UrlPath.EMPTY;
    }

    @Override
    public SpreadsheetEngineHateosResourceHandlerContext context() {
        return new FakeSpreadsheetEngineHateosResourceHandlerContext() {
            @Override
            public MediaType contentType() {
                return MediaType.APPLICATION_JSON;
            }
        };
    }

    private HttpEntity httpEntity(final String value) {
        return HttpEntity.EMPTY.setContentType(MediaType.APPLICATION_JSON.setCharset(CharsetName.UTF_8))
            .setBodyText(value)
            .setContentLength();
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(
            this.createHandler(),
            SpreadsheetParserSelectorEditHateosHttpEntityHandler.class.getSimpleName()
        );
    }

    // type name.........................................................................................................

    @Override
    public String typeNamePrefix() {
        return SpreadsheetParser.class.getSimpleName();
    }

    @Override
    public String typeNameSuffix() {
        return HateosHttpEntityHandler.class.getSimpleName();
    }

    // class............................................................................................................

    @Override
    public Class<SpreadsheetParserSelectorEditHateosHttpEntityHandler> type() {
        return SpreadsheetParserSelectorEditHateosHttpEntityHandler.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}

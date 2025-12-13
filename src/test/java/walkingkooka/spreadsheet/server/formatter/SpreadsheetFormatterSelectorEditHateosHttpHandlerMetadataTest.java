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

package walkingkooka.spreadsheet.server.formatter;

import org.junit.jupiter.api.Test;
import walkingkooka.ToStringTesting;
import walkingkooka.convert.Converter;
import walkingkooka.convert.ConverterContext;
import walkingkooka.convert.provider.ConverterName;
import walkingkooka.convert.provider.ConverterSelector;
import walkingkooka.net.Url;
import walkingkooka.net.header.HeaderException;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpProtocolVersion;
import walkingkooka.net.http.HttpTransport;
import walkingkooka.net.http.server.HttpRequests;
import walkingkooka.net.http.server.HttpResponses;
import walkingkooka.net.http.server.hateos.HateosHttpHandlerTesting;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.format.SpreadsheetFormatter;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterInfoSet;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterProviderSamplesContext;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterSample;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterSelector;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterSelectorToken;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.server.FakeSpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.value.SpreadsheetCell;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.text.TextNode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetFormatterSelectorEditHateosHttpHandlerMetadataTest implements HateosHttpHandlerTesting<SpreadsheetFormatterSelectorEditHateosHttpHandlerMetadata, SpreadsheetEngineHateosResourceHandlerContext>,
    SpreadsheetMetadataTesting,
    ToStringTesting<SpreadsheetFormatterSelectorEditHateosHttpHandlerMetadata> {

    @Test
    public void testHandleMissingAcceptApplicationJsonFails() {
        final HeaderException thrown = assertThrows(
            HeaderException.class,
            () -> this.createHateosHttpHandler()
                .handle(
                    HttpRequests.get(
                        HttpTransport.UNSECURED,
                        Url.parseRelative("/api/spreadsheet/1/metadata/formulaConverter/edit/date%20dd/mm/yyyy"),
                        HttpProtocolVersion.VERSION_1_0,
                        HttpEntity.EMPTY
                    ),
                    HttpResponses.recording(),
                    new FakeSpreadsheetEngineHateosResourceHandlerContext() {
                        @Override
                        public MediaType contentType() {
                            return MediaType.APPLICATION_JSON;
                        }
                    }
                )
        );

        this.checkEquals(
            "Missing header Accept",
            thrown.getMessage()
        );
    }

    @Test
    public void testHandleMissingSpreadsheetFormatterSelector() {
        this.handleAndCheck(
            HttpRequests.get(
                HttpTransport.UNSECURED,
                Url.parseRelative("/api/spreadsheet/1/metadata/formulaConverter/edit"),
                HttpProtocolVersion.VERSION_1_0,
                HttpEntity.EMPTY.setAccept(
                    MediaType.APPLICATION_JSON.accept()
                )
            ),
            this.context(),
            HttpResponses.parse(
                "HTTP/1.0 200 OK\r\n" +
                    "Content-Length: 33\r\n" +
                    "Content-Type: application/json; charset=UTF-8\r\n" +
                    "X-Content-Type-Name: SpreadsheetFormatterSelectorEdit\r\n" +
                    "\r\n" +
                    "{\n" +
                    "  \"message\": \"Empty \\\"text\\\"\"\n" +
                    "}"
            )
        );
    }

    @Test
    public void testHandleEmptySpreadsheetFormatterSelector() {
        this.handleAndCheck(
            HttpRequests.get(
                HttpTransport.UNSECURED,
                Url.parseRelative("/api/spreadsheet/1/metadata/formulaConverter/edit/"),
                HttpProtocolVersion.VERSION_1_0,
                HttpEntity.EMPTY.setAccept(
                    MediaType.APPLICATION_JSON.accept()
                )
            ),
            this.context(),
            HttpResponses.parse(
                "HTTP/1.0 200 OK\r\n" +
                    "Content-Length: 33\r\n" +
                    "Content-Type: application/json; charset=UTF-8\r\n" +
                    "X-Content-Type-Name: SpreadsheetFormatterSelectorEdit\r\n" +
                    "\r\n" +
                    "{\n" +
                    "  \"message\": \"Empty \\\"text\\\"\"\n" +
                    "}"
            )
        );
    }

    @Test
    public void testHandle() {
        this.handleAndCheck(
            HttpRequests.get(
                HttpTransport.UNSECURED,
                Url.parseRelative("/api/spreadsheet/1/metadata/formulaConverter/edit/date%20dd/mm/yyyy"),
                HttpProtocolVersion.VERSION_1_0,
                HttpEntity.EMPTY.setAccept(
                    MediaType.APPLICATION_JSON.accept()
                )
            ),
            this.context(),
            HttpResponses.parse(
                "HTTP/1.0 200 OK\r\n" +
                    "Content-Length: 2395\r\n" +
                    "Content-Type: application/json; charset=UTF-8\r\n" +
                    "X-Content-Type-Name: SpreadsheetFormatterSelectorEdit\r\n" +
                    "\r\n" +
                    "{\n" +
                    "  \"selector\": \"date dd/mm/yyyy\",\n" +
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
            )
        );
    }

    @Override
    public SpreadsheetFormatterSelectorEditHateosHttpHandlerMetadata createHateosHttpHandler() {
        return SpreadsheetFormatterSelectorEditHateosHttpHandlerMetadata.INSTANCE;
    }

    @Override
    public SpreadsheetEngineHateosResourceHandlerContext context() {
        return new FakeSpreadsheetEngineHateosResourceHandlerContext() {
            @Override
            public MediaType contentType() {
                return MediaType.APPLICATION_JSON;
            }

            @Override
            public Indentation indentation() {
                return INDENTATION;
            }

            @Override
            public LineEnding lineEnding() {
                return LineEnding.NL;
            }

            @Override
            public <T> T unmarshall(final JsonNode json,
                                    final Class<T> type) {
                return JSON_NODE_UNMARSHALL_CONTEXT.unmarshall(
                    json,
                    type
                );
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
            public SpreadsheetFormatterInfoSet spreadsheetFormatterInfos() {
                return SPREADSHEET_FORMATTER_PROVIDER.spreadsheetFormatterInfos();
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
                    SPREADSHEET_FORMATTER_CONTEXT
                );
            }

            @Override
            public Locale locale() {
                return SpreadsheetFormatterSelectorEditHateosHttpHandlerMetadataTest.LOCALE;
            }

            @Override
            public LocalDateTime now() {
                return HAS_NOW.now();
            }

            @Override
            public JsonNode marshall(final Object value) {
                return JSON_NODE_MARSHALL_CONTEXT.marshall(value);
            }

            @Override
            public ProviderContext providerContext() {
                return PROVIDER_CONTEXT;
            }
        };
    }

    @Override
    public void testTypeNaming() {
        throw new UnsupportedOperationException();
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(
            this.createHateosHttpHandler(),
            SpreadsheetFormatterSelectorEditHateosHttpHandlerMetadata.class.getSimpleName()
        );
    }

    // class............................................................................................................

    @Override
    public Class<SpreadsheetFormatterSelectorEditHateosHttpHandlerMetadata> type() {
        return SpreadsheetFormatterSelectorEditHateosHttpHandlerMetadata.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}

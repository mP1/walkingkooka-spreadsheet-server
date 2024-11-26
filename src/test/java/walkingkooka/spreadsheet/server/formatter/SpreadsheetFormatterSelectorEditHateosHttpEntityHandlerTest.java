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
import walkingkooka.collect.Range;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.set.Sets;
import walkingkooka.convert.Converter;
import walkingkooka.convert.ConverterContext;
import walkingkooka.convert.provider.ConverterName;
import walkingkooka.convert.provider.ConverterSelector;
import walkingkooka.net.header.Accept;
import walkingkooka.net.header.CharsetName;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHttpEntityHandler;
import walkingkooka.net.http.server.hateos.HateosHttpEntityHandlerTesting;
import walkingkooka.net.http.server.hateos.HateosResourceMapping;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.format.SpreadsheetFormatter;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterName;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProviderSamplesContext;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSample;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSelector;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSelectorToken;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.server.FakeSpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.text.TextNode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class SpreadsheetFormatterSelectorEditHateosHttpEntityHandlerTest implements HateosHttpEntityHandlerTesting<SpreadsheetFormatterSelectorEditHateosHttpEntityHandler, SpreadsheetFormatterName, SpreadsheetEngineHateosResourceHandlerContext>,
        SpreadsheetMetadataTesting,
        ToStringTesting<SpreadsheetFormatterSelectorEditHateosHttpEntityHandler> {

    private final static SpreadsheetFormatterName FORMATTER_NAME = SpreadsheetFormatterName.DATE_FORMAT_PATTERN;

    @Test
    public void testHandleOneFails() {
        this.handleOneFails(
                this.id(),
                this.entity(),
                this.parameters(),
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
                this.context(),
                UnsupportedOperationException.class
        );
    }

    @Test
    public void testHandleNoneFails() {
        this.handleNoneFails(
                this.entity(),
                this.parameters(),
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
                this.context(),
                UnsupportedOperationException.class
        );
    }

    @Test
    public void testHandleAllContentTypeBadContentType() {
        final IllegalArgumentException thrown = this.handleAllFails(
                this.entity()
                        .setHeader(
                                HttpHeaderName.CONTENT_TYPE,
                                Lists.of(MediaType.TEXT_PLAIN)
                        ),
                this.parameters(),
                this.context(),
                IllegalArgumentException.class
        );
        this.checkEquals(
                "Content-Type: Got text/plain require application/json",
                thrown.getMessage()
        );
    }

    @Test
    public void testHandleAllBadAccept() {
        final IllegalArgumentException thrown = this.handleAllFails(
                this.entity()
                        .setContentType(MediaType.APPLICATION_JSON)
                        .addHeader(
                                HttpHeaderName.ACCEPT,
                                Accept.parse("text/plain")
                        ),
                this.parameters(),
                this.context(),
                IllegalArgumentException.class
        );
        this.checkEquals(
                "Accept: Got text/plain require application/json",
                thrown.getMessage()
        );
    }

    @Test
    public void testHandleAll() {
        this.handleAllAndCheck(
                // two format requests
                this.httpEntity(
                        JsonNode.string("date-format-pattern dd/mm/yyyy").toString()
                ).addHeader(
                        HttpHeaderName.ACCEPT,
                        Accept.with(
                                Lists.of(MediaType.APPLICATION_JSON)
                        )
                ),
                this.parameters(),
                new FakeSpreadsheetEngineHateosResourceHandlerContext() {
                    @Override
                    public MediaType contentType() {
                        return MediaType.APPLICATION_JSON;
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
                    public List<SpreadsheetFormatterSample> spreadsheetFormatterSamples(final SpreadsheetFormatterName name,
                                                                                        final SpreadsheetFormatterProviderSamplesContext context) {
                        return SPREADSHEET_FORMATTER_PROVIDER.spreadsheetFormatterSamples(
                                name,
                                context
                        );
                    }

                    @Override
                    public Optional<TextNode> formatValue(final Object value,
                                                          final SpreadsheetFormatter formatter) {
                        return formatter.format(
                                value,
                                SPREADSHEET_FORMATTER_CONTEXT
                        );
                    }

                    @Override
                    public LocalDateTime now() {
                        return NOW.get();
                    }

                    @Override
                    public JsonNode marshall(final Object value) {
                        return JSON_NODE_MARSHALL_CONTEXT.marshall(value);
                    }
                },
                this.httpEntity(
                        "{\n" +
                                "  \"selector\": \"date-format-pattern dd/mm/yyyy\",\n" +
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
                                "      \"selector\": \"date-format-pattern d/m/yy\",\n" +
                                "      \"value\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"31/12/99\"\n" +
                                "      }\n" +
                                "    },\n" +
                                "    {\n" +
                                "      \"label\": \"Medium\",\n" +
                                "      \"selector\": \"date-format-pattern d mmm yyyy\",\n" +
                                "      \"value\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"31 Dec. 1999\"\n" +
                                "      }\n" +
                                "    },\n" +
                                "    {\n" +
                                "      \"label\": \"Long\",\n" +
                                "      \"selector\": \"date-format-pattern d mmmm yyyy\",\n" +
                                "      \"value\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"31 December 1999\"\n" +
                                "      }\n" +
                                "    },\n" +
                                "    {\n" +
                                "      \"label\": \"Full\",\n" +
                                "      \"selector\": \"date-format-pattern dddd, d mmmm yyyy\",\n" +
                                "      \"value\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Friday, 31 December 1999\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  ]\n" +
                                "}"
                ).addHeader(
                        HateosResourceMapping.X_CONTENT_TYPE_NAME,
                        SpreadsheetFormatterSelectorEdit.class.getSimpleName()
                )
        );
    }

    @Override
    public SpreadsheetFormatterSelectorEditHateosHttpEntityHandler createHandler() {
        return SpreadsheetFormatterSelectorEditHateosHttpEntityHandler.instance();
    }

    @Override
    public SpreadsheetFormatterName id() {
        return FORMATTER_NAME;
    }

    @Override
    public Set<SpreadsheetFormatterName> manyIds() {
        return Sets.of(
                FORMATTER_NAME
        );
    }

    @Override
    public Range<SpreadsheetFormatterName> range() {
        return Range.singleton(FORMATTER_NAME);
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
    public SpreadsheetEngineHateosResourceHandlerContext context() {
        return new FakeSpreadsheetEngineHateosResourceHandlerContext() {
            @Override
            public MediaType contentType() {
                return MediaType.APPLICATION_JSON;
            }
        };
    }

    private HttpEntity httpEntity(final String value) {
        return HttpEntity.EMPTY.setContentType(
                        MediaType.APPLICATION_JSON.setCharset(CharsetName.UTF_8)
                ).setBodyText(value)
                .setContentLength();
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(
                this.createHandler(),
                SpreadsheetFormatterSelectorEditHateosHttpEntityHandler.class.getSimpleName()
        );
    }

    // type name.........................................................................................................

    @Override
    public String typeNamePrefix() {
        return SpreadsheetFormatter.class.getSimpleName();
    }

    @Override
    public String typeNameSuffix() {
        return HateosHttpEntityHandler.class.getSimpleName();
    }

    // class............................................................................................................

    @Override
    public Class<SpreadsheetFormatterSelectorEditHateosHttpEntityHandler> type() {
        return SpreadsheetFormatterSelectorEditHateosHttpEntityHandler.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}

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
import walkingkooka.convert.provider.ConverterProvider;
import walkingkooka.convert.provider.ConverterSelector;
import walkingkooka.net.header.CharsetName;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHttpEntityHandler;
import walkingkooka.net.http.server.hateos.HateosHttpEntityHandlerTesting;
import walkingkooka.net.http.server.hateos.HateosResourceMapping;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.convert.SpreadsheetConvertersConverterProviders;
import walkingkooka.spreadsheet.format.SpreadsheetFormatter;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterName;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProvider;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProviders;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSample;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSelector;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSelectorTextComponent;
import walkingkooka.spreadsheet.format.edit.SpreadsheetFormatterSelectorEdit;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.parser.SpreadsheetParserProvider;
import walkingkooka.spreadsheet.parser.SpreadsheetParserProviders;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelNameResolvers;
import walkingkooka.spreadsheet.server.engine.FakeSpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.engine.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContexts;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContexts;
import walkingkooka.tree.text.TextNode;

import java.math.MathContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class SpreadsheetFormatterEditHateosHttpEntityHandlerTest implements HateosHttpEntityHandlerTesting<SpreadsheetFormatterEditHateosHttpEntityHandler, SpreadsheetFormatterName, SpreadsheetEngineHateosResourceHandlerContext>,
        SpreadsheetMetadataTesting,
        ToStringTesting<SpreadsheetFormatterEditHateosHttpEntityHandler> {

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
                "Got text/plain expected application/json",
                thrown.getMessage()
        );
    }

    @Test
    public void testHandleAll() {
        final SpreadsheetFormatterProvider spreadsheetFormatterProvider = SpreadsheetFormatterProviders.spreadsheetFormatPattern(
                Locale.forLanguageTag("EN-AU"),
                () -> LocalDateTime.of(
                        1999,
                        12,
                        31,
                        12,
                        58,
                        59
                )
        );
        final SpreadsheetParserProvider spreadsheetParserProvider = SpreadsheetParserProviders.spreadsheetParsePattern(spreadsheetFormatterProvider);
        final ConverterProvider converterProvider = SpreadsheetConvertersConverterProviders.spreadsheetConverters(
                SpreadsheetMetadataTesting.METADATA_EN_AU,
                spreadsheetFormatterProvider,
                spreadsheetParserProvider
        );

        this.handleAllAndCheck(
                // two format requests
                this.httpEntity(
                        JsonNode.string("date-format-pattern dd/mm/yyyy").toString()
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
                        return fromJson(json, type);
                    }

                    @Override
                    public SpreadsheetMetadata spreadsheetMetadata() {
                        return SpreadsheetMetadataTesting.METADATA_EN_AU;
                    }

                    @Override
                    public <C extends ConverterContext> Converter<C> converter(final ConverterSelector selector) {
                        return converterProvider.converter(selector);
                    }

                    @Override
                    public <C extends ConverterContext> Converter<C> converter(final ConverterName converterName,
                                                                               final List<?> values) {
                        return converterProvider.converter(
                                converterName,
                                values
                        );
                    }

                    @Override
                    public SpreadsheetFormatter spreadsheetFormatter(final SpreadsheetFormatterSelector spreadsheetFormatterSelector) {
                        return spreadsheetFormatterProvider.spreadsheetFormatter(spreadsheetFormatterSelector);
                    }

                    @Override
                    public Optional<SpreadsheetFormatterSelectorTextComponent> spreadsheetFormatterNextTextComponent(final SpreadsheetFormatterSelector selector) {
                        return spreadsheetFormatterProvider.spreadsheetFormatterNextTextComponent(selector);
                    }

                    @Override
                    public List<SpreadsheetFormatterSample<?>> spreadsheetFormatterSamples(final SpreadsheetFormatterName name) {
                        return spreadsheetFormatterProvider.spreadsheetFormatterSamples(name);
                    }

                    @Override
                    public Optional<TextNode> formatValue(final Object value,
                                                          final SpreadsheetFormatter formatter) {
                        return formatter.format(
                                value,
                                SpreadsheetMetadataTesting.METADATA_EN_AU.formatterContext(
                                        converterProvider,
                                        spreadsheetFormatterProvider,
                                        LocalDateTime::now,
                                        SpreadsheetLabelNameResolvers.fake()
                                )
                        );
                    }

                    @Override
                    public JsonNode marshall(final Object value) {
                        return JsonNodeMarshallContexts.basic()
                                .marshall(value);
                    }
                },
                this.httpEntity(
                        "{\n" +
                                "  \"selector\": \"date-format-pattern dd/mm/yyyy\",\n" +
                                "  \"message\": \"\",\n" +
                                "  \"textComponents\": [\n" +
                                "    {\n" +
                                "      \"type\": \"spreadsheet-formatter-selector-text-component\",\n" +
                                "      \"value\": {\n" +
                                "        \"label\": \"dd\",\n" +
                                "        \"text\": \"dd\",\n" +
                                "        \"alternatives\": [\n" +
                                "          {\n" +
                                "            \"label\": \"d\",\n" +
                                "            \"text\": \"d\"\n" +
                                "          },\n" +
                                "          {\n" +
                                "            \"label\": \"ddd\",\n" +
                                "            \"text\": \"ddd\"\n" +
                                "          },\n" +
                                "          {\n" +
                                "            \"label\": \"dddd\",\n" +
                                "            \"text\": \"dddd\"\n" +
                                "          }\n" +
                                "        ]\n" +
                                "      }\n" +
                                "    },\n" +
                                "    {\n" +
                                "      \"type\": \"spreadsheet-formatter-selector-text-component\",\n" +
                                "      \"value\": {\n" +
                                "        \"label\": \"/\",\n" +
                                "        \"text\": \"/\",\n" +
                                "        \"alternatives\": []\n" +
                                "      }\n" +
                                "    },\n" +
                                "    {\n" +
                                "      \"type\": \"spreadsheet-formatter-selector-text-component\",\n" +
                                "      \"value\": {\n" +
                                "        \"label\": \"mm\",\n" +
                                "        \"text\": \"mm\",\n" +
                                "        \"alternatives\": [\n" +
                                "          {\n" +
                                "            \"label\": \"m\",\n" +
                                "            \"text\": \"m\"\n" +
                                "          },\n" +
                                "          {\n" +
                                "            \"label\": \"mmm\",\n" +
                                "            \"text\": \"mmm\"\n" +
                                "          },\n" +
                                "          {\n" +
                                "            \"label\": \"mmmm\",\n" +
                                "            \"text\": \"mmmm\"\n" +
                                "          },\n" +
                                "          {\n" +
                                "            \"label\": \"mmmmm\",\n" +
                                "            \"text\": \"mmmmm\"\n" +
                                "          }\n" +
                                "        ]\n" +
                                "      }\n" +
                                "    },\n" +
                                "    {\n" +
                                "      \"type\": \"spreadsheet-formatter-selector-text-component\",\n" +
                                "      \"value\": {\n" +
                                "        \"label\": \"/\",\n" +
                                "        \"text\": \"/\",\n" +
                                "        \"alternatives\": []\n" +
                                "      }\n" +
                                "    },\n" +
                                "    {\n" +
                                "      \"type\": \"spreadsheet-formatter-selector-text-component\",\n" +
                                "      \"value\": {\n" +
                                "        \"label\": \"yyyy\",\n" +
                                "        \"text\": \"yyyy\",\n" +
                                "        \"alternatives\": [\n" +
                                "          {\n" +
                                "            \"label\": \"yy\",\n" +
                                "            \"text\": \"yy\"\n" +
                                "          }\n" +
                                "        ]\n" +
                                "      }\n" +
                                "    }\n" +
                                "  ],\n" +
                                "  \"next\": {\n" +
                                "    \"label\": \"\",\n" +
                                "    \"text\": \"\",\n" +
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
                                "        \"type\": \"local-date\",\n" +
                                "        \"value\": \"1999-12-31\"\n" +
                                "      }\n" +
                                "    },\n" +
                                "    {\n" +
                                "      \"label\": \"Medium\",\n" +
                                "      \"selector\": \"date-format-pattern d mmm yyyy\",\n" +
                                "      \"value\": {\n" +
                                "        \"type\": \"local-date\",\n" +
                                "        \"value\": \"1999-12-31\"\n" +
                                "      }\n" +
                                "    },\n" +
                                "    {\n" +
                                "      \"label\": \"Long\",\n" +
                                "      \"selector\": \"date-format-pattern d mmmm yyyy\",\n" +
                                "      \"value\": {\n" +
                                "        \"type\": \"local-date\",\n" +
                                "        \"value\": \"1999-12-31\"\n" +
                                "      }\n" +
                                "    },\n" +
                                "    {\n" +
                                "      \"label\": \"Full\",\n" +
                                "      \"selector\": \"date-format-pattern dddd, d mmmm yyyy\",\n" +
                                "      \"value\": {\n" +
                                "        \"type\": \"local-date\",\n" +
                                "        \"value\": \"1999-12-31\"\n" +
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
    public SpreadsheetFormatterEditHateosHttpEntityHandler createHandler() {
        return SpreadsheetFormatterEditHateosHttpEntityHandler.instance();
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
        return HttpEntity.EMPTY.addHeader(
                        HttpHeaderName.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON.setCharset(CharsetName.UTF_8)
                ).setBodyText(value)
                .setContentLength();
    }

    private <T> T fromJson(final JsonNode json,
                           final Class<T> type) {
        return JsonNodeUnmarshallContexts.basic(
                        ExpressionNumberKind.BIG_DECIMAL,
                        MathContext.DECIMAL32)
                .unmarshall(json, type);
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(
                this.createHandler(),
                SpreadsheetFormatterEditHateosHttpEntityHandler.class.getSimpleName()
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
    public Class<SpreadsheetFormatterEditHateosHttpEntityHandler> type() {
        return SpreadsheetFormatterEditHateosHttpEntityHandler.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}

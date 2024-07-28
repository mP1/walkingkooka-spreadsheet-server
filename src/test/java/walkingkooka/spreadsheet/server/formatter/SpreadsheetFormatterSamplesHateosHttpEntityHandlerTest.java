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
import walkingkooka.collect.set.Sets;
import walkingkooka.convert.Converter;
import walkingkooka.convert.ConverterContext;
import walkingkooka.convert.ConverterContexts;
import walkingkooka.convert.Converters;
import walkingkooka.convert.provider.ConverterName;
import walkingkooka.datetime.DateTimeContexts;
import walkingkooka.math.DecimalNumberContexts;
import walkingkooka.net.header.CharsetName;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHttpEntityHandler;
import walkingkooka.net.http.server.hateos.HateosHttpEntityHandlerTesting;
import walkingkooka.net.http.server.hateos.HateosResourceMapping;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.convert.SpreadsheetConverterContexts;
import walkingkooka.spreadsheet.convert.SpreadsheetConverters;
import walkingkooka.spreadsheet.format.SpreadsheetFormatter;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContext;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContexts;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterName;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSample;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSampleList;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSelector;
import walkingkooka.spreadsheet.format.SpreadsheetFormatters;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetPattern;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelNameResolvers;
import walkingkooka.spreadsheet.server.engine.FakeSpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.engine.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.tree.expression.ExpressionNumberConverterContext;
import walkingkooka.tree.expression.ExpressionNumberConverterContexts;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContexts;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContexts;
import walkingkooka.tree.text.TextNode;

import java.math.MathContext;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class SpreadsheetFormatterSamplesHateosHttpEntityHandlerTest implements HateosHttpEntityHandlerTesting<SpreadsheetFormatterSamplesHateosHttpEntityHandler, SpreadsheetFormatterName, SpreadsheetEngineHateosResourceHandlerContext>,
        ToStringTesting<SpreadsheetFormatterSamplesHateosHttpEntityHandler>,
        SpreadsheetMetadataTesting {

    private final static SpreadsheetFormatterName FORMATTER_NAME = SpreadsheetFormatterName.DATE_FORMAT_PATTERN;

    @Test
    public void testHandleAllFails() {
        this.handleAllFails(
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
    public void testHandleOne() {
        final SpreadsheetFormatterSelector selector = SpreadsheetPattern.parseDateFormatPattern("yyyy")
                .spreadsheetFormatterSelector();

        this.handleOneAndCheck(
                selector.name(), // resource id
                HttpEntity.EMPTY,
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
                    public List<SpreadsheetFormatterSample<?>> spreadsheetFormatterSamples(final SpreadsheetFormatterName name) {
                        return SPREADSHEET_FORMATTER_PROVIDER.spreadsheetFormatterSamples(selector.name());
                    }

                    @Override
                    public <C extends ConverterContext> Converter<C> converter(final ConverterName name,
                                                                               final List<?> values) {
                        return CONVERTER_PROVIDER.converter(
                                name,
                                values
                        );
                    }

                    @Override
                    public SpreadsheetFormatter spreadsheetFormatter(final SpreadsheetFormatterSelector spreadsheetFormatterSelector) {
                        return SPREADSHEET_FORMATTER_PROVIDER.spreadsheetFormatter(spreadsheetFormatterSelector);
                    }

                    @Override
                    public Optional<TextNode> formatValue(final Object value,
                                                          final SpreadsheetFormatter formatter) {
                        return formatter.format(
                                value,
                                SpreadsheetFormatterContexts.basic(
                                        (n) -> {
                                            throw new UnsupportedOperationException();
                                        },
                                        (n) -> {
                                            throw new UnsupportedOperationException();
                                        },
                                        1, // cellCharacterWidth
                                        SpreadsheetFormatterContext.DEFAULT_GENERAL_FORMAT_NUMBER_DIGIT_COUNT,
                                        SpreadsheetFormatters.fake(),
                                        SpreadsheetConverterContexts.basic(
                                                SpreadsheetConverters.basic(),
                                                SpreadsheetLabelNameResolvers.fake(),
                                                ExpressionNumberConverterContexts.basic(
                                                        Converters.fake()
                                                                .cast(ExpressionNumberConverterContext.class),
                                                        ConverterContexts.basic(
                                                                Converters.JAVA_EPOCH_OFFSET, // dateOffset
                                                                Converters.fake(),
                                                                DateTimeContexts.locale(
                                                                        Locale.forLanguageTag("EN-AU"),
                                                                        1950, // default year
                                                                        50,
                                                                        () -> {
                                                                            throw new UnsupportedOperationException();
                                                                        } // now
                                                                ),
                                                                DecimalNumberContexts.american(MathContext.DECIMAL32)
                                                        ),
                                                        ExpressionNumberKind.BIG_DECIMAL
                                                )
                                        )
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
                        "[\n" +
                                "  {\n" +
                                "    \"label\": \"Short\",\n" +
                                "    \"selector\": \"date-format-pattern d/m/yy\",\n" +
                                "    \"value\": {\n" +
                                "      \"type\": \"text\",\n" +
                                "      \"value\": \"31/12/99\"\n" +
                                "    }\n" +
                                "  },\n" +
                                "  {\n" +
                                "    \"label\": \"Medium\",\n" +
                                "    \"selector\": \"date-format-pattern d mmm yyyy\",\n" +
                                "    \"value\": {\n" +
                                "      \"type\": \"text\",\n" +
                                "      \"value\": \"31 Dec. 1999\"\n" +
                                "    }\n" +
                                "  },\n" +
                                "  {\n" +
                                "    \"label\": \"Long\",\n" +
                                "    \"selector\": \"date-format-pattern d mmmm yyyy\",\n" +
                                "    \"value\": {\n" +
                                "      \"type\": \"text\",\n" +
                                "      \"value\": \"31 December 1999\"\n" +
                                "    }\n" +
                                "  },\n" +
                                "  {\n" +
                                "    \"label\": \"Full\",\n" +
                                "    \"selector\": \"date-format-pattern dddd, d mmmm yyyy\",\n" +
                                "    \"value\": {\n" +
                                "      \"type\": \"text\",\n" +
                                "      \"value\": \"Friday, 31 December 1999\"\n" +
                                "    }\n" +
                                "  }\n" +
                                "]"
                ).addHeader(
                        HateosResourceMapping.X_CONTENT_TYPE_NAME,
                        SpreadsheetFormatterSampleList.class.getSimpleName()
                )
        );
    }

    @Override
    public SpreadsheetFormatterSamplesHateosHttpEntityHandler createHandler() {
        return SpreadsheetFormatterSamplesHateosHttpEntityHandler.instance();
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

    private HttpEntity httpEntity(final Object value) {
        return httpEntity(
                JsonNodeMarshallContexts.basic()
                        .marshall(
                                value
                        ).toString()
        );
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
                SpreadsheetFormatterSamplesHateosHttpEntityHandler.class.getSimpleName()
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
    public Class<SpreadsheetFormatterSamplesHateosHttpEntityHandler> type() {
        return SpreadsheetFormatterSamplesHateosHttpEntityHandler.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}

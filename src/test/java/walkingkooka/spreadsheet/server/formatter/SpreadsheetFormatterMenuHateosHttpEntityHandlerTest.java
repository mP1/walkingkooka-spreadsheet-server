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
import walkingkooka.convert.provider.ConverterName;
import walkingkooka.convert.provider.ConverterSelector;
import walkingkooka.net.header.Accept;
import walkingkooka.net.header.CharsetName;
import walkingkooka.net.header.HeaderException;
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
import walkingkooka.spreadsheet.format.SpreadsheetFormatterInfoSet;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterName;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProviderSamplesContext;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSample;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSelector;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSelectorToken;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.server.FakeSpreadsheetHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetHateosResourceHandlerContext;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.text.TextNode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class SpreadsheetFormatterMenuHateosHttpEntityHandlerTest implements HateosHttpEntityHandlerTesting<SpreadsheetFormatterMenuHateosHttpEntityHandler, SpreadsheetFormatterName, SpreadsheetHateosResourceHandlerContext>,
        SpreadsheetMetadataTesting,
        ToStringTesting<SpreadsheetFormatterMenuHateosHttpEntityHandler> {

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
    public void testHandleAllMissingAcceptApplicationJson() {
        final HeaderException thrown = this.handleAllFails(
                HttpEntity.EMPTY,
                this.parameters(),
                new FakeSpreadsheetHateosResourceHandlerContext() {
                    @Override
                    public MediaType contentType() {
                        return MediaType.APPLICATION_JSON;
                    }
                },
                HeaderException.class
        );
        this.checkEquals(
                "Required value is absent for Accept",
                thrown.getMessage()
        );
    }

    @Test
    public void testHandleAll() {
        this.handleAllAndCheck(
                HttpEntity.EMPTY.addHeader(
                        HttpHeaderName.ACCEPT,
                        Accept.parse(MediaType.APPLICATION_JSON.toHeaderText())
                ),
                this.parameters(),
                new FakeSpreadsheetHateosResourceHandlerContext() {
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
                    public SpreadsheetFormatterInfoSet spreadsheetFormatterInfos() {
                        return SPREADSHEET_FORMATTER_PROVIDER.spreadsheetFormatterInfos();
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
                        "[\n" +
                                "  {\n" +
                                "    \"label\": \"Short\",\n" +
                                "    \"selector\": \"date-format-pattern d/m/yy\"\n" +
                                "  },\n" +
                                "  {\n" +
                                "    \"label\": \"Medium\",\n" +
                                "    \"selector\": \"date-format-pattern d mmm yyyy\"\n" +
                                "  },\n" +
                                "  {\n" +
                                "    \"label\": \"Long\",\n" +
                                "    \"selector\": \"date-format-pattern d mmmm yyyy\"\n" +
                                "  },\n" +
                                "  {\n" +
                                "    \"label\": \"Full\",\n" +
                                "    \"selector\": \"date-format-pattern dddd, d mmmm yyyy\"\n" +
                                "  },\n" +
                                "  {\n" +
                                "    \"label\": \"Short\",\n" +
                                "    \"selector\": \"date-time-format-pattern d/m/yy, h:mm AM/PM\"\n" +
                                "  },\n" +
                                "  {\n" +
                                "    \"label\": \"Medium\",\n" +
                                "    \"selector\": \"date-time-format-pattern d mmm yyyy, h:mm:ss AM/PM\"\n" +
                                "  },\n" +
                                "  {\n" +
                                "    \"label\": \"Long\",\n" +
                                "    \"selector\": \"date-time-format-pattern d mmmm yyyy \\\\a\\\\t h:mm:ss AM/PM\"\n" +
                                "  },\n" +
                                "  {\n" +
                                "    \"label\": \"Full\",\n" +
                                "    \"selector\": \"date-time-format-pattern dddd, d mmmm yyyy \\\\a\\\\t h:mm:ss AM/PM\"\n" +
                                "  },\n" +
                                "  {\n" +
                                "    \"label\": \"General\",\n" +
                                "    \"selector\": \"general\"\n" +
                                "  },\n" +
                                "  {\n" +
                                "    \"label\": \"Number\",\n" +
                                "    \"selector\": \"number-format-pattern #,##0.###\"\n" +
                                "  },\n" +
                                "  {\n" +
                                "    \"label\": \"Integer\",\n" +
                                "    \"selector\": \"number-format-pattern #,##0\"\n" +
                                "  },\n" +
                                "  {\n" +
                                "    \"label\": \"Percent\",\n" +
                                "    \"selector\": \"number-format-pattern #,##0%\"\n" +
                                "  },\n" +
                                "  {\n" +
                                "    \"label\": \"Currency\",\n" +
                                "    \"selector\": \"number-format-pattern $#,##0.00\"\n" +
                                "  },\n" +
                                "  {\n" +
                                "    \"label\": \"Default\",\n" +
                                "    \"selector\": \"text-format-pattern @\"\n" +
                                "  },\n" +
                                "  {\n" +
                                "    \"label\": \"Short\",\n" +
                                "    \"selector\": \"time-format-pattern h:mm AM/PM\"\n" +
                                "  },\n" +
                                "  {\n" +
                                "    \"label\": \"Long\",\n" +
                                "    \"selector\": \"time-format-pattern h:mm:ss AM/PM\"\n" +
                                "  }\n" +
                                "]"
                ).addHeader(
                        HateosResourceMapping.X_CONTENT_TYPE_NAME,
                        SpreadsheetFormatterSelectorMenuList.class.getSimpleName()
                )
        );
    }

    @Override
    public SpreadsheetFormatterMenuHateosHttpEntityHandler createHandler() {
        return SpreadsheetFormatterMenuHateosHttpEntityHandler.instance();
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
    public SpreadsheetHateosResourceHandlerContext context() {
        return new FakeSpreadsheetHateosResourceHandlerContext() {
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
                SpreadsheetFormatterMenuHateosHttpEntityHandler.class.getSimpleName()
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
    public Class<SpreadsheetFormatterMenuHateosHttpEntityHandler> type() {
        return SpreadsheetFormatterMenuHateosHttpEntityHandler.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}

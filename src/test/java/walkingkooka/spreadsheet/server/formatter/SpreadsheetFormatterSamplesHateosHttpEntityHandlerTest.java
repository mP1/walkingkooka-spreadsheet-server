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
import walkingkooka.Either;
import walkingkooka.ToStringTesting;
import walkingkooka.collect.Range;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.set.Sets;
import walkingkooka.convert.Converter;
import walkingkooka.convert.ConverterContext;
import walkingkooka.convert.provider.ConverterName;
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
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSampleList;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSelector;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetPattern;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.server.FakeSpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.text.TextNode;

import java.time.LocalDateTime;
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
    public void testHandleOneBadAcceptFails() {
        final IllegalArgumentException thrown = this.handleOneFails(
                this.id(),
                this.entity()
                        .addHeader(
                                HttpHeaderName.ACCEPT,
                                Accept.with(
                                        Lists.of(
                                                MediaType.IMAGE_BMP
                                        )
                                )
                        ),
                this.parameters(),
                this.context(),
                IllegalArgumentException.class
        );

        this.checkEquals(
                "Accept: Got image/bmp require application/json",
                thrown.getMessage()
        );
    }

    @Test
    public void testHandleOne() {
        final SpreadsheetFormatterSelector selector = SpreadsheetPattern.parseDateFormatPattern("yyyy")
                .spreadsheetFormatterSelector();

        this.handleOneAndCheck(
                selector.name(), // resource id
                HttpEntity.EMPTY.addHeader(
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
                    public List<SpreadsheetFormatterSample> spreadsheetFormatterSamples(final SpreadsheetFormatterName name,
                                                                                        final SpreadsheetFormatterProviderSamplesContext context) {
                        return SPREADSHEET_FORMATTER_PROVIDER.spreadsheetFormatterSamples(
                                name,
                                context
                        );
                    }

                    @Override
                    public <C extends ConverterContext> Converter<C> converter(final ConverterName name,
                                                                               final List<?> values,
                                                                               final ProviderContext context) {
                        return CONVERTER_PROVIDER.converter(
                                name,
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
                    public Locale locale() {
                        return LOCALE;
                    }

                    @Override
                    public LocalDateTime now() {
                        return NOW.get();
                    }

                    @Override
                    public <T> Either<T, String> convert(final Object value,
                                                         final Class<T> type) {
                        return SPREADSHEET_FORMULA_CONVERTER_CONTEXT.convert(
                                value,
                                type
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
                    public List<String> ampms() {
                        return SPREADSHEET_FORMATTER_CONTEXT.ampms();
                    }

                    @Override
                    public String ampm(final int hourOfDay) {
                        return SPREADSHEET_FORMATTER_CONTEXT.ampm(hourOfDay);
                    }

                    @Override
                    public List<String> monthNames() {
                        return SPREADSHEET_FORMATTER_CONTEXT.monthNames();
                    }

                    @Override
                    public String monthName(final int month) {
                        return SPREADSHEET_FORMATTER_CONTEXT.monthName(month);
                    }

                    @Override
                    public List<String> monthNameAbbreviations() {
                        return SPREADSHEET_FORMATTER_CONTEXT.monthNameAbbreviations();
                    }

                    @Override
                    public String monthNameAbbreviation(final int month) {
                        return SPREADSHEET_FORMATTER_CONTEXT.monthNameAbbreviation(month);
                    }

                    @Override
                    public List<String> weekDayNames() {
                        return SPREADSHEET_FORMATTER_CONTEXT.weekDayNames();
                    }

                    @Override
                    public String weekDayName(final int day) {
                        return SPREADSHEET_FORMATTER_CONTEXT.weekDayName(day);
                    }

                    @Override
                    public List<String> weekDayNameAbbreviations() {
                        return SPREADSHEET_FORMATTER_CONTEXT.weekDayNameAbbreviations();
                    }

                    @Override
                    public String weekDayNameAbbreviation(final int day) {
                        return SPREADSHEET_FORMATTER_CONTEXT.weekDayNameAbbreviation(day);
                    }

                    @Override
                    public int defaultYear() {
                        return SPREADSHEET_FORMATTER_CONTEXT.defaultYear();
                    }

                    @Override
                    public int twoDigitYear() {
                        return SPREADSHEET_FORMATTER_CONTEXT.twoDigitYear();
                    }

                    @Override
                    public int twoToFourDigitYear(final int year) {
                        return SPREADSHEET_FORMATTER_CONTEXT.twoToFourDigitYear(year);
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

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
import walkingkooka.net.header.CharsetName;
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
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.format.SpreadsheetFormatter;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterInfoSet;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetFormatterMenuHateosHttpHandlerTest implements HateosHttpHandlerTesting<SpreadsheetFormatterMenuHateosHttpHandler, SpreadsheetEngineHateosResourceHandlerContext>,
    SpreadsheetMetadataTesting,
    ToStringTesting<SpreadsheetFormatterMenuHateosHttpHandler> {

    @Test
    public void testHandleMissingAcceptApplicationJsonFails() {
        final HeaderException thrown = assertThrows(
            HeaderException.class,
            () -> this.createHateosHttpHandler()
                .handle(
                    HttpRequests.get(
                        HttpTransport.UNSECURED,
                        Url.parseRelative("/api/spreadsheet/1/formatter/*/menu"),
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
    public void testHandle() {
        this.handleAndCheck(
            HttpRequests.get(
                HttpTransport.UNSECURED,
                Url.parseRelative("/api/spreadsheet/1/formatter/*/menu"),
                HttpProtocolVersion.VERSION_1_0,
                HttpEntity.EMPTY.setAccept(
                    MediaType.APPLICATION_JSON.accept()
                )
            ),
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
                public LocalDateTime now() {
                    return NOW.now();
                }

                @Override
                public JsonNode marshall(final Object value) {
                    return JSON_NODE_MARSHALL_CONTEXT.marshall(value);
                }
            },
            HttpResponses.parse(
                "HTTP/1.0 200 OK\r\n" +
                "Content-Length: 1429\r\n" +
                "Content-Type: application/json; charset=UTF-8\r\n" +
                "X-Content-Type-Name: SpreadsheetFormatterSelectorMenuList\r\n" +
                "\r\n" +
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
                "    \"label\": \"Default\",\n" +
                "    \"selector\": \"text-format-pattern @\"\n" +
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
            )
        );
    }

    @Override
    public SpreadsheetFormatterMenuHateosHttpHandler createHateosHttpHandler() {
        return SpreadsheetFormatterMenuHateosHttpHandler.INSTANCE;
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
            this.createHateosHttpHandler(),
            SpreadsheetFormatterMenuHateosHttpHandler.class.getSimpleName()
        );
    }

    // type name.........................................................................................................

    @Override
    public String typeNamePrefix() {
        return SpreadsheetFormatter.class.getSimpleName();
    }

    // class............................................................................................................

    @Override
    public Class<SpreadsheetFormatterMenuHateosHttpHandler> type() {
        return SpreadsheetFormatterMenuHateosHttpHandler.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}

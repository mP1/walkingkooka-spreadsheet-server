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

package walkingkooka.spreadsheet.server.datetimesymbols;

import org.junit.jupiter.api.Test;
import walkingkooka.datetime.DateTimeSymbols;
import walkingkooka.net.Url;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpProtocolVersion;
import walkingkooka.net.http.HttpStatusCode;
import walkingkooka.net.http.HttpTransport;
import walkingkooka.net.http.server.HttpRequests;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.HttpResponses;
import walkingkooka.net.http.server.hateos.HateosHttpHandlerTesting;
import walkingkooka.net.http.server.hateos.HateosResourceMappings;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.server.locale.FakeLocaleHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.locale.LocaleHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.net.SpreadsheetServerMediaTypes;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;
import walkingkooka.tree.json.JsonNode;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public final class DateTimeSymbolsFindByLocaleStartsWithHateosHttpHandlerTest implements HateosHttpHandlerTesting<DateTimeSymbolsFindByLocaleStartsWithHateosHttpHandler, LocaleHateosResourceHandlerContext>,
    SpreadsheetMetadataTesting {

    @Test
    public void testHandleGetAcceptApplicationJsonOffsetZeroCountOne() {
        final HttpResponse response = HttpResponses.recording();
        response.setStatus(HttpStatusCode.OK.status());
        response.setEntity(
            HttpEntity.EMPTY.setContentType(
                SpreadsheetServerMediaTypes.CONTENT_TYPE
            ).addHeader(
                HateosResourceMappings.X_CONTENT_TYPE_NAME,
                DateTimeSymbolsHateosResourceSet.class.getSimpleName()
            ).setBodyText(
                "[\n" +
                    "  {\n" +
                    "    \"localeTag\": \"en\",\n" +
                    "    \"text\": \"English\",\n" +
                    "    \"dateTimeSymbols\": {\n" +
                    "      \"ampms\": [\n" +
                    "        \"AM\",\n" +
                    "        \"PM\"\n" +
                    "      ],\n" +
                    "      \"monthNames\": [\n" +
                    "        \"January\",\n" +
                    "        \"February\",\n" +
                    "        \"March\",\n" +
                    "        \"April\",\n" +
                    "        \"May\",\n" +
                    "        \"June\",\n" +
                    "        \"July\",\n" +
                    "        \"August\",\n" +
                    "        \"September\",\n" +
                    "        \"October\",\n" +
                    "        \"November\",\n" +
                    "        \"December\"\n" +
                    "      ],\n" +
                    "      \"monthNameAbbreviations\": [\n" +
                    "        \"Jan\",\n" +
                    "        \"Feb\",\n" +
                    "        \"Mar\",\n" +
                    "        \"Apr\",\n" +
                    "        \"May\",\n" +
                    "        \"Jun\",\n" +
                    "        \"Jul\",\n" +
                    "        \"Aug\",\n" +
                    "        \"Sep\",\n" +
                    "        \"Oct\",\n" +
                    "        \"Nov\",\n" +
                    "        \"Dec\"\n" +
                    "      ],\n" +
                    "      \"weekDayNames\": [\n" +
                    "        \"Sunday\",\n" +
                    "        \"Monday\",\n" +
                    "        \"Tuesday\",\n" +
                    "        \"Wednesday\",\n" +
                    "        \"Thursday\",\n" +
                    "        \"Friday\",\n" +
                    "        \"Saturday\"\n" +
                    "      ],\n" +
                    "      \"weekDayNameAbbreviations\": [\n" +
                    "        \"Sun\",\n" +
                    "        \"Mon\",\n" +
                    "        \"Tue\",\n" +
                    "        \"Wed\",\n" +
                    "        \"Thu\",\n" +
                    "        \"Fri\",\n" +
                    "        \"Sat\"\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"localeTag\": \"en-001\",\n" +
                    "    \"text\": \"English (World)\",\n" +
                    "    \"dateTimeSymbols\": {\n" +
                    "      \"ampms\": [\n" +
                    "        \"AM\",\n" +
                    "        \"PM\"\n" +
                    "      ],\n" +
                    "      \"monthNames\": [\n" +
                    "        \"January\",\n" +
                    "        \"February\",\n" +
                    "        \"March\",\n" +
                    "        \"April\",\n" +
                    "        \"May\",\n" +
                    "        \"June\",\n" +
                    "        \"July\",\n" +
                    "        \"August\",\n" +
                    "        \"September\",\n" +
                    "        \"October\",\n" +
                    "        \"November\",\n" +
                    "        \"December\"\n" +
                    "      ],\n" +
                    "      \"monthNameAbbreviations\": [\n" +
                    "        \"Jan\",\n" +
                    "        \"Feb\",\n" +
                    "        \"Mar\",\n" +
                    "        \"Apr\",\n" +
                    "        \"May\",\n" +
                    "        \"Jun\",\n" +
                    "        \"Jul\",\n" +
                    "        \"Aug\",\n" +
                    "        \"Sep\",\n" +
                    "        \"Oct\",\n" +
                    "        \"Nov\",\n" +
                    "        \"Dec\"\n" +
                    "      ],\n" +
                    "      \"weekDayNames\": [\n" +
                    "        \"Sunday\",\n" +
                    "        \"Monday\",\n" +
                    "        \"Tuesday\",\n" +
                    "        \"Wednesday\",\n" +
                    "        \"Thursday\",\n" +
                    "        \"Friday\",\n" +
                    "        \"Saturday\"\n" +
                    "      ],\n" +
                    "      \"weekDayNameAbbreviations\": [\n" +
                    "        \"Sun\",\n" +
                    "        \"Mon\",\n" +
                    "        \"Tue\",\n" +
                    "        \"Wed\",\n" +
                    "        \"Thu\",\n" +
                    "        \"Fri\",\n" +
                    "        \"Sat\"\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  }\n" +
                    "]"
            ).setContentLength()
        );

        this.handleAndCheck(
            HttpRequests.get(
                HttpTransport.UNSECURED,
                Url.parseRelative("/api/dateTimeSymbols/*/localeStartsWith/English?offset=0&count=2"),
                HttpProtocolVersion.VERSION_1_0,
                HttpEntity.EMPTY.setAccept(
                    SpreadsheetServerMediaTypes.CONTENT_TYPE.accept()
                )
            ),
            this.context(),
            response
        );
    }

    @Test
    public void testHandleGetAcceptApplicationJsonOffset7Count1() {
        final HttpResponse response = HttpResponses.recording();
        response.setStatus(HttpStatusCode.OK.status());
        response.setEntity(
            HttpEntity.EMPTY.setContentType(
                SpreadsheetServerMediaTypes.CONTENT_TYPE
            ).addHeader(
                HateosResourceMappings.X_CONTENT_TYPE_NAME,
                DateTimeSymbolsHateosResourceSet.class.getSimpleName()
            ).setBodyText(
                "[\n" +
                    "  {\n" +
                    "    \"localeTag\": \"en-AU\",\n" +
                    "    \"text\": \"English (Australia)\",\n" +
                    "    \"dateTimeSymbols\": {\n" +
                    "      \"ampms\": [\n" +
                    "        \"am\",\n" +
                    "        \"pm\"\n" +
                    "      ],\n" +
                    "      \"monthNames\": [\n" +
                    "        \"January\",\n" +
                    "        \"February\",\n" +
                    "        \"March\",\n" +
                    "        \"April\",\n" +
                    "        \"May\",\n" +
                    "        \"June\",\n" +
                    "        \"July\",\n" +
                    "        \"August\",\n" +
                    "        \"September\",\n" +
                    "        \"October\",\n" +
                    "        \"November\",\n" +
                    "        \"December\"\n" +
                    "      ],\n" +
                    "      \"monthNameAbbreviations\": [\n" +
                    "        \"Jan.\",\n" +
                    "        \"Feb.\",\n" +
                    "        \"Mar.\",\n" +
                    "        \"Apr.\",\n" +
                    "        \"May\",\n" +
                    "        \"Jun.\",\n" +
                    "        \"Jul.\",\n" +
                    "        \"Aug.\",\n" +
                    "        \"Sep.\",\n" +
                    "        \"Oct.\",\n" +
                    "        \"Nov.\",\n" +
                    "        \"Dec.\"\n" +
                    "      ],\n" +
                    "      \"weekDayNames\": [\n" +
                    "        \"Sunday\",\n" +
                    "        \"Monday\",\n" +
                    "        \"Tuesday\",\n" +
                    "        \"Wednesday\",\n" +
                    "        \"Thursday\",\n" +
                    "        \"Friday\",\n" +
                    "        \"Saturday\"\n" +
                    "      ],\n" +
                    "      \"weekDayNameAbbreviations\": [\n" +
                    "        \"Sun.\",\n" +
                    "        \"Mon.\",\n" +
                    "        \"Tue.\",\n" +
                    "        \"Wed.\",\n" +
                    "        \"Thu.\",\n" +
                    "        \"Fri.\",\n" +
                    "        \"Sat.\"\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  }\n" +
                    "]"
            ).setContentLength()
        );

        this.handleAndCheck(
            HttpRequests.get(
                HttpTransport.UNSECURED,
                Url.parseRelative("/api/dateTimeSymbols/*/localeStartsWith/English?offset=7&count=1"),
                HttpProtocolVersion.VERSION_1_0,
                HttpEntity.EMPTY.setAccept(
                    SpreadsheetServerMediaTypes.CONTENT_TYPE.accept()
                )
            ),
            this.context(),
            response
        );
    }

    @Override
    public DateTimeSymbolsFindByLocaleStartsWithHateosHttpHandler createHateosHttpHandler() {
        return DateTimeSymbolsFindByLocaleStartsWithHateosHttpHandler.INSTANCE;
    }

    @Override
    public FakeLocaleHateosResourceHandlerContext context() {
        return new FakeLocaleHateosResourceHandlerContext() {
            @Override
            public MediaType contentType() {
                return SpreadsheetServerMediaTypes.CONTENT_TYPE;
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
            public Locale locale() {
                return LOCALE;
            }

            @Override
            public Optional<DateTimeSymbols> dateTimeSymbolsForLocale(final Locale locale) {
                return LOCALE_CONTEXT.dateTimeSymbolsForLocale(locale);
            }

            @Override
            public Set<Locale> findByLocaleText(final String text,
                                                final int offset,
                                                final int count,
                                                final Locale locale) {
                return LOCALE_CONTEXT.findByLocaleText(
                    text,
                    offset,
                    count,
                    locale
                );
            }

            @Override
            public Optional<String> localeText(final Locale locale,
                                               final Locale requestedLocale) {
                return LOCALE_CONTEXT.localeText(
                    locale,
                    requestedLocale
                );
            }

            @Override
            public JsonNode marshall(final Object value) {
                return JSON_NODE_MARSHALL_CONTEXT.marshall(value);
            }
        };
    }

    // class............................................................................................................

    @Override
    public Class<DateTimeSymbolsFindByLocaleStartsWithHateosHttpHandler> type() {
        return DateTimeSymbolsFindByLocaleStartsWithHateosHttpHandler.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}

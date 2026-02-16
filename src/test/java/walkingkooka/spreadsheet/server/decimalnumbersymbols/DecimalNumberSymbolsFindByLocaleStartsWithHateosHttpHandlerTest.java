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

package walkingkooka.spreadsheet.server.decimalnumbersymbols;

import org.junit.jupiter.api.Test;
import walkingkooka.math.DecimalNumberSymbols;
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

public final class DecimalNumberSymbolsFindByLocaleStartsWithHateosHttpHandlerTest implements HateosHttpHandlerTesting<DecimalNumberSymbolsFindByLocaleStartsWithHateosHttpHandler, LocaleHateosResourceHandlerContext>,
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
                DecimalNumberSymbolsHateosResourceSet.class.getSimpleName()
            ).setBodyText(
                "[\n" +
                    "  {\n" +
                    "    \"localeTag\": \"en\",\n" +
                    "    \"text\": \"English\",\n" +
                    "    \"decimalNumberSymbols\": {\n" +
                    "      \"negativeSign\": \"-\",\n" +
                    "      \"positiveSign\": \"+\",\n" +
                    "      \"zeroDigit\": \"0\",\n" +
                    "      \"currencySymbol\": \"¤\",\n" +
                    "      \"decimalSeparator\": \".\",\n" +
                    "      \"exponentSymbol\": \"E\",\n" +
                    "      \"groupSeparator\": \",\",\n" +
                    "      \"infinitySymbol\": \"∞\",\n" +
                    "      \"monetaryDecimalSeparator\": \".\",\n" +
                    "      \"nanSymbol\": \"NaN\",\n" +
                    "      \"percentSymbol\": \"%\",\n" +
                    "      \"permillSymbol\": \"‰\"\n" +
                    "    }\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"localeTag\": \"en-001\",\n" +
                    "    \"text\": \"English (World)\",\n" +
                    "    \"decimalNumberSymbols\": {\n" +
                    "      \"negativeSign\": \"-\",\n" +
                    "      \"positiveSign\": \"+\",\n" +
                    "      \"zeroDigit\": \"0\",\n" +
                    "      \"currencySymbol\": \"¤\",\n" +
                    "      \"decimalSeparator\": \".\",\n" +
                    "      \"exponentSymbol\": \"E\",\n" +
                    "      \"groupSeparator\": \",\",\n" +
                    "      \"infinitySymbol\": \"∞\",\n" +
                    "      \"monetaryDecimalSeparator\": \".\",\n" +
                    "      \"nanSymbol\": \"NaN\",\n" +
                    "      \"percentSymbol\": \"%\",\n" +
                    "      \"permillSymbol\": \"‰\"\n" +
                    "    }\n" +
                    "  }\n" +
                    "]"
            ).setContentLength()
        );

        this.handleAndCheck(
            HttpRequests.get(
                HttpTransport.UNSECURED,
                Url.parseRelative("/api/decimalNumberSymbols/*/localeStartsWith/English?offset=0&count=2"),
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
                DecimalNumberSymbolsHateosResourceSet.class.getSimpleName()
            ).setBodyText(
                "[\n" +
                    "  {\n" +
                    "    \"localeTag\": \"en-AU\",\n" +
                    "    \"text\": \"English (Australia)\",\n" +
                    "    \"decimalNumberSymbols\": {\n" +
                    "      \"negativeSign\": \"-\",\n" +
                    "      \"positiveSign\": \"+\",\n" +
                    "      \"zeroDigit\": \"0\",\n" +
                    "      \"currencySymbol\": \"$\",\n" +
                    "      \"decimalSeparator\": \".\",\n" +
                    "      \"exponentSymbol\": \"e\",\n" +
                    "      \"groupSeparator\": \",\",\n" +
                    "      \"infinitySymbol\": \"∞\",\n" +
                    "      \"monetaryDecimalSeparator\": \".\",\n" +
                    "      \"nanSymbol\": \"NaN\",\n" +
                    "      \"percentSymbol\": \"%\",\n" +
                    "      \"permillSymbol\": \"‰\"\n" +
                    "    }\n" +
                    "  }\n" +
                    "]"
            ).setContentLength()
        );

        this.handleAndCheck(
            HttpRequests.get(
                HttpTransport.UNSECURED,
                Url.parseRelative("/api/decimalNumberSymbols/*/localeStartsWith/English?offset=7&count=1"),
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
    public DecimalNumberSymbolsFindByLocaleStartsWithHateosHttpHandler createHateosHttpHandler() {
        return DecimalNumberSymbolsFindByLocaleStartsWithHateosHttpHandler.INSTANCE;
    }

    @Override
    public LocaleHateosResourceHandlerContext context() {
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
            public Optional<DecimalNumberSymbols> decimalNumberSymbolsForLocale(final Locale locale) {
                return LOCALE_CONTEXT.decimalNumberSymbolsForLocale(locale);
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
    public Class<DecimalNumberSymbolsFindByLocaleStartsWithHateosHttpHandler> type() {
        return DecimalNumberSymbolsFindByLocaleStartsWithHateosHttpHandler.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}

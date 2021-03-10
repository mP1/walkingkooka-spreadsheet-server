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

package walkingkooka.spreadsheet.server;

import org.junit.jupiter.api.Test;
import walkingkooka.collect.list.Lists;
import walkingkooka.math.Fraction;
import walkingkooka.net.Url;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.HttpProtocolVersion;
import walkingkooka.net.http.HttpStatus;
import walkingkooka.net.http.HttpStatusCode;
import walkingkooka.net.http.HttpTransport;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequests;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.HttpResponses;
import walkingkooka.net.http.server.hateos.HateosContentType;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.format.SpreadsheetText;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetFormatPattern;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetParsePatterns;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetPattern;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStores;
import walkingkooka.spreadsheet.reference.store.SpreadsheetExpressionReferenceStores;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStores;
import walkingkooka.spreadsheet.reference.store.SpreadsheetRangeStores;
import walkingkooka.spreadsheet.security.store.SpreadsheetGroupStores;
import walkingkooka.spreadsheet.security.store.SpreadsheetUserStores;
import walkingkooka.spreadsheet.server.format.SpreadsheetFormatRequest;
import walkingkooka.spreadsheet.server.format.SpreadsheetLocaleDefaultDateTimeFormat;
import walkingkooka.spreadsheet.server.format.SpreadsheetMultiFormatRequest;
import walkingkooka.spreadsheet.server.format.SpreadsheetMultiFormatResponse;
import walkingkooka.spreadsheet.server.parse.SpreadsheetMultiParseRequest;
import walkingkooka.spreadsheet.server.parse.SpreadsheetMultiParseResponse;
import walkingkooka.spreadsheet.server.parse.SpreadsheetParseRequest;
import walkingkooka.spreadsheet.store.SpreadsheetCellStores;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepositories;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.tree.expression.ExpressionNumberContexts;
import walkingkooka.tree.expression.FunctionExpressionName;
import walkingkooka.tree.expression.function.ExpressionFunction;
import walkingkooka.tree.expression.function.ExpressionFunctionContext;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContexts;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContexts;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class SpreadsheetHttpServerApiSpreadsheetEngineBiConsumerTest extends SpreadsheetHttpServerTestCase2<SpreadsheetHttpServerApiSpreadsheetEngineBiConsumer> {

    private final static String BASE_URL = "http://example.com/";
    private final static SpreadsheetId ID = SpreadsheetId.with(1);

    // format...........................................................................................................

    @Test
    public void testFormat() {
        final SpreadsheetMultiFormatRequest spreadsheetMultiFormatRequest = SpreadsheetMultiFormatRequest.with(
                Lists.of(
                        SpreadsheetFormatRequest.with(
                                LocalDate.of(1999, 12, 31),
                                SpreadsheetFormatPattern.parseDateFormatPattern("yyyy/dd/mm")
                        ),
                        SpreadsheetFormatRequest.with(
                                LocalDateTime.of(1999, 12, 31, 12, 58, 59),
                                SpreadsheetLocaleDefaultDateTimeFormat.INSTANCE
                        )
                )
        );

        final HttpRequest httpRequest = this.request(
                HttpMethod.POST,
                "1/format",
                toJsonString(spreadsheetMultiFormatRequest));
        final HttpResponse httpResponse = HttpResponses.recording();

        this.handleRequest(httpRequest, httpResponse);

        final SpreadsheetMultiFormatResponse spreadsheetMultiFormatResponse = SpreadsheetMultiFormatResponse.with(
                Lists.of(
                        SpreadsheetText.with(SpreadsheetText.WITHOUT_COLOR, "1999/31/12"),
                        "31 December 1999, 12:58:59 pm"
                )
        );

        this.checkHttpResponse(httpResponse,
                HttpStatusCode.OK.setMessage("POST SpreadsheetMultiFormatResponse OK"),
                spreadsheetMultiFormatResponse
        );
    }

    // parse.............................................................................................................

    @Test
    public void testParse() {
        final String pattern = "yyyy/mm/dd";

        final SpreadsheetMultiParseRequest spreadsheetMultiParseRequest = SpreadsheetMultiParseRequest.with(
                Lists.of(
                        SpreadsheetParseRequest.with(
                                pattern,
                                "spreadsheet-date-formatter" // @see MultiParser.SPREADSHEET_DATE_FORMATTER
                        )
                )
        );

        final HttpRequest httpRequest = this.request(
                HttpMethod.POST,
                "1/parse",
                toJsonString(spreadsheetMultiParseRequest));
        final HttpResponse httpResponse = HttpResponses.recording();

        this.handleRequest(httpRequest, httpResponse);

        final SpreadsheetMultiParseResponse spreadsheetMultiParseResponse = SpreadsheetMultiParseResponse.with(
                Lists.of(
                        SpreadsheetParsePatterns.parseDateFormatPattern(pattern)
                )
        );

        this.checkHttpResponse(
                httpResponse,
                HttpStatusCode.OK.setMessage("POST SpreadsheetMultiParseResponse OK"),
                spreadsheetMultiParseResponse
        );
    }

    // engine hateos....................................................................................................

    @Test
    public void testEngineHateos() {
        final HttpRequest httpRequest = this.request(
                HttpMethod.GET,
                "1/cell/A1",
                ""
        );
        final HttpResponse httpResponse = HttpResponses.recording();

        this.handleRequest(httpRequest, httpResponse);

        this.checkHttpResponse(httpResponse,
                HttpStatusCode.OK.setMessage("GET SpreadsheetDelta OK"),
                JsonNode.object()
        );
    }

    // helpers..........................................................................................................

    private HttpRequest request(final HttpMethod method,
                                final String urlPathAppend,
                                final String bodyText) {
        final MediaType contentType = MediaType.APPLICATION_JSON;
        return HttpRequests.value(method,
                HttpTransport.UNSECURED,
                Url.parseAbsolute(BASE_URL + urlPathAppend).relativeUrl(),
                HttpProtocolVersion.VERSION_1_0,
                HttpEntity.EMPTY
                        .addHeader(HttpHeaderName.CONTENT_TYPE, contentType)
                        .addHeader(HttpHeaderName.ACCEPT, contentType.accept())
                        .setBodyText(bodyText)
                        .setContentLength()
        );
    }

    private void handleRequest(final HttpRequest request,
                               final HttpResponse response) {
        this.createBiConsumer()
                .router(SpreadsheetId.with(1L))
                .route(request.routerParameters())
                .get()
                .accept(request, response);
    }

    private void checkHttpResponse(final HttpResponse httpResponse,
                                   final HttpStatus status,
                                   final Object body) {
        assertEquals(Optional.of(status), httpResponse.status(), () -> "status\n" + httpResponse);
        assertEquals(toJsonString(body),
                httpResponse.entities().get(0).bodyText(),
                () -> httpResponse.toString());
    }

    private static String toJsonString(final Object value) {
        return JsonNodeMarshallContexts.basic()
                .marshall(value)
                .toString();
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(this.createBiConsumer(), BASE_URL);
    }

    // helpers..........................................................................................................

    private SpreadsheetHttpServerApiSpreadsheetEngineBiConsumer createBiConsumer() {
        return SpreadsheetHttpServerApiSpreadsheetEngineBiConsumer.with(
                Url.parseAbsolute(BASE_URL),
                HateosContentType.json(JsonNodeUnmarshallContexts.basic(ExpressionNumberContexts.fake()), JsonNodeMarshallContexts.basic()),
                fractioner(),
                idToFunctions(),
                idToStoreRepository()
        );
    }

    private Function<BigDecimal, Fraction> fractioner() {
        return (b) -> {
            throw new UnsupportedOperationException();
        };
    }

    private Function<SpreadsheetId, Function<FunctionExpressionName, ExpressionFunction<?, ExpressionFunctionContext>>> idToFunctions() {
        return (i) ->
            (n) -> {
                throw new UnsupportedOperationException();
            };
    }

    private Function<SpreadsheetId, SpreadsheetStoreRepository> idToStoreRepository() {
        final EmailAddress user = EmailAddress.parse("user@example.com");
        final LocalDateTime now = LocalDateTime.now();;

        final SpreadsheetMetadata metadata = SpreadsheetMetadata.NON_LOCALE_DEFAULTS
                .set(SpreadsheetMetadataPropertyName.LOCALE, Locale.forLanguageTag("EN-AU"))
                .loadFromLocale()
                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, ID)
                .set(SpreadsheetMetadataPropertyName.CREATOR, user)
                .set(SpreadsheetMetadataPropertyName.CREATE_DATE_TIME, now)
                .set(SpreadsheetMetadataPropertyName.MODIFIED_BY, user)
                .set(SpreadsheetMetadataPropertyName.MODIFIED_DATE_TIME, now)
                .set(SpreadsheetMetadataPropertyName.TEXT_FORMAT_PATTERN, SpreadsheetPattern.parseTextFormatPattern("@"));

        final SpreadsheetMetadataStore metadataStore = SpreadsheetMetadataStores.treeMap();
        metadataStore.save(metadata);

        final SpreadsheetStoreRepository repository = SpreadsheetStoreRepositories.basic(
                SpreadsheetCellStores.treeMap(),
                SpreadsheetExpressionReferenceStores.treeMap(),
                SpreadsheetGroupStores.treeMap(),
                SpreadsheetLabelStores.treeMap(),
                SpreadsheetExpressionReferenceStores.treeMap(),
                metadataStore,
                SpreadsheetRangeStores.treeMap(),
                SpreadsheetRangeStores.treeMap(),
                SpreadsheetUserStores.treeMap());
        return (i) -> {
            assertEquals(ID, i, "id");
            return repository;
        };
    }

    // ClassTesting.....................................................................................................

    @Override
    public Class<SpreadsheetHttpServerApiSpreadsheetEngineBiConsumer> type() {
        return SpreadsheetHttpServerApiSpreadsheetEngineBiConsumer.class;
    }

    // TypeNameTesting..................................................................................................

    @Override
    public final String typeNamePrefix() {
        return SpreadsheetHttpServerApiSpreadsheetEngineBiConsumer.class.getSimpleName();
    }

    @Override
    public String typeNameSuffix() {
        return BiConsumer.class.getSimpleName();
    }
}

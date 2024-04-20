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
import walkingkooka.collect.set.Sets;
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
import walkingkooka.spreadsheet.SpreadsheetFormula;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.compare.SpreadsheetComparator;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorName;
import walkingkooka.spreadsheet.compare.SpreadsheetComparators;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetPattern;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStores;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.security.store.SpreadsheetGroupStores;
import walkingkooka.spreadsheet.security.store.SpreadsheetUserStores;
import walkingkooka.spreadsheet.store.SpreadsheetCellRangeStores;
import walkingkooka.spreadsheet.store.SpreadsheetCellStores;
import walkingkooka.spreadsheet.store.SpreadsheetColumnStores;
import walkingkooka.spreadsheet.store.SpreadsheetExpressionReferenceStores;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStores;
import walkingkooka.spreadsheet.store.SpreadsheetRowStores;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepositories;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;
import walkingkooka.tree.expression.ExpressionEvaluationContext;
import walkingkooka.tree.expression.FunctionExpressionName;
import walkingkooka.tree.expression.function.ExpressionFunction;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContexts;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetHttpServerApiSpreadsheetEngineBiConsumerTest extends SpreadsheetHttpServerTestCase2<SpreadsheetHttpServerApiSpreadsheetEngineBiConsumer> {

    private final static String BASE_URL = "https://example.com";
    private final static SpreadsheetId ID = SpreadsheetId.with(1);

    // cell.............................................................................................................

    @Test
    public void testRouteCellGetLoadCell() {
        this.routeAndCheck(HttpMethod.GET, "/api/1/cell/A1", HttpStatusCode.OK);
    }

    @Test
    public void testRouteCellPostSaveCell() {
        assertThrows(
                IllegalArgumentException.class,
                () -> this.route(HttpMethod.POST, "/api/1/cell/A1", "")
        );
    }

    @Test
    public void testRouteCellPutFails() {
        this.routeAndFail(HttpMethod.PUT, "/api/1/cell/A1");
    }

    @Test
    public void testRouteCellDelete() {
        this.routeAndCheck(
                HttpMethod.DELETE,
                "/api/1/cell/A1",
                HttpStatusCode.OK
        );
    }

    @Test
    public void testRouteCellDeleteRange() {
        this.routeAndCheck(
                HttpMethod.DELETE,
                "/api/1/cell/A1:B2",
                HttpStatusCode.OK
        );
    }

    // cell/SpreadsheetEngineEvaluation..................................................................................

    @Test
    public void testRouteCellGetLoadCellSpreadsheetEngineEvaluation() {
        for (SpreadsheetEngineEvaluation evaluation : SpreadsheetEngineEvaluation.values()) {
            this.routeAndCheck(HttpMethod.GET, "/api/1/cell/A1/" + evaluation.toLinkRelation().toString(), HttpStatusCode.OK);
        }
    }

    // cellReference.....................................................................................................

    @Test
    public void testRouteCellReferenceGet() {
        this.routeAndCheck(HttpMethod.GET, "/api/1/cell-reference/B2?count=1", HttpStatusCode.OK);
    }

    @Test
    public void testRouteCellReferenceGetInvalidFails() {
        assertThrows(
                IllegalArgumentException.class,
                () -> this.route(HttpMethod.GET, "/api/1/cell-reference/!invalid", "")
        );
    }

    @Test
    public void testRouteCellReferencePutFails() {
        this.routeAndFail(HttpMethod.PUT, "/api/1/cell-reference/A");
    }

    // column...........................................................................................................

    @Test
    public void testRouteColumnsGetFails() {
        this.routeAndFail(HttpMethod.GET, "/api/1/column/A");
    }

    @Test
    public void testRouteColumnsPostFails() {
        this.routeAndFail(HttpMethod.POST, "/api/1/column/A");
    }

    @Test
    public void testRouteColumnsPutFails() {
        this.routeAndFail(HttpMethod.PUT, "/api/1/column/A");
    }

    @Test
    public void testRouteColumnsDelete() {
        this.routeAndCheck(HttpMethod.DELETE, "/api/1/column/A", HttpStatusCode.OK);
    }

    // row..............................................................................................................

    @Test
    public void testRouteRowsGetFails() {
        this.routeAndFail(HttpMethod.GET, "/api/1/row/1");
    }

    @Test
    public void testRouteRowsPostFails() {
        this.routeAndFail(HttpMethod.POST, "/api/1/row/1");
    }

    @Test
    public void testRouteRowsPutFails() {
        this.routeAndFail(HttpMethod.PUT, "/api/1/row/1");
    }

    @Test
    public void testRouteRowsDelete() {
        this.routeAndCheck(HttpMethod.DELETE, "/api/1/row/1", HttpStatusCode.OK);
    }

    // fillCells........................................................................................................

    @Test
    public void testRouteFillCellsGetFails() {
        this.routeAndFail(HttpMethod.GET, "/api/1/cell/A1:B2/fill");
    }

    @Test
    public void testRouteFillCellsPost() {
        this.routeAndCheck(
                HttpMethod.POST,
                "/api/1/cell/A1:B2/fill",
                JsonNodeMarshallContexts.basic()
                        .marshall(
                                SpreadsheetDelta.EMPTY
                                        .setCells(
                                                Sets.of(
                                                        SpreadsheetSelection.parseCell("A2")
                                                                .setFormula(
                                                                        SpreadsheetFormula.EMPTY
                                                                                .setText("1")
                                                                )
                                                )
                                        )
                        ).toString(),
                HttpStatusCode.OK
        );
    }

    @Test
    public void testRouteFillCellsPutFails() {
        this.routeAndFail(HttpMethod.PUT, "/api/1/cell/A1:B2/fill");
    }

    @Test
    public void testRouteFillCellsDeleteFails() {
        this.routeAndFail(HttpMethod.DELETE, "/api/1/cell/A1:B2/fill");
    }

    // labels...........................................................................................................

    @Test
    public void testRouteLabelGetNotFound() {
        this.routeAndCheck(HttpMethod.GET, "/api/1/label/UnknownLabel99", HttpStatusCode.NO_CONTENT);
    }

    @Test
    public void testRouteLabelGet() {
        this.routeAndCheck(HttpMethod.GET, "/api/1/label/" + LABEL, HttpStatusCode.OK);
    }

    private void routeAndCheck(final HttpMethod method,
                               final String url,
                               final HttpStatusCode statusCode) {
        this.routeAndCheck(method,
                url,
                "",
                statusCode);
    }

    private void routeAndFail(final HttpMethod method,
                              final String url) {
        this.routeAndCheck(
                method,
                url,
                "",
                HttpStatusCode.METHOD_NOT_ALLOWED
        );
    }

    private void routeAndCheck(final HttpMethod method,
                               final String url,
                               final String bodyText,
                               final HttpStatusCode statusCode) {
        final HttpResponse response = this.route(
                method,
                url,
                bodyText
        );

        this.checkEquals(
                statusCode,
                response.status().map(HttpStatus::value).orElse(null),
                () -> "status\n" + response
        );
    }

    private HttpResponse route(final HttpMethod method,
                               final String url,
                               final String bodyText) {
        final HttpRequest request = this.request(
                method,
                url,
                bodyText
        );
        final HttpResponse response = HttpResponses.recording();

        this.handleRequest(request, response);

        return response;
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
        this.checkEquals(Optional.of(status), httpResponse.status(), () -> "status\n" + httpResponse);
        this.checkEquals(toJsonString(body),
                httpResponse.entities().get(0).bodyText(),
                httpResponse::toString);
    }

    private static String toJsonString(final Object value) {
        return JsonNodeMarshallContexts.basic()
                .marshall(value)
                .toString();
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(this.createBiConsumer(), BASE_URL + "/api");
    }

    // helpers..........................................................................................................

    private SpreadsheetHttpServerApiSpreadsheetEngineBiConsumer createBiConsumer() {
        return SpreadsheetHttpServerApiSpreadsheetEngineBiConsumer.with(
                Url.parseAbsolute(BASE_URL + "/api"),
                //HateosContentType.json(JsonNodeUnmarshallContexts.basic(ExpressionNumberContexts.fake()), JsonNodeMarshallContexts.basic()),
                Indentation.SPACES2,
                LineEnding.NL,
                fractioner(),
                createMetadata(),
                this.metadataStore,
                spreadsheetIdNameToComparator(),
                spreadsheetSpreadsheetSpreadsheetIdToFunctions(),
                idToStoreRepository(),
                spreadsheetMetadataStamper(),
                LocalDateTime::now
        );
    }

    private Function<BigDecimal, Fraction> fractioner() {
        return (b) -> {
            throw new UnsupportedOperationException();
        };
    }

    private Function<Optional<Locale>, SpreadsheetMetadata> createMetadata() {
        return (l) -> {
            throw new UnsupportedOperationException();

        };
    }

    private final SpreadsheetMetadataStore metadataStore = SpreadsheetMetadataStores.treeMap();

    private Function<SpreadsheetId, Function<SpreadsheetComparatorName, SpreadsheetComparator<?>>> spreadsheetIdNameToComparator() {
        return (id) -> SpreadsheetComparators.nameToSpreadsheetComparator();
    }

    private Function<SpreadsheetId, Function<FunctionExpressionName, ExpressionFunction<?, ExpressionEvaluationContext>>> spreadsheetSpreadsheetSpreadsheetIdToFunctions() {
        return (i) ->
                (n) -> {
                    throw new UnsupportedOperationException();
                };
    }

    private Function<SpreadsheetId, SpreadsheetStoreRepository> idToStoreRepository() {
        final EmailAddress user = EmailAddress.parse("user@example.com");
        final LocalDateTime now = LocalDateTime.now();

        final SpreadsheetMetadata metadata = SpreadsheetMetadata.NON_LOCALE_DEFAULTS
                .set(SpreadsheetMetadataPropertyName.LOCALE, Locale.forLanguageTag("EN-AU"))
                .loadFromLocale()
                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, ID)
                .set(SpreadsheetMetadataPropertyName.CREATOR, user)
                .set(SpreadsheetMetadataPropertyName.CREATE_DATE_TIME, now)
                .set(SpreadsheetMetadataPropertyName.MODIFIED_BY, user)
                .set(SpreadsheetMetadataPropertyName.MODIFIED_DATE_TIME, now)
                .set(SpreadsheetMetadataPropertyName.TEXT_FORMAT_PATTERN, SpreadsheetPattern.DEFAULT_TEXT_FORMAT_PATTERN);

        this.metadataStore.save(metadata);

        final SpreadsheetLabelStore labelStore = SpreadsheetLabelStores.treeMap();
        labelStore.save(LABEL.mapping(SpreadsheetSelection.parseCell("Z99")));

        final SpreadsheetStoreRepository repository = SpreadsheetStoreRepositories.basic(
                SpreadsheetCellStores.treeMap(),
                SpreadsheetExpressionReferenceStores.treeMap(),
                SpreadsheetColumnStores.treeMap(),
                SpreadsheetGroupStores.treeMap(),
                labelStore,
                SpreadsheetExpressionReferenceStores.treeMap(),
                metadataStore,
                SpreadsheetCellRangeStores.treeMap(),
                SpreadsheetCellRangeStores.treeMap(),
                SpreadsheetRowStores.treeMap(),
                SpreadsheetUserStores.treeMap()
        );
        return (i) -> {
            this.checkEquals(ID, i, "id");
            return repository;
        };
    }

    private final static SpreadsheetLabelName LABEL = SpreadsheetSelection.labelName("Label123");

    private Function<SpreadsheetMetadata, SpreadsheetMetadata> spreadsheetMetadataStamper() {
        return m -> m.set(SpreadsheetMetadataPropertyName.MODIFIED_DATE_TIME, LocalDateTime.now());
    }

    // ClassTesting.....................................................................................................

    @Override
    public Class<SpreadsheetHttpServerApiSpreadsheetEngineBiConsumer> type() {
        return SpreadsheetHttpServerApiSpreadsheetEngineBiConsumer.class;
    }

    // TypeNameTesting..................................................................................................

    @Override
    public String typeNamePrefix() {
        return SpreadsheetHttpServerApiSpreadsheetEngineBiConsumer.class.getSimpleName();
    }

    @Override
    public String typeNameSuffix() {
        return BiConsumer.class.getSimpleName();
    }
}

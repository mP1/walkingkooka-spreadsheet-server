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
import walkingkooka.ToStringTesting;
import walkingkooka.collect.set.Sets;
import walkingkooka.environment.AuditInfo;
import walkingkooka.locale.LocaleContexts;
import walkingkooka.net.Url;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.HttpProtocolVersion;
import walkingkooka.net.http.HttpStatus;
import walkingkooka.net.http.HttpStatusCode;
import walkingkooka.net.http.HttpTransport;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpHandlerTesting;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequests;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.HttpResponses;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContexts;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.reflect.TypeNameTesting;
import walkingkooka.spreadsheet.SpreadsheetContext;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.engine.SpreadsheetMetadataMode;
import walkingkooka.spreadsheet.expression.SpreadsheetExpressionFunctions;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetPattern;
import walkingkooka.spreadsheet.formula.SpreadsheetFormula;
import walkingkooka.spreadsheet.meta.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataContexts;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStores;
import walkingkooka.spreadsheet.provider.SpreadsheetProviders;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepositories;
import walkingkooka.tree.expression.function.ExpressionFunctions;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionProviders;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetHttpServerApiSpreadsheetEngineHttpHandlerTest implements HttpHandlerTesting<SpreadsheetHttpServerApiSpreadsheetEngineHttpHandler>,
    ToStringTesting<SpreadsheetHttpServerApiSpreadsheetEngineHttpHandler>,
    TypeNameTesting<SpreadsheetHttpServerApiSpreadsheetEngineHttpHandler>,
    SpreadsheetMetadataTesting {

    private final static String SERVER_URL = "https://example.com";
    private final static SpreadsheetId ID = SpreadsheetId.with(1);

    // with.............................................................................................................

    @Test
    public void testWithNullContextFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetHttpServerApiSpreadsheetEngineHttpHandler.with(null)
        );
    }

    // cell.............................................................................................................

    @Test
    public void testRouteCellGetLoadCell() {
        this.routeAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/cell/A1",
            HttpStatusCode.OK
        );
    }

    @Test
    public void testRouteCellPostSaveCell() {
        assertThrows(
            IllegalArgumentException.class,
            () -> this.route(
                HttpMethod.POST,
                "/api/spreadsheet/1/cell/A1",
                ""
            )
        );
    }

    @Test
    public void testRouteCellPutFails() {
        this.routeAndFail(HttpMethod.PUT, "/api/spreadsheet/1/cell/A1");
    }

    @Test
    public void testRouteCellDelete() {
        this.routeAndCheck(
            HttpMethod.DELETE,
            "/api/spreadsheet/1/cell/A1",
            HttpStatusCode.OK
        );
    }

    @Test
    public void testRouteCellDeleteRange() {
        this.routeAndCheck(
            HttpMethod.DELETE,
            "/api/spreadsheet/1/cell/A1:B2",
            HttpStatusCode.OK
        );
    }

    // cell/SpreadsheetEngineEvaluation..................................................................................

    @Test
    public void testRouteCellGetLoadCellSpreadsheetEngineEvaluation() {
        for (SpreadsheetEngineEvaluation evaluation : SpreadsheetEngineEvaluation.values()) {
            this.routeAndCheck(
                HttpMethod.GET,
                "/api/spreadsheet/1/cell/A1/" + evaluation.toLinkRelation()
                    .toString(),
                HttpStatusCode.OK
            );
        }
    }

    // column...........................................................................................................

    @Test
    public void testRouteColumnsGetFails() {
        this.routeAndFail(
            HttpMethod.GET,
            "/api/spreadsheet/1/column/A"
        );
    }

    @Test
    public void testRouteColumnsPostFails() {
        this.routeAndFail(
            HttpMethod.POST,
            "/api/spreadsheet/1/column/A"
        );
    }

    @Test
    public void testRouteColumnsPutFails() {
        this.routeAndFail(
            HttpMethod.PUT,
            "/api/spreadsheet/1/column/A"
        );
    }

    @Test
    public void testRouteColumnsDelete() {
        this.routeAndCheck(
            HttpMethod.DELETE,
            "/api/spreadsheet/1/column/A",
            HttpStatusCode.OK
        );
    }

    // row..............................................................................................................

    @Test
    public void testRouteRowsGetFails() {
        this.routeAndFail(
            HttpMethod.GET,
            "/api/spreadsheet/1/row/1"
        );
    }

    @Test
    public void testRouteRowsPostFails() {
        this.routeAndFail(
            HttpMethod.POST,
            "/api/spreadsheet/1/row/1"
        );
    }

    @Test
    public void testRouteRowsPutFails() {
        this.routeAndFail(
            HttpMethod.PUT,
            "/api/spreadsheet/1/row/1"
        );
    }

    @Test
    public void testRouteRowsDelete() {
        this.routeAndCheck(
            HttpMethod.DELETE,
            "/api/spreadsheet/1/row/1",
            HttpStatusCode.OK
        );
    }

    // fillCells........................................................................................................

    @Test
    public void testRouteFillCellsGetFails() {
        this.routeAndFail(
            HttpMethod.GET,
            "/api/spreadsheet/1/cell/A1:B2/fill"
        );
    }

    @Test
    public void testRouteFillCellsPost() {
        this.routeAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/A1:B2/fill",
            JSON_NODE_MARSHALL_CONTEXT.marshall(
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
        this.routeAndFail(
            HttpMethod.PUT,
            "/api/spreadsheet/1/cell/A1:B2/fill"
        );
    }

    @Test
    public void testRouteFillCellsDeleteFails() {
        this.routeAndFail(
            HttpMethod.DELETE,
            "/api/spreadsheet/1/cell/A1:B2/fill"
        );
    }

    // labels...........................................................................................................

    @Test
    public void testRouteLabelGetNotFound() {
        this.routeAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/label/UnknownLabel99",
            HttpStatusCode.OK
        );
    }

    @Test
    public void testRouteLabelGet() {
        this.routeAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/label/" + LABEL,
            HttpStatusCode.OK
        );
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
        return HttpRequests.value(
            HttpTransport.UNSECURED,
            method,
            Url.parseAbsolute(SERVER_URL + urlPathAppend).relativeUrl(),
            HttpProtocolVersion.VERSION_1_0,
            HttpEntity.EMPTY
                .setContentType(contentType)
                .addHeader(HttpHeaderName.ACCEPT, contentType.accept())
                .setBodyText(bodyText)
                .setContentLength()
        );
    }

    private void handleRequest(final HttpRequest request,
                               final HttpResponse response) {
        final SpreadsheetHttpServerApiSpreadsheetEngineHttpHandler handler = this.createHttpHandler();
        final SpreadsheetContext spreadsheetContext = handler.context.createSpreadsheetContext(
            USER,
            Optional.of(LOCALE)
        );

        final SpreadsheetId spreadsheetId = spreadsheetContext.spreadsheetId();

        this.checkEquals(
            1L,
            spreadsheetId.id()
        );

        handler.router(spreadsheetId)
            .route(request.routerParameters())
            .get()
            .handle(
                request,
                response
            );
    }

    @Override
    public SpreadsheetHttpServerApiSpreadsheetEngineHttpHandler createHttpHandler() {
        return SpreadsheetHttpServerApiSpreadsheetEngineHttpHandler.with(
            SpreadsheetServerContexts.basic(
                (id) -> SpreadsheetStoreRepositories.treeMap(metadataStore),
                SpreadsheetProviders.basic(
                    CONVERTER_PROVIDER,
                    ExpressionFunctionProviders.basic(
                        Url.parseAbsolute("https://example.com/functions"),
                        SpreadsheetExpressionFunctions.NAME_CASE_SENSITIVITY,
                        Sets.of(
                            ExpressionFunctions.typeName()
                        )
                    ),
                    SPREADSHEET_COMPARATOR_PROVIDER,
                    SPREADSHEET_EXPORTER_PROVIDER,
                    SPREADSHEET_FORMATTER_PROVIDER,
                    FORM_HANDLER_PROVIDER,
                    SPREADSHEET_IMPORTER_PROVIDER,
                    SPREADSHEET_PARSER_PROVIDER,
                    VALIDATOR_PROVIDER
                ),
                (c) -> SpreadsheetEngineContexts.spreadsheetContext(
                    SpreadsheetMetadataMode.FORMULA,
                    c,
                    TERMINAL_CONTEXT
                ),
                SPREADSHEET_ENVIRONMENT_CONTEXT,
                LOCALE_CONTEXT,
                SpreadsheetMetadataContexts.basic(
                    (u, l) -> {
                        final LocalDateTime now = HAS_NOW.now();

                        final Locale locale = Locale.forLanguageTag("EN-AU");

                        final SpreadsheetMetadata metadata = SpreadsheetMetadata.NON_LOCALE_DEFAULTS
                            .set(SpreadsheetMetadataPropertyName.LOCALE, locale)
                            .loadFromLocale(
                                LocaleContexts.jre(locale)
                            ).set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, ID)
                            .set(
                                SpreadsheetMetadataPropertyName.AUDIT_INFO,
                                AuditInfo.create(
                                    USER,
                                    now
                                )
                            ).set(
                                SpreadsheetMetadataPropertyName.TEXT_FORMATTER,
                                SpreadsheetPattern.DEFAULT_TEXT_FORMAT_PATTERN.spreadsheetFormatterSelector()
                            );

                        return this.metadataStore.save(metadata);
                    },
                    this.metadataStore
                ), // SpreadsheetMetadataContext
                HateosResourceHandlerContexts.basic(
                    INDENTATION,
                    EOL,
                    JSON_NODE_MARSHALL_UNMARSHALL_CONTEXT
                ),
                PROVIDER_CONTEXT.cloneEnvironment(),
                TERMINAL_SERVER_CONTEXT
            )
        );
    }

    private final SpreadsheetMetadataStore metadataStore = SpreadsheetMetadataStores.treeMap();

    private final static SpreadsheetLabelName LABEL = SpreadsheetSelection.labelName("Label123");

    // toString.........................................................................................................

    @Test
    public void testToString() {
        final SpreadsheetHttpServerApiSpreadsheetEngineHttpHandler handler = this.createHttpHandler();

        this.toStringAndCheck(
            handler,
            handler.context.toString()
        );
    }

    // class............................................................................................................

    @Override
    public Class<SpreadsheetHttpServerApiSpreadsheetEngineHttpHandler> type() {
        return SpreadsheetHttpServerApiSpreadsheetEngineHttpHandler.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }

    @Override
    public String typeNamePrefix() {
        return SpreadsheetHttpServerApiSpreadsheetEngineHttpHandler.class.getSimpleName();
    }

    @Override
    public String typeNameSuffix() {
        return HttpHandler.class.getSimpleName();
    }
}

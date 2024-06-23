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

package walkingkooka.spreadsheet.server.context;

import org.junit.jupiter.api.Test;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.RelativeUrl;
import walkingkooka.net.Url;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.header.AcceptCharset;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.HttpStatus;
import walkingkooka.net.http.HttpStatusCode;
import walkingkooka.net.http.server.FakeHttpRequest;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.HttpResponses;
import walkingkooka.net.http.server.hateos.FakeHateosResourceHandler;
import walkingkooka.net.http.server.hateos.HateosContentType;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContexts;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContexts;

import java.math.MathContext;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetContextHateosResourceHandlersRouterTest extends SpreadsheetContextHateosResourceHandlerTestCase<SpreadsheetContextHateosResourceHandlersRouter> {

    private final static Indentation INDENTATION = Indentation.SPACES2;
    private final static LineEnding LINE_ENDING = LineEnding.SYSTEM;
    private final static ExpressionNumberKind EXPRESSION_NUMBER_KIND = ExpressionNumberKind.DEFAULT;

    @Test
    public void testWithNullBaseFails() {
        this.withFails(
                null,
                this.contentType(),
                INDENTATION,
                LINE_ENDING,
                this.createAndSaveMetadata(),
                this.deleteMetadata(),
                this.loadMetadata());
    }

    @Test
    public void testWithNullContentTypeFails() {
        this.withFails(
                this.base(),
                null,
                INDENTATION,
                LINE_ENDING,
                this.createAndSaveMetadata(),
                this.deleteMetadata(),
                this.loadMetadata()
        );
    }

    @Test
    public void testWithNullIndentationFails() {
        this.withFails(
                this.base(),
                this.contentType(),
                null,
                LINE_ENDING,
                this.createAndSaveMetadata(),
                this.deleteMetadata(),
                this.loadMetadata()
        );
    }

    @Test
    public void testWithNullLineEndingFails() {
        this.withFails(
                this.base(),
                this.contentType(),
                INDENTATION,
                null,
                this.createAndSaveMetadata(),
                this.deleteMetadata(),
                this.loadMetadata()
        );
    }

    @Test
    public void testWithNullCreateAndSaveMetadataHandlerFails() {
        this.withFails(
                this.base(),
                this.contentType(),
                INDENTATION,
                LINE_ENDING,
                null,
                this.deleteMetadata(),
                this.loadMetadata()
        );
    }

    @Test
    public void testWithNullDeleteMetadataHandlerFails() {
        this.withFails(
                this.base(),
                this.contentType(),
                INDENTATION,
                LINE_ENDING,
                this.createAndSaveMetadata(),
                null,
                this.loadMetadata()
        );
    }

    @Test
    public void testWithNullLoadMetadataHandlerFails() {
        this.withFails(
                this.base(),
                this.contentType(),
                INDENTATION,
                LINE_ENDING,
                this.createAndSaveMetadata(),
                this.deleteMetadata(),
                null
        );
    }

    private void withFails(final AbsoluteUrl base,
                           final HateosContentType contentType,
                           final Indentation indentation,
                           final LineEnding lineEnding,
                           final HateosResourceHandler<SpreadsheetId, SpreadsheetMetadata, SpreadsheetMetadataSet> createAndSaveMetadata,
                           final HateosResourceHandler<SpreadsheetId, SpreadsheetMetadata, SpreadsheetMetadataSet> deleteMetadata,
                           final HateosResourceHandler<SpreadsheetId, SpreadsheetMetadata, SpreadsheetMetadataSet> loadMetadata) {
        assertThrows(
                NullPointerException.class,
                () -> SpreadsheetContextHateosResourceHandlersRouter.with(
                        base,
                        contentType,
                        indentation,
                        lineEnding,
                        createAndSaveMetadata,
                        deleteMetadata,
                        loadMetadata
                )
        );
    }

    // handle...........................................................................................................

    @Test
    public void testHandleMetadataLoadGet() {
        this.routeAndCheck(HttpMethod.GET,
                URL + "/spreadsheet/12ef",
                HttpStatusCode.OK,
                "{\n" +
                        "  \"spreadsheet-id\": \"12ef\",\n" +
                        "  \"creator\": \"load@example.com\"\n" +
                        "}");
    }

    @Test
    public void testHandleMetadataLoadGetAll() {
        this.routeAndCheck(
                HttpMethod.GET,
                URL + "/spreadsheet/*?from=0&count=5",
                HttpStatusCode.OK,
                "[\n" +
                        "  {\n" +
                        "    \"spreadsheet-id\": \"1\",\n" +
                        "    \"creator\": \"loaded@example.com\"\n" +
                        "  },\n" +
                        "  {\n" +
                        "    \"spreadsheet-id\": \"2\",\n" +
                        "    \"creator\": \"loaded@example.com\"\n" +
                        "  }\n" +
                        "]"
        );
    }


    @Test
    public void testHandleMetadataCreatePost() {
        this.routeAndCheck(HttpMethod.POST,
                URL + "/spreadsheet/",
                HttpStatusCode.CREATED,
                "{\n" +
                        "  \"spreadsheet-id\": \"12ef\",\n" +
                        "  \"creator\": \"created@example.com\"\n" +
                        "}");
    }

    @Test
    public void testHandleMetadataSavePost() {
        this.routeAndCheck(HttpMethod.POST,
                URL + "/spreadsheet/12ef",
                HttpStatusCode.OK,
                "{\n" +
                        "  \"spreadsheet-id\": \"12ef\",\n" +
                        "  \"creator\": \"saved@example.com\"\n" +
                        "}");
    }

    @Test
    public void testHandleMetadataMetadataPut() {
        this.routeAndFail(HttpMethod.PUT,
                URL + "/spreadsheet/12ef",
                HttpStatusCode.METHOD_NOT_ALLOWED);
    }

    @Test
    public void testRouteGetUnknownFails() {
        this.routeAndFail(HttpMethod.GET,
                URL + "/unknown?",
                HttpStatusCode.NOT_FOUND);
    }

    @Test
    public void testHandleMetadataDelete() {
        this.routeAndCheck(
                HttpMethod.DELETE,
                URL + "/spreadsheet/12ef",
                HttpStatusCode.NO_CONTENT,
                ""
        );
    }

    private Router<HttpRequestAttribute<?>, HttpHandler> router() {
        return SpreadsheetContextHateosResourceHandlersRouter.with(
                this.base(),
                this.contentType(),
                INDENTATION,
                LINE_ENDING,
                this.createAndSaveMetadata(),
                this.deleteMetadata(),
                this.loadMetadata()
        );
    }

    private void routeAndCheck(final HttpMethod method,
                               final String url,
                               final HttpStatusCode statusCode,
                               final String responseBody) {
        final HttpRequest request = this.request(method, url);
        final Optional<HttpHandler> possible = this.route(request);
        this.checkNotEquals(Optional.empty(), possible);
        if (possible.isPresent()) {
            final HttpResponse response = HttpResponses.recording();
            possible.get()
                    .handle(
                            request,
                            response
                    );
            this.checkEquals(statusCode,
                    response.status().map(HttpStatus::value).orElse(null),
                    () -> "status " + request + " " + response + "\n" + possible);

            final List<HttpEntity> responseEntities = response.entities();
            this.checkEquals(
                    responseBody,
                    responseEntities.isEmpty() ?
                            "" :
                            responseEntities.get(0)
                                    .bodyText()
            );
        }
    }

    private void routeAndFail(final HttpMethod method,
                              final String url,
                              final HttpStatusCode statusCode) {
        final HttpRequest request = this.request(method, url);
        final Optional<HttpHandler> possible = this.route(request);
        if (possible.isPresent()) {
            final HttpResponse response = HttpResponses.recording();
            possible.get()
                    .handle(
                            request,
                            response
                    );
            this.checkEquals(statusCode,
                    response.status().map(HttpStatus::value).orElse(null),
                    () -> "status " + request + " " + response + "\n" + possible);
        }
    }

    private Optional<HttpHandler> route(final HttpRequest request) {
        return this.router().route(request.routerParameters());
    }

    private HttpRequest request(final HttpMethod method,
                                final String url) {
        return new FakeHttpRequest() {

            @Override
            public RelativeUrl url() {
                return Url.parseAbsolute(url).relativeUrl();
            }

            @Override
            public HttpMethod method() {
                return method;
            }

            @Override
            public Map<HttpHeaderName<?>, List<?>> headers() {
                final MediaType contentType = contentType().contentType();

                return Maps.of(
                        HttpHeaderName.ACCEPT, Lists.of(contentType.accept()),
                        HttpHeaderName.ACCEPT_CHARSET, Lists.of(AcceptCharset.parse("UTF-8")),
                        HttpHeaderName.CONTENT_TYPE, Lists.of(contentType().contentType())
                );
            }

            @Override
            public String bodyText() {
                return "";
            }

            @Override
            public String toString() {
                return this.method() + " " + this.url();
            }
        };
    }

    private final static String URL = "https://example.com/api";

    private AbsoluteUrl base() {
        return AbsoluteUrl.parseAbsolute(URL);
    }

    private HateosContentType contentType() {
        return HateosContentType.json(
                JsonNodeUnmarshallContexts.basic(
                        EXPRESSION_NUMBER_KIND,
                        MathContext.DECIMAL32
                ),
                JsonNodeMarshallContexts.basic());
    }

    private HateosResourceHandler<SpreadsheetId, SpreadsheetMetadata, SpreadsheetMetadataSet> createAndSaveMetadata() {
        return new FakeHateosResourceHandler<>() {
            @Override
            public Optional<SpreadsheetMetadata> handleNone(final Optional<SpreadsheetMetadata> resource,
                                                            final Map<HttpRequestAttribute<?>, Object> parameters) {
                return Optional.of(
                        SpreadsheetMetadata.EMPTY
                                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, spreadsheetId())
                                .set(SpreadsheetMetadataPropertyName.CREATOR, EmailAddress.parse("created@example.com"))
                );
            }

            @Override
            public Optional<SpreadsheetMetadata> handleOne(final SpreadsheetId id,
                                                           final Optional<SpreadsheetMetadata> resource,
                                                           final Map<HttpRequestAttribute<?>, Object> parameters) {
                return Optional.of(
                        SpreadsheetMetadata.EMPTY
                                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, spreadsheetId())
                                .set(SpreadsheetMetadataPropertyName.CREATOR, EmailAddress.parse("saved@example.com"))
                );
            }
        };
    }

    private HateosResourceHandler<SpreadsheetId, SpreadsheetMetadata, SpreadsheetMetadataSet> deleteMetadata() {
        return new FakeHateosResourceHandler<>() {

            @Override
            public Optional<SpreadsheetMetadata> handleOne(final SpreadsheetId id,
                                                           final Optional<SpreadsheetMetadata> resource,
                                                           final Map<HttpRequestAttribute<?>, Object> parameters) {
                return Optional.empty();
            }
        };
    }

    private HateosResourceHandler<SpreadsheetId, SpreadsheetMetadata, SpreadsheetMetadataSet> loadMetadata() {
        return new FakeHateosResourceHandler<>() {
            @Override
            public Optional<SpreadsheetMetadata> handleOne(final SpreadsheetId id,
                                                           final Optional<SpreadsheetMetadata> resource,
                                                           final Map<HttpRequestAttribute<?>, Object> parameters) {
                return Optional.of(
                        SpreadsheetMetadata.EMPTY
                                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, spreadsheetId())
                                .set(SpreadsheetMetadataPropertyName.CREATOR, EmailAddress.parse("load@example.com"))
                );
            }

            @Override
            public Optional<SpreadsheetMetadataSet> handleAll(final Optional<SpreadsheetMetadataSet> resource,
                                                               final Map<HttpRequestAttribute<?>, Object> parameters) {
                return Optional.of(
                        SpreadsheetMetadataSet.with(
                                Sets.of(
                                        SpreadsheetMetadata.EMPTY
                                                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(1))
                                                .set(SpreadsheetMetadataPropertyName.CREATOR, EmailAddress.parse("loaded@example.com")),
                                        SpreadsheetMetadata.EMPTY
                                                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(2))
                                                .set(SpreadsheetMetadataPropertyName.CREATOR, EmailAddress.parse("loaded@example.com"))
                                )
                        )
                );
            }
        };
    }

    private SpreadsheetId spreadsheetId() {
        return SpreadsheetId.parse("12ef");
    }

    // ToString.........................................................................................................

    @Override
    public void testCheckToStringOverridden() {
    }

    // ClassTesting......................................................................................................
    @Override
    public Class<SpreadsheetContextHateosResourceHandlersRouter> type() {
        return SpreadsheetContextHateosResourceHandlersRouter.class;
    }

    // TypeNameTesting..................................................................................................

    @Override
    public String typeNameSuffix() {
        return "Router";
    }
}
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

package walkingkooka.spreadsheet.server.meta;

import org.junit.jupiter.api.Test;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.RelativeUrl;
import walkingkooka.net.Url;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.header.AcceptCharset;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.HttpStatus;
import walkingkooka.net.http.HttpStatusCode;
import walkingkooka.net.http.server.FakeHttpRequest;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.HttpResponses;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.meta.store.FakeSpreadsheetMetadataStore;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.store.repo.FakeSpreadsheetStoreRepository;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;
import walkingkooka.tree.json.JsonNode;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetMetadataHateosResourceHandlersRouterTest extends SpreadsheetMetadataHateosResourceHandlerTestCase<SpreadsheetMetadataHateosResourceHandlersRouter>
        implements SpreadsheetMetadataTesting {

    private final static MediaType CONTENT_TYPE = MediaType.APPLICATION_JSON;
    private final static Indentation INDENTATION = Indentation.SPACES2;
    private final static LineEnding LINE_ENDING = LineEnding.SYSTEM;

    private final SpreadsheetId SPREADSHEET_ID = SpreadsheetId.with(0x12ef);

    private final TestSpreadsheetMetadataHateosResourceHandlerContext CONTEXT = new TestSpreadsheetMetadataHateosResourceHandlerContext();

    static class TestSpreadsheetMetadataHateosResourceHandlerContext extends FakeSpreadsheetMetadataHateosResourceHandlerContext {

        @Override
        public MediaType contentType() {
            return CONTENT_TYPE;
        }

        @Override
        public JsonNode marshall(final Object value) {
            return JSON_NODE_MARSHALL_CONTEXT.marshall(value);
        }
    }

    @Test
    public void testWithNullBaseFails() {
        this.withFails(
                null,
                INDENTATION,
                LINE_ENDING,
                CONTEXT
        );
    }

    @Test
    public void testWithNullIndentationFails() {
        this.withFails(
                this.base(),
                null,
                LINE_ENDING,
                CONTEXT
        );
    }

    @Test
    public void testWithNullLineEndingFails() {
        this.withFails(
                this.base(),
                INDENTATION,
                null,
                CONTEXT
        );
    }

    @Test
    public void testWithNullLoadContextFails() {
        this.withFails(
                this.base(),
                INDENTATION,
                LINE_ENDING,
                null
        );
    }

    private void withFails(final AbsoluteUrl base,
                           final Indentation indentation,
                           final LineEnding lineEnding,
                           final SpreadsheetMetadataHateosResourceHandlerContext context) {
        assertThrows(
                NullPointerException.class,
                () -> SpreadsheetMetadataHateosResourceHandlersRouter.with(
                        base,
                        indentation,
                        lineEnding,
                        context
                )
        );
    }

    // handle...........................................................................................................

    @Test
    public void testHandleOneLoadGet() {
        this.routeAndCheck(
                new TestSpreadsheetMetadataHateosResourceHandlerContext() {
                    @Override
                    public SpreadsheetStoreRepository storeRepository(final SpreadsheetId id) {
                        checkEquals(SPREADSHEET_ID, id, "id");
                        return new FakeSpreadsheetStoreRepository() {
                            @Override
                            public SpreadsheetMetadataStore metadatas() {
                                return new FakeSpreadsheetMetadataStore() {
                                    @Override
                                    public Optional<SpreadsheetMetadata> load(final SpreadsheetId id) {
                                        return Optional.of(
                                                SpreadsheetMetadata.EMPTY.set(
                                                        SpreadsheetMetadataPropertyName.SPREADSHEET_ID,
                                                        SPREADSHEET_ID
                                                ).set(
                                                        SpreadsheetMetadataPropertyName.CREATOR,
                                                        USER
                                                )
                                        );
                                    }
                                };
                            }
                        };
                    }
                },
                HttpMethod.GET,
                URL + "/spreadsheet/12ef",
                "", // request body
                HttpStatusCode.OK,
                "{\n" +
                        "  \"spreadsheet-id\": \"12ef\",\n" +
                        "  \"creator\": \"user@example.com\"\n" +
                        "}"
        );
    }

    @Test
    public void testHandleOneLoadGetAll() {
        this.routeAndCheck(
                new TestSpreadsheetMetadataHateosResourceHandlerContext() {

                    @Override
                    public SpreadsheetMetadataStore metadataStore() {
                        return new FakeSpreadsheetMetadataStore() {

                            @Override
                            public List<SpreadsheetMetadata> values(final int offset,
                                                                    final int count) {
                                checkEquals(0, offset, "offset");
                                checkEquals(5, count, "count");
                                return Lists.of(
                                        SpreadsheetMetadata.EMPTY.set(
                                                SpreadsheetMetadataPropertyName.SPREADSHEET_ID,
                                                SpreadsheetId.with(1)
                                        ).set(
                                                SpreadsheetMetadataPropertyName.CREATOR,
                                                EmailAddress.parse("load-all@example.com")
                                        ),
                                        SpreadsheetMetadata.EMPTY.set(
                                                SpreadsheetMetadataPropertyName.SPREADSHEET_ID,
                                                SpreadsheetId.with(2)
                                        ).set(
                                                SpreadsheetMetadataPropertyName.CREATOR,
                                                EmailAddress.parse("load-all@example.com")
                                        )
                                );
                            }
                        };
                    }
                },
                HttpMethod.GET,
                URL + "/spreadsheet/*?from=0&count=5",
                "", // request body
                HttpStatusCode.OK,
                "[\n" +
                        "  {\n" +
                        "    \"spreadsheet-id\": \"1\",\n" +
                        "    \"creator\": \"load-all@example.com\"\n" +
                        "  },\n" +
                        "  {\n" +
                        "    \"spreadsheet-id\": \"2\",\n" +
                        "    \"creator\": \"load-all@example.com\"\n" +
                        "  }\n" +
                        "]"
        );
    }


    @Test
    public void testHandleOneCreatePost() {
        this.routeAndCheck(
                new TestSpreadsheetMetadataHateosResourceHandlerContext() {
                    @Override
                    public SpreadsheetMetadataStore metadataStore() {
                        return this.store;
                    }

                    @Override
                    public SpreadsheetMetadata saveMetadata(final SpreadsheetMetadata metadata) {
                        return this.store.save(
                                metadata.set(
                                        SpreadsheetMetadataPropertyName.SPREADSHEET_ID,
                                        SPREADSHEET_ID
                                )
                        );
                    }

                    private final SpreadsheetMetadataStore store = new FakeSpreadsheetMetadataStore() {
                        @Override
                        public SpreadsheetMetadata create(final EmailAddress creator,
                                                          final Optional<Locale> locale) {
                            return SpreadsheetMetadata.EMPTY.set(
                                    SpreadsheetMetadataPropertyName.SPREADSHEET_ID,
                                    SPREADSHEET_ID
                            ).set(
                                    SpreadsheetMetadataPropertyName.CREATOR,
                                    creator
                            ).setOrRemove(
                                    SpreadsheetMetadataPropertyName.LOCALE,
                                    locale.orElse(null)
                            );
                        }
                    };
                },
                HttpMethod.POST,
                URL + "/spreadsheet/",
                "", // request body
                HttpStatusCode.CREATED,
                "{\n" +
                        "  \"spreadsheet-id\": \"12ef\",\n" +
                        "  \"creator\": \"user@example.com\"\n" +
                        "}");
    }

    @Test
    public void testHandleOneSavePost() {
        this.saved = null;

        final SpreadsheetMetadata unsaved = SpreadsheetMetadata.EMPTY
                .set(
                        SpreadsheetMetadataPropertyName.CREATOR,
                        EmailAddress.parse("saved@example.com")
                ).set(
                        SpreadsheetMetadataPropertyName.SPREADSHEET_ID,
                        SPREADSHEET_ID
                );

        this.routeAndCheck(
                new TestSpreadsheetMetadataHateosResourceHandlerContext() {
                    @Override
                    public <T> T unmarshall(final JsonNode json,
                                            final Class<T> type) {
                        return JSON_NODE_UNMARSHALL_CONTEXT.unmarshall(
                                json,
                                type
                        );
                    }

                    @Override
                    public SpreadsheetMetadata saveMetadata(final SpreadsheetMetadata metadata) {
                        SpreadsheetMetadataHateosResourceHandlersRouterTest.this.saved = metadata;
                        return metadata;
                    }
                },
                HttpMethod.POST,
                URL + "/spreadsheet/12ef",
                JSON_NODE_MARSHALL_CONTEXT.marshall(unsaved)
                        .toString(),
                HttpStatusCode.OK,
                "{\n" +
                        "  \"spreadsheet-id\": \"12ef\",\n" +
                        "  \"creator\": \"saved@example.com\"\n" +
                        "}"
        );

        this.checkEquals(
                unsaved,
                saved,
                "saved"
        );
    }

    private SpreadsheetMetadata saved;

    @Test
    public void testHandleOneMetadataPut() {
        this.routeAndFail(
                HttpMethod.PUT,
                URL + "/spreadsheet/12ef",
                "", // request body
                HttpStatusCode.METHOD_NOT_ALLOWED
        );
    }

    @Test
    public void testRouteGetUnknownFails() {
        this.routeAndFail(
                HttpMethod.GET,
                URL + "/unknown?",
                "", // request body
                HttpStatusCode.NOT_FOUND
        );
    }

    @Test
    public void testHandleOneDelete() {
        this.deleted = true;

        this.routeAndCheck(
                new TestSpreadsheetMetadataHateosResourceHandlerContext() {
                    @Override
                    public SpreadsheetStoreRepository storeRepository(final SpreadsheetId id) {
                        checkEquals(SPREADSHEET_ID, id, "id");
                        return new FakeSpreadsheetStoreRepository() {
                            @Override
                            public SpreadsheetMetadataStore metadatas() {
                                return new FakeSpreadsheetMetadataStore() {

                                    @Override
                                    public void delete(final SpreadsheetId id) {
                                        checkEquals(SPREADSHEET_ID, id, "id");
                                        SpreadsheetMetadataHateosResourceHandlersRouterTest.this.deleted = true;
                                    }
                                };
                            }
                        };
                    }
                },
                HttpMethod.DELETE,
                URL + "/spreadsheet/12ef",
                "", // request body
                HttpStatusCode.NO_CONTENT,
                ""
        );

        this.checkEquals(
                true,
                this.deleted,
                "deleted"
        );
    }

    private boolean deleted;

    private Router<HttpRequestAttribute<?>, HttpHandler> router(final TestSpreadsheetMetadataHateosResourceHandlerContext context) {
        return SpreadsheetMetadataHateosResourceHandlersRouter.with(
                this.base(),
                INDENTATION,
                LINE_ENDING,
                context
        );
    }

    private void routeAndCheck(final TestSpreadsheetMetadataHateosResourceHandlerContext context,
                               final HttpMethod method,
                               final String url,
                               final String requestBody,
                               final HttpStatusCode statusCode,
                               final String responseBody) {
        final HttpRequest request = this.request(
                method,
                url,
                requestBody
        );
        final Optional<HttpHandler> possible = this.route(
                context,
                request
        );
        this.checkNotEquals(
                Optional.empty(),
                possible
        );
        if (possible.isPresent()) {
            final HttpResponse response = HttpResponses.recording();
            possible.get()
                    .handle(
                            request,
                            response
                    );
            this.checkEquals(
                    statusCode,
                    response.status()
                            .map(HttpStatus::value)
                            .orElse(null),
                    () -> "status " + request + " " + response + "\n" + possible
            );

            this.checkEquals(
                    responseBody,
                    response.entity().bodyText()
            );
        }
    }

    private void routeAndFail(final HttpMethod method,
                              final String url,
                              final String requestBody,
                              final HttpStatusCode statusCode) {
        final HttpRequest request = this.request(
                method,
                url,
                requestBody
        );
        final Optional<HttpHandler> possible = this.route(
                CONTEXT,
                request
        );
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

    private Optional<HttpHandler> route(final TestSpreadsheetMetadataHateosResourceHandlerContext context,
                                        final HttpRequest request) {
        return this.router(context)
                .route(request.routerParameters());
    }

    private HttpRequest request(final HttpMethod method,
                                final String url,
                                final String body) {
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
                return Maps.of(
                        HttpHeaderName.ACCEPT, Lists.of(CONTENT_TYPE.accept()),
                        HttpHeaderName.ACCEPT_CHARSET, Lists.of(AcceptCharset.parse("UTF-8")),
                        HttpHeaderName.CONTENT_TYPE, Lists.of(CONTENT_TYPE),
                        HttpHeaderName.CONTENT_LENGTH, Lists.of(this.bodyLength())
                );
            }

            @Override
            public String bodyText() {
                return body;
            }

            @Override
            public long bodyLength() {
                return this.bodyText()
                        .length();
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

    // ToString.........................................................................................................

    @Override
    public void testCheckToStringOverridden() {
    }

    // ClassTesting......................................................................................................
    @Override
    public Class<SpreadsheetMetadataHateosResourceHandlersRouter> type() {
        return SpreadsheetMetadataHateosResourceHandlersRouter.class;
    }

    // TypeNameTesting..................................................................................................

    @Override
    public String typeNameSuffix() {
        return "Router";
    }
}

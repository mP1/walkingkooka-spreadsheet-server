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
package walkingkooka.spreadsheet.server.label;

import org.junit.jupiter.api.Test;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.RelativeUrl;
import walkingkooka.net.Url;
import walkingkooka.net.header.CharsetName;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.HttpStatus;
import walkingkooka.net.http.HttpStatusCode;
import walkingkooka.net.http.server.FakeHttpRequest;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.HttpResponses;
import walkingkooka.net.http.server.hateos.HateosResourceMapping;
import walkingkooka.reflect.ClassTesting2;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.server.engine.FakeSpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.engine.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStores;
import walkingkooka.spreadsheet.store.repo.FakeSpreadsheetStoreRepository;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;
import walkingkooka.tree.json.JsonNode;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class SpreadsheetLabelHateosResourceMappingsTest implements SpreadsheetMetadataTesting,
        ClassTesting2<SpreadsheetLabelHateosResourceMappings> {

    private final static MediaType CONTENT_TYPE = MediaType.APPLICATION_JSON;

    private final static AbsoluteUrl URL = Url.parseAbsolute("https://example.com/");

    private final static SpreadsheetLabelName LABEL = SpreadsheetSelection.labelName("label123");
    private final static SpreadsheetLabelMapping MAPPING = SpreadsheetLabelMapping.with(
            LABEL,
            SpreadsheetSelection.parseCell("B2")
    );

    // label.............................................................................................................

    @Test
    public void testRouteDifferent() {
        final SpreadsheetLabelStore store = SpreadsheetLabelStores.treeMap();
        store.save(MAPPING);

        this.routeAndCheck2(
                store,
                HttpMethod.GET,
                URL + "/different/" + LABEL,
                "",
                HttpStatusCode.NOT_FOUND,
                ""
        );
    }

    @Test
    public void testRouteGet() {
        final SpreadsheetLabelStore store = SpreadsheetLabelStores.treeMap();
        store.save(MAPPING);

        this.routeAndCheck2(
                store,
                HttpMethod.GET,
                URL + "/label/" + LABEL,
                "",
                HttpStatusCode.OK,
                JSON_NODE_MARSHALL_CONTEXT.marshall(MAPPING).toString()
        );
    }

    @Test
    public void testRouteGetUnknown() {
        final SpreadsheetLabelStore store = SpreadsheetLabelStores.readOnly(SpreadsheetLabelStores.treeMap());

        this.routeAndCheck2(
                store,
                HttpMethod.GET,
                URL + "/label/" + LABEL,
                "",
                HttpStatusCode.NO_CONTENT,
                ""
        );
    }

    @Test
    public void testRoutePostSave() {
        final SpreadsheetLabelStore store = SpreadsheetLabelStores.treeMap();

        this.routeAndCheck2(
                store,
                HttpMethod.POST,
                URL + "/label/",
                JSON_NODE_MARSHALL_CONTEXT.marshall(MAPPING).toString(),
                HttpStatusCode.CREATED,
                JSON_NODE_MARSHALL_CONTEXT.marshall(MAPPING).toString()
        );

        this.checkEquals(MAPPING, store.loadOrFail(LABEL));
    }

    @Test
    public void testRoutePostUpdate() {
        final SpreadsheetLabelStore store = SpreadsheetLabelStores.treeMap();
        store.save(LABEL.mapping(SpreadsheetSelection.parseCell("ZZ99")));

        this.routeAndCheck2(
                store,
                HttpMethod.POST,
                URL + "/label/",
                JSON_NODE_MARSHALL_CONTEXT.marshall(MAPPING).toString(),
                HttpStatusCode.CREATED,
                JSON_NODE_MARSHALL_CONTEXT.marshall(MAPPING).toString()
        );

        this.checkEquals(MAPPING, store.loadOrFail(LABEL));
    }

    @Test
    public void testRoutePutFails() {
        this.routeAndCheck2(
                SpreadsheetLabelStores.fake(),
                HttpMethod.PUT,
                URL + "/label/" + LABEL,
                "",
                HttpStatusCode.METHOD_NOT_ALLOWED,
                ""
        );
    }

    @Test
    public void testRouteDelete() {
        final SpreadsheetLabelStore store = SpreadsheetLabelStores.treeMap();
        store.save(MAPPING);

        this.routeAndCheck2(
                store,
                HttpMethod.DELETE,
                URL + "/label/" + LABEL,
                "",
                HttpStatusCode.NO_CONTENT,
                ""
        );

        this.checkEquals(Optional.empty(), store.load(LABEL));
    }

    // helpers..........................................................................................................

    private void routeAndCheck2(final SpreadsheetLabelStore store,
                                final HttpMethod method,
                                final String url,
                                final String requestBody,
                                final HttpStatusCode statusCode,
                                final String responseBody) {
        this.routeAndCheck2(
                SpreadsheetLabelHateosResourceMappings.INSTANCE,
                new FakeSpreadsheetEngineHateosResourceHandlerContext() {

                    @Override
                    public MediaType contentType() {
                        return MediaType.APPLICATION_JSON;
                    }

                    @Override
                    public JsonNode marshall(final Object value) {
                        return JSON_NODE_MARSHALL_CONTEXT.marshall(value);
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
                    public SpreadsheetStoreRepository storeRepository() {
                        return new FakeSpreadsheetStoreRepository() {
                            @Override
                            public SpreadsheetLabelStore labels() {
                                return store;
                            }
                        };
                    }
                },
                method,
                url,
                requestBody,
                statusCode,
                responseBody
        );
    }

    private void routeAndCheck2(final HateosResourceMapping<SpreadsheetLabelName, SpreadsheetLabelMapping, SpreadsheetLabelMapping, SpreadsheetLabelMapping, SpreadsheetEngineHateosResourceHandlerContext> mapping,
                                final SpreadsheetEngineHateosResourceHandlerContext context,
                                final HttpMethod method,
                                final String url,
                                final String requestBody,
                                final HttpStatusCode statusCode,
                                final String responseBody) {
        final HttpRequest request = this.request(method, url, requestBody);
        final Optional<HttpHandler> possible = HateosResourceMapping.router(
                URL,
                Sets.of(mapping),
                Indentation.SPACES2,
                LineEnding.NL,
                context
        ).route(request.routerParameters());

        this.checkNotEquals(Optional.empty(),
                possible,
                () -> method + " " + url);
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

            final List<HttpEntity> entities = response.entities();
            this.checkEquals(responseBody,
                    entities.isEmpty() ? "" : entities.get(0).bodyText(),
                    () -> "body " + request + " " + response + "\n" + possible);
        }
    }

    private HttpRequest request(final HttpMethod method,
                                final String url,
                                final String bodyText) {
        return new FakeHttpRequest() {

            @Override
            public RelativeUrl url() {
                return Url.parseAbsolute(url).relativeUrl();
            }

            @Override
            public HttpMethod method() {
                return method;
            }

            @SuppressWarnings("UnnecessaryBoxing")
            @Override
            public Map<HttpHeaderName<?>, List<?>> headers() {
                return Maps.of(
                        HttpHeaderName.ACCEPT, Lists.of(CONTENT_TYPE.accept()),
                        HttpHeaderName.CONTENT_TYPE, Lists.of(CONTENT_TYPE.setCharset(CharsetName.UTF_8)),
                        HttpHeaderName.CONTENT_LENGTH, Lists.of(Long.valueOf(this.bodyLength()))
                );
            }

            @Override
            public String bodyText() {
                return bodyText;
            }

            @Override
            public long bodyLength() {
                return bodyText.length();
            }
        };
    }

    // ClassTesting.....................................................................................................

    @Override
    public Class<SpreadsheetLabelHateosResourceMappings> type() {
        return SpreadsheetLabelHateosResourceMappings.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }
}

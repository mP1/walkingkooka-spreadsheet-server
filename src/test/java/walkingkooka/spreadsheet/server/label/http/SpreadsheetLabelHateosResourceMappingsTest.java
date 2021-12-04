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
package walkingkooka.spreadsheet.server.label.http;

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
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.HttpResponses;
import walkingkooka.net.http.server.hateos.HateosContentType;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.net.http.server.hateos.HateosResourceMapping;
import walkingkooka.reflect.ClassTesting2;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStores;
import walkingkooka.tree.expression.ExpressionNumberContexts;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContexts;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContexts;

import java.math.MathContext;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetLabelHateosResourceMappingsTest implements ClassTesting2<SpreadsheetLabelHateosResourceMappings> {

    private final static AbsoluteUrl URL = Url.parseAbsolute("http://example.com/");

    private final static ExpressionNumberKind EXPRESSION_NUMBER_KIND = ExpressionNumberKind.DEFAULT;
    private final static SpreadsheetLabelName LABEL = SpreadsheetLabelName.labelName("label123");
    private final static SpreadsheetLabelMapping MAPPING = SpreadsheetLabelMapping.with(
            LABEL,
            SpreadsheetSelection.parseCell("B2")
    );

    // with.............................................................................................................

    @Test
    public void testRouterDeleteNullFails() {
        this.withFails(
                null,
                this.load(),
                this.saveOrUpdate()
        );
    }

    @Test
    public void testRouterLoadNullFails() {
        this.withFails(
                this.delete(),
                null,
                this.saveOrUpdate()
        );
    }

    @Test
    public void testRouterSaveOrUpdateNullFails() {
        this.withFails(
                this.delete(),
                this.load(),
                null
        );
    }

    private void withFails(final HateosHandler<SpreadsheetLabelName, SpreadsheetLabelMapping, SpreadsheetLabelMapping> delete,
                           HateosHandler<SpreadsheetLabelName, SpreadsheetLabelMapping, SpreadsheetLabelMapping> load,
                           HateosHandler<SpreadsheetLabelName, SpreadsheetLabelMapping, SpreadsheetLabelMapping> saveOrUpdate) {
        assertThrows(NullPointerException.class, () -> SpreadsheetLabelHateosResourceMappings.with(
                delete,
                load,
                saveOrUpdate
        ));
    }

    // label.............................................................................................................

    @Test
    public void testRouteDifferent() {
        final SpreadsheetLabelStore store = SpreadsheetLabelStores.treeMap();
        store.save(MAPPING);

        this.routeAndCheck2(
                delete(),
                load(),
                saveOrUpdate(),
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
                delete(),
                SpreadsheetLabelHateosHandlers.load(store),
                saveOrUpdate(),
                HttpMethod.GET,
                URL + "/label/" + LABEL,
                "",
                HttpStatusCode.OK,
                toJson(MAPPING)
        );
    }

    @Test
    public void testRouteGetUnknown() {
        final SpreadsheetLabelStore store = SpreadsheetLabelStores.readOnly(SpreadsheetLabelStores.treeMap());

        this.routeAndCheck2(
                delete(),
                SpreadsheetLabelHateosHandlers.load(store),
                saveOrUpdate(),
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
                delete(),
                load(),
                SpreadsheetLabelHateosHandlers.saveOrUpdate(store),
                HttpMethod.POST,
                URL + "/label/",
                toJson(MAPPING),
                HttpStatusCode.OK,
                toJson(MAPPING)
        );

        this.checkEquals(MAPPING, store.loadOrFail(LABEL));
    }

    @Test
    public void testRoutePostUpdate() {
        final SpreadsheetLabelStore store = SpreadsheetLabelStores.treeMap();
        store.save(LABEL.mapping(SpreadsheetSelection.parseCell("ZZ99")));

        this.routeAndCheck2(
                delete(),
                load(),
                SpreadsheetLabelHateosHandlers.saveOrUpdate(store),
                HttpMethod.POST,
                URL + "/label/",
                toJson(MAPPING),
                HttpStatusCode.OK,
                toJson(MAPPING)
        );

        this.checkEquals(MAPPING, store.loadOrFail(LABEL));
    }

    @Test
    public void testRoutePutFails() {
        this.routeAndCheck2(
                delete(),
                load(),
                saveOrUpdate(),
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
                SpreadsheetLabelHateosHandlers.delete(store),
                load(),
                saveOrUpdate(),
                HttpMethod.DELETE,
                URL + "/label/" + LABEL,
                "",
                HttpStatusCode.NO_CONTENT,
                ""
        );

        this.checkEquals(Optional.empty(), store.load(LABEL));
    }

    // helpers..........................................................................................................

    private static String toJson(final SpreadsheetLabelMapping mapping) {
        return marshallContext().marshall(mapping).toString();
    }

    private void routeAndCheck2(final HateosHandler<SpreadsheetLabelName, SpreadsheetLabelMapping, SpreadsheetLabelMapping> delete,
                                final HateosHandler<SpreadsheetLabelName, SpreadsheetLabelMapping, SpreadsheetLabelMapping> load,
                                final HateosHandler<SpreadsheetLabelName, SpreadsheetLabelMapping, SpreadsheetLabelMapping> saveOrUpdate,
                                final HttpMethod method,
                                final String url,
                                final String requestBody,
                                final HttpStatusCode statusCode,
                                final String responseBody) {
        this.routeAndCheck2(
                SpreadsheetLabelHateosResourceMappings.with(delete, load, saveOrUpdate),
                method,
                url,
                requestBody,
                statusCode,
                responseBody
        );
    }

    private static JsonNodeUnmarshallContext unmarshallContext() {
        return JsonNodeUnmarshallContexts.basic(ExpressionNumberContexts.fake());
    }

    private static JsonNodeMarshallContext marshallContext() {
        return JsonNodeMarshallContexts.basic();
    }

    private void routeAndCheck2(final HateosResourceMapping<SpreadsheetLabelName, SpreadsheetLabelMapping, SpreadsheetLabelMapping, SpreadsheetLabelMapping> mapping,
                                final HttpMethod method,
                                final String url,
                                final String requestBody,
                                final HttpStatusCode statusCode,
                                final String responseBody) {
        final HttpRequest request = this.request(method, url, requestBody);
        final Optional<BiConsumer<HttpRequest, HttpResponse>> possible = HateosResourceMapping.router(URL,
                contentType(),
                Sets.of(mapping)
        ).route(request.routerParameters());
        this.checkNotEquals(Optional.empty(),
                possible,
                () -> method + " " + url);
        if (possible.isPresent()) {
            final HttpResponse response = HttpResponses.recording();
            possible.get().accept(request, response);
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

            @Override
            public Map<HttpHeaderName<?>, List<?>> headers() {
                final MediaType contentType = HateosContentType.JSON_CONTENT_TYPE;

                return Maps.of(
                        HttpHeaderName.ACCEPT, Lists.of(contentType.accept()),
                        HttpHeaderName.CONTENT_TYPE, Lists.of(contentType.setCharset(CharsetName.UTF_8)),
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

    private HateosContentType contentType() {
        return HateosContentType.json(
                JsonNodeUnmarshallContexts.basic(ExpressionNumberContexts.basic(EXPRESSION_NUMBER_KIND, MathContext.DECIMAL32)),
                JsonNodeMarshallContexts.basic()
        );
    }

    // all these handlers throw UOE

    private HateosHandler<SpreadsheetLabelName, SpreadsheetLabelMapping, SpreadsheetLabelMapping> delete() {
        return SpreadsheetLabelHateosHandlers.delete(SpreadsheetLabelStores.fake());
    }

    private HateosHandler<SpreadsheetLabelName, SpreadsheetLabelMapping, SpreadsheetLabelMapping> load() {
        return SpreadsheetLabelHateosHandlers.load(SpreadsheetLabelStores.fake());
    }

    private HateosHandler<SpreadsheetLabelName, SpreadsheetLabelMapping, SpreadsheetLabelMapping> saveOrUpdate() {
        return SpreadsheetLabelHateosHandlers.saveOrUpdate(SpreadsheetLabelStores.fake());
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

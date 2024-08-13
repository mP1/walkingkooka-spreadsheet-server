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

import walkingkooka.math.Fraction;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.UrlPathName;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.provider.SpreadsheetProvider;
import walkingkooka.spreadsheet.server.meta.SpreadsheetMetadataHateosResourceHandlerContexts;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A handler that routes all spreadsheet API calls.
 */
final class SpreadsheetHttpServerApiSpreadsheetEngineHttpHandler implements HttpHandler {

    /**
     * Creates a new {@link SpreadsheetHttpServerApiSpreadsheetEngineHttpHandler} handler.
     */
    static SpreadsheetHttpServerApiSpreadsheetEngineHttpHandler with(final AbsoluteUrl base,
                                                                     final Indentation indentation,
                                                                     final LineEnding lineEnding,
                                                                     final Function<BigDecimal, Fraction> fractioner,
                                                                     final Function<Optional<Locale>, SpreadsheetMetadata> createMetadata,
                                                                     final SpreadsheetMetadataStore metadataStore,
                                                                     final Function<SpreadsheetId, SpreadsheetProvider> spreadsheetIdToSpreadsheetProvider,
                                                                     final Function<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdToStoreRepository,
                                                                     final Function<SpreadsheetMetadata, SpreadsheetMetadata> spreadsheetMetadataStamper,
                                                                     final JsonNodeMarshallContext jsonNodeMarshallContext,
                                                                     final JsonNodeUnmarshallContext jsonNodeUnmarshallContext,
                                                                     final Supplier<LocalDateTime> now) {
        return new SpreadsheetHttpServerApiSpreadsheetEngineHttpHandler(
                base,
                indentation,
                lineEnding,
                fractioner,
                createMetadata,
                metadataStore,
                spreadsheetIdToSpreadsheetProvider,
                spreadsheetIdToStoreRepository,
                spreadsheetMetadataStamper,
                jsonNodeMarshallContext,
                jsonNodeUnmarshallContext,
                now
        );
    }

    /**
     * Private ctor
     */
    private SpreadsheetHttpServerApiSpreadsheetEngineHttpHandler(final AbsoluteUrl base,
                                                                 final Indentation indentation,
                                                                 final LineEnding lineEnding,
                                                                 final Function<BigDecimal, Fraction> fractioner,
                                                                 final Function<Optional<Locale>, SpreadsheetMetadata> createMetadata,
                                                                 final SpreadsheetMetadataStore metadataStore,
                                                                 final Function<SpreadsheetId, SpreadsheetProvider> spreadsheetIdToSpreadsheetProvider,
                                                                 final Function<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdToStoreRepository,
                                                                 final Function<SpreadsheetMetadata, SpreadsheetMetadata> spreadsheetMetadataStamper,
                                                                 final JsonNodeMarshallContext jsonNodeMarshallContext,
                                                                 final JsonNodeUnmarshallContext jsonNodeUnmarshallContext,
                                                                 final Supplier<LocalDateTime> now) {
        super();

        this.baseUrl = base;
        this.indentation = indentation;
        this.lineEnding = lineEnding;

        this.fractioner = fractioner;

        this.createMetadata = createMetadata;
        this.metadataStore = metadataStore;

        this.spreadsheetIdToSpreadsheetProvider = spreadsheetIdToSpreadsheetProvider;

        this.spreadsheetIdToStoreRepository = spreadsheetIdToStoreRepository;

        int spreadsheetIdPathComponent = 0;

        for (final UrlPathName name : base.path()) {
            spreadsheetIdPathComponent++;
        }

        this.spreadsheetIdPathComponent = spreadsheetIdPathComponent;

        this.spreadsheetMetadataStamper = spreadsheetMetadataStamper;

        this.jsonNodeMarshallContext = jsonNodeMarshallContext;
        this.jsonNodeUnmarshallContext = jsonNodeUnmarshallContext;

        this.now = now;
    }

    // Router...........................................................................................................

    @Override
    public void handle(final HttpRequest request,
                       final HttpResponse response) {
        SpreadsheetHttpServerApiSpreadsheetEngineHttpHandlerRequest.with(
                request,
                response,
                this
        ).handle();
    }

    // shared with SpreadsheetHttpServerApiSpreadsheetEngineHttpHandlerRequest
    final int spreadsheetIdPathComponent;

    /**
     * Creates a {@link Router} for engine apis with base url=<code>/api/spreadsheet/$spreadsheetId$/</code> for the given spreadsheet.
     */
    Router<HttpRequestAttribute<?>, HttpHandler> router(final SpreadsheetId id) {
        return SpreadsheetMetadataHateosResourceHandlerContexts.basic(
                this.baseUrl,
                this.indentation,
                this.lineEnding,
                this.fractioner,
                this.createMetadata,
                this.metadataStore,
                this.spreadsheetIdToSpreadsheetProvider,
                this.spreadsheetIdToStoreRepository,
                this.spreadsheetMetadataStamper,
                this.jsonNodeMarshallContext,
                this.jsonNodeUnmarshallContext,
                this.now
        ).httpRouter(id);
    }

    private final Indentation indentation;

    private final LineEnding lineEnding;

    private final Function<BigDecimal, Fraction> fractioner;

    private final Function<Optional<Locale>, SpreadsheetMetadata> createMetadata;

    private final SpreadsheetMetadataStore metadataStore;

    private final Function<SpreadsheetId, SpreadsheetProvider> spreadsheetIdToSpreadsheetProvider;

    /**
     * A {@link Function} that returns a {@link SpreadsheetStoreRepository} for a given {@link SpreadsheetId}.
     */
    private final Function<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdToStoreRepository;

    /**
     * Updates the last-modified timestamp.
     */
    private final Function<SpreadsheetMetadata, SpreadsheetMetadata> spreadsheetMetadataStamper;

    private final JsonNodeMarshallContext jsonNodeMarshallContext;

    private final JsonNodeUnmarshallContext jsonNodeUnmarshallContext;

    private final Supplier<LocalDateTime> now;

    // toString.........................................................................................................

    @Override
    public String toString() {
        return this.baseUrl.toString();
    }

    private final AbsoluteUrl baseUrl;
}

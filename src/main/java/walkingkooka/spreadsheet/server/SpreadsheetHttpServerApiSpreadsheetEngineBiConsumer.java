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
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.hateos.HateosContentType;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.server.context.SpreadsheetContexts;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;
import walkingkooka.tree.expression.ExpressionEvaluationContext;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.expression.FunctionExpressionName;
import walkingkooka.tree.expression.function.ExpressionFunction;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContexts;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContexts;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A handler that routes all spreadsheet API calls.
 */
final class SpreadsheetHttpServerApiSpreadsheetEngineBiConsumer implements BiConsumer<HttpRequest, HttpResponse> {

    /**
     * Creates a new {@link SpreadsheetHttpServerApiSpreadsheetEngineBiConsumer} handler.
     */
    static SpreadsheetHttpServerApiSpreadsheetEngineBiConsumer with(final AbsoluteUrl base,
                                                                    final Indentation indentation,
                                                                    final LineEnding lineEnding,
                                                                    final Function<BigDecimal, Fraction> fractioner,
                                                                    final Function<Optional<Locale>, SpreadsheetMetadata> createMetadata,
                                                                    final Function<SpreadsheetId, Function<FunctionExpressionName, ExpressionFunction<?, ExpressionEvaluationContext>>> idToFunctions,
                                                                    final Function<SpreadsheetId, SpreadsheetStoreRepository> idToStoreRepository,
                                                                    final Function<SpreadsheetMetadata, SpreadsheetMetadata> spreadsheetMetadataStamper,
                                                                    final Supplier<LocalDateTime> now) {
        return new SpreadsheetHttpServerApiSpreadsheetEngineBiConsumer(
                base,
                indentation,
                lineEnding,
                fractioner,
                createMetadata,
                idToFunctions,
                idToStoreRepository,
                spreadsheetMetadataStamper,
                now
        );
    }

    /**
     * Private ctor
     */
    private SpreadsheetHttpServerApiSpreadsheetEngineBiConsumer(final AbsoluteUrl base,
                                                                final Indentation indentation,
                                                                final LineEnding lineEnding,
                                                                final Function<BigDecimal, Fraction> fractioner,
                                                                final Function<Optional<Locale>, SpreadsheetMetadata> createMetadata,
                                                                final Function<SpreadsheetId, Function<FunctionExpressionName, ExpressionFunction<?, ExpressionEvaluationContext>>> idToFunctions,
                                                                final Function<SpreadsheetId, SpreadsheetStoreRepository> idToStoreRepository,
                                                                final Function<SpreadsheetMetadata, SpreadsheetMetadata> spreadsheetMetadataStamper,
                                                                final Supplier<LocalDateTime> now) {
        super();

        this.baseUrl = base;
        this.indentation = indentation;
        this.lineEnding = lineEnding;

        this.fractioner = fractioner;

        this.createMetadata = createMetadata;
        this.idToFunctions = idToFunctions;
        this.idToStoreRepository = idToStoreRepository;

        int spreadsheetIdPathComponent = 0;

        for (final UrlPathName name : base.path()) {
            spreadsheetIdPathComponent++;
        }

        this.spreadsheetIdPathComponent = spreadsheetIdPathComponent;

        this.spreadsheetMetadataStamper = spreadsheetMetadataStamper;

        this.now = now;
    }

    // Router...........................................................................................................

    @Override
    public void accept(final HttpRequest request,
                       final HttpResponse response) {
        SpreadsheetHttpServerApiSpreadsheetEngineBiConsumerRequest.with(request, response, this)
                .handle();
    }

    // shared with SpreadsheetHttpServerApiSpreadsheetEngineBiConsumerRequest
    final int spreadsheetIdPathComponent;

    /**
     * Creates a {@link Router} for engine apis with base url=<code>/api/spreadsheet/$spreadsheetId$/</code> for the given spreadsheet.
     */
    Router<HttpRequestAttribute<?>, BiConsumer<HttpRequest, HttpResponse>> router(final SpreadsheetId id) {
        return SpreadsheetContexts.basic(
                this.baseUrl,
                HateosContentType.json(
                        JsonNodeUnmarshallContexts.basic(
                                ExpressionNumberKind.DOUBLE,
                                MathContext.DECIMAL32
                        ),
                        JsonNodeMarshallContexts.basic()
                ),
                this.indentation,
                this.lineEnding,
                this.fractioner,
                this.createMetadata,
                this.idToFunctions,
                this.idToStoreRepository,
                this.spreadsheetMetadataStamper,
                SpreadsheetContexts::jsonHateosContentType,
                this.now
        ).httpRouter(id);
    }

    private final Indentation indentation;

    private final LineEnding lineEnding;

    private final Function<BigDecimal, Fraction> fractioner;

    private final Function<Optional<Locale>, SpreadsheetMetadata> createMetadata;

    private final Function<SpreadsheetId, Function<FunctionExpressionName, ExpressionFunction<?, ExpressionEvaluationContext>>> idToFunctions;

    /**
     * A {@link Function} that returns a {@link SpreadsheetStoreRepository} for a given {@link SpreadsheetId}.
     */
    private final Function<SpreadsheetId, SpreadsheetStoreRepository> idToStoreRepository;

    /**
     * Updates the last-modified timestamp.
     */
    private final Function<SpreadsheetMetadata, SpreadsheetMetadata> spreadsheetMetadataStamper;

    private final Supplier<LocalDateTime> now;

    // toString.........................................................................................................

    @Override
    public String toString() {
        return this.baseUrl.toString();
    }

    private final AbsoluteUrl baseUrl;
}

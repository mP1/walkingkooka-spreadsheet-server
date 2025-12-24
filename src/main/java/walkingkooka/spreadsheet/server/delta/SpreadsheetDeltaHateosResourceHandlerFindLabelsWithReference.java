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

package walkingkooka.spreadsheet.server.delta;

import walkingkooka.collect.Range;
import walkingkooka.net.UrlPath;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.net.SpreadsheetUrlQueryParameters;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A {@link HateosResourceHandler} that finds {@link walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping} for a given {@link walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference}.
 */
final class SpreadsheetDeltaHateosResourceHandlerFindLabelsWithReference extends SpreadsheetDeltaHateosResourceHandler<SpreadsheetCellReference> {

    /**
     * Singleton
     */
    final static SpreadsheetDeltaHateosResourceHandlerFindLabelsWithReference INSTANCE = new SpreadsheetDeltaHateosResourceHandlerFindLabelsWithReference();

    private SpreadsheetDeltaHateosResourceHandlerFindLabelsWithReference() {
        super();
    }

    @Override
    public Optional<SpreadsheetDelta> handleAll(final Optional<SpreadsheetDelta> resource,
                                                final Map<HttpRequestAttribute<?>, Object> parameters,
                                                final UrlPath path,
                                                final SpreadsheetEngineHateosResourceHandlerContext context) {
        return this.findLabelsWithReference(
            SpreadsheetSelection.ALL_CELLS,
            resource,
            parameters,
            path,
            context
        );
    }

    @Override
    public Optional<SpreadsheetDelta> handleOne(final SpreadsheetCellReference cell,
                                                final Optional<SpreadsheetDelta> resource,
                                                final Map<HttpRequestAttribute<?>, Object> parameters,
                                                final UrlPath path,
                                                final SpreadsheetEngineHateosResourceHandlerContext context) {
        return this.findLabelsWithReference(
            Objects.requireNonNull(cell, "cell"),
            resource,
            parameters,
            path,
            context
        );
    }

    @Override
    public Optional<SpreadsheetDelta> handleRange(final Range<SpreadsheetCellReference> cells,
                                                  final Optional<SpreadsheetDelta> resource,
                                                  final Map<HttpRequestAttribute<?>, Object> parameters,
                                                  final UrlPath path,
                                                  final SpreadsheetEngineHateosResourceHandlerContext context) {
        HateosResourceHandler.checkIdRange(cells);

        return this.findLabelsWithReference(
            SpreadsheetSelection.cellRange(cells),
            resource,
            parameters,
            path,
            context
        );
    }

    private Optional<SpreadsheetDelta> findLabelsWithReference(final SpreadsheetExpressionReference reference,
                                                               final Optional<SpreadsheetDelta> resource,
                                                               final Map<HttpRequestAttribute<?>, Object> parameters,
                                                               final UrlPath path,
                                                               final SpreadsheetEngineHateosResourceHandlerContext context) {
        HateosResourceHandler.checkResourceEmpty(resource);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkPathEmpty(path);
        HateosResourceHandler.checkContext(context);

        return Optional.of(
            context.spreadsheetEngine()
                .findLabelsWithReference(
                    reference,
                    SpreadsheetUrlQueryParameters.offset(parameters)
                        .orElse(0),
                    SpreadsheetUrlQueryParameters.count(parameters)
                        .orElse(DEFAULT_COUNT),
                    context
                )
        );
    }

    final static int DEFAULT_COUNT = 50;

    @Override
    String operation() {
        return "findLabelsWithReference";
    }
}

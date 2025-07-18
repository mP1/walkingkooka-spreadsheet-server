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

import walkingkooka.net.UrlPath;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleMany;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleNone;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleRange;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetUrlQueryParameters;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A {@link HateosResourceHandler} that calls {@link SpreadsheetEngine#loadLabel(SpreadsheetLabelName, SpreadsheetEngineContext)}.
 */
final class SpreadsheetDeltaHateosResourceHandlerLoadLabel extends SpreadsheetDeltaHateosResourceHandler<SpreadsheetLabelName>
    implements UnsupportedHateosResourceHandlerHandleNone<SpreadsheetLabelName, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleRange<SpreadsheetLabelName, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleMany<SpreadsheetLabelName, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetEngineHateosResourceHandlerContext> {

    /**
     * Singleton
     */
    final static SpreadsheetDeltaHateosResourceHandlerLoadLabel INSTANCE = new SpreadsheetDeltaHateosResourceHandlerLoadLabel();

    private SpreadsheetDeltaHateosResourceHandlerLoadLabel() {
        super();
    }

    // handleOne........................................................................................................

    @Override
    public Optional<SpreadsheetDelta> handleOne(final SpreadsheetLabelName label,
                                                final Optional<SpreadsheetDelta> resource,
                                                final Map<HttpRequestAttribute<?>, Object> parameters,
                                                final UrlPath path,
                                                final SpreadsheetEngineHateosResourceHandlerContext context) {
        Objects.requireNonNull(label, "label");
        HateosResourceHandler.checkResourceEmpty(resource);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkPath(path);
        HateosResourceHandler.checkContext(context);

        return Optional.of(
            context.spreadsheetEngine()
                .loadLabel(
                    label,
                    context
                )
        );
    }

    @Override
    public Optional<SpreadsheetDelta> handleAll(final Optional<SpreadsheetDelta> resource,
                                                final Map<HttpRequestAttribute<?>, Object> parameters,
                                                final UrlPath path,
                                                final SpreadsheetEngineHateosResourceHandlerContext context) {
        HateosResourceHandler.checkResourceEmpty(resource);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkPath(path);
        HateosResourceHandler.checkContext(context);

        return Optional.of(
            context.spreadsheetEngine()
                .loadLabels(
                    SpreadsheetUrlQueryParameters.offset(parameters)
                        .orElse(0),
                    SpreadsheetUrlQueryParameters.count(parameters)
                        .orElse(DEFAULT_COUNT),
                    context
                )
        );
    }

    final static int DEFAULT_COUNT = 10;

    @Override
    String operation() {
        return "loadLabel";
    }
}

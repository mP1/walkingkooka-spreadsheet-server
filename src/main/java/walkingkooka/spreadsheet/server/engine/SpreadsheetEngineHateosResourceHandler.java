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

package walkingkooka.spreadsheet.server.engine;

import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleMany;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleNone;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetViewport;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * An abstract {@link HateosResourceHandler} that includes uses a {@link SpreadsheetEngine} and {@link SpreadsheetEngineContext} to do things.
 */
abstract class SpreadsheetEngineHateosResourceHandler<I extends Comparable<I>, V, C> implements HateosResourceHandler<I, V, C>,
        UnsupportedHateosResourceHandlerHandleMany<I, V, C>,
        UnsupportedHateosResourceHandlerHandleNone<I, V, C> {

    /**
     * Checks required factory method parameters are not null.
     */
    static void check(final SpreadsheetEngine engine,
                      final SpreadsheetEngineContext context) {
        Objects.requireNonNull(engine, "engine");
        Objects.requireNonNull(context, "context");
    }

    /**
     * Package private to limit sub classing.
     */
    SpreadsheetEngineHateosResourceHandler(final SpreadsheetEngine engine,
                                           final SpreadsheetEngineContext context) {
        super();
        this.engine = engine;
        this.context = context;
    }

    final SpreadsheetEngine engine;
    final SpreadsheetEngineContext context;

    final Optional<SpreadsheetViewport> viewport(final Map<HttpRequestAttribute<?>, Object> parameters,
                                                 final Optional<SpreadsheetDelta> delta,
                                                 final boolean includeNavigation) {
        Optional<SpreadsheetViewport> viewport = SpreadsheetEngineHttps.viewport(
                parameters,
                includeNavigation
        );
        if (false == viewport.isPresent()) {
            viewport = delta.flatMap(SpreadsheetDelta::viewport);
        }

        return viewport;
    }
}

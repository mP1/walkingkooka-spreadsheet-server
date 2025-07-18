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

import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleMany;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleNone;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetViewport;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;

import java.util.Map;
import java.util.Optional;

/**
 * An abstract {@link HateosResourceHandler} that includes uses a {@link SpreadsheetEngine} and {@link SpreadsheetEngineContext} to do things.
 */
abstract class SpreadsheetDeltaHateosResourceHandler<I extends Comparable<I>> implements HateosResourceHandler<I, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleMany<I, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleNone<I, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetEngineHateosResourceHandlerContext> {

    /**
     * Package private to limit sub classing.
     */
    SpreadsheetDeltaHateosResourceHandler() {
        super();
    }

    @Override
    public final String toString() {
        return SpreadsheetEngine.class.getSimpleName() + "." + this.operation();
    }

    abstract String operation();

    /**
     * Applies the query and window if any was present on the input {@link SpreadsheetDelta}
     */
    final SpreadsheetDelta prepareResponse(final Optional<SpreadsheetDelta> in,
                                           final Map<HttpRequestAttribute<?>, Object> parameters,
                                           final SpreadsheetEngineHateosResourceHandlerContext context,
                                           final SpreadsheetDelta out) {
        return SpreadsheetDeltaHttpMappings.prepareResponse(
            in,
            parameters,
            out,
            context
        );
    }

    final Optional<SpreadsheetViewport> viewport(final Map<HttpRequestAttribute<?>, Object> parameters,
                                                 final Optional<SpreadsheetDelta> delta,
                                                 final boolean includeNavigation) {
        Optional<SpreadsheetViewport> viewport = SpreadsheetDeltaUrlQueryParameters.viewport(
            parameters,
            includeNavigation
        );
        if (false == viewport.isPresent()) {
            viewport = delta.flatMap(SpreadsheetDelta::viewport);
        }

        return viewport;
    }
}

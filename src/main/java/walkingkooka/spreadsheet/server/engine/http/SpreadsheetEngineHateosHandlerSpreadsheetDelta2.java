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

package walkingkooka.spreadsheet.server.engine.http;

import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;

import java.util.Map;
import java.util.Optional;

/**
 * An abstract {@link HateosHandler} that includes uses a {@link SpreadsheetEngine} and {@link SpreadsheetEngineContext} to do things.
 * The {@link #handleAll(Optional, Map)} throws {@link }UnsupportedOperationException}
 */
abstract class SpreadsheetEngineHateosHandlerSpreadsheetDelta2<I extends Comparable<I>>
        extends SpreadsheetEngineHateosHandlerSpreadsheetDelta<I> {

    /**
     * Package private to limit sub classing.
     */
    SpreadsheetEngineHateosHandlerSpreadsheetDelta2(final SpreadsheetEngine engine,
                                                    final SpreadsheetEngineContext context) {
        super(engine, context);
    }

    @Override
    public final Optional<SpreadsheetDelta> handleAll(final Optional<SpreadsheetDelta> resource,
                                                      final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosHandler.checkResource(resource);
        HateosHandler.checkParameters(parameters);

        throw new UnsupportedOperationException();
    }
}

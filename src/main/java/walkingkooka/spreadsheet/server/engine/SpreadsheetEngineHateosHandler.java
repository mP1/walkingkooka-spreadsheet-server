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

import walkingkooka.net.UrlParameterName;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetViewport;
import walkingkooka.text.CharSequences;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * An abstract {@link HateosHandler} that includes uses a {@link SpreadsheetEngine} and {@link SpreadsheetEngineContext} to do things.
 */
abstract class SpreadsheetEngineHateosHandler<I extends Comparable<I>, V, C> implements HateosHandler<I, V, C> {

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
    SpreadsheetEngineHateosHandler(final SpreadsheetEngine engine,
                                   final SpreadsheetEngineContext context) {
        super();
        this.engine = engine;
        this.context = context;
    }

    @Override
    public final Optional<C> handleList(final List<I> ids,
                                        final Optional<C> resource,
                                        final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosHandler.checkIdList(ids);
        HateosHandler.checkResource(resource);
        HateosHandler.checkParameters(parameters);

        throw new UnsupportedOperationException();
    }

    @Override
    public final Optional<V> handleNone(final Optional<V> resource,
                                        final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosHandler.checkResource(resource);
        HateosHandler.checkParameters(parameters);

        throw new UnsupportedOperationException();
    }

    final SpreadsheetEngine engine;
    final SpreadsheetEngineContext context;

    /**
     * Returns the count parameter as an integer.
     */
    static int count(final Map<HttpRequestAttribute<?>, Object> parameters) {
        final List<String> counts = (List<String>) parameters.get(COUNT);
        if (null == counts) {
            throw new IllegalArgumentException("Missing count parameter");
        }
        switch (counts.size()) {
            case 0:
                throw new IllegalArgumentException("Missing count parameter");
            default:
                final String countString = counts.get(0);
                try {
                    return Integer.parseInt(countString);
                } catch (final NumberFormatException cause) {
                    throw new IllegalArgumentException("Invalid count parameter got " + CharSequences.quoteAndEscape(countString));
                }
        }
    }

    // @VisibleForTesting
    final static UrlParameterName COUNT = UrlParameterName.with("count");

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

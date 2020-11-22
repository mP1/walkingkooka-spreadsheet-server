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

package walkingkooka.spreadsheet.server.engine.hateos;

import walkingkooka.collect.Range;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;

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
    public final Optional<C> handleAll(final Optional<C> resource,
                                       final Map<HttpRequestAttribute<?>, Object> parameters) {
        checkResource(resource);
        checkParameters(parameters);

        throw new UnsupportedOperationException();
    }

    @Override
    public final Optional<C> handleList(final List<I> list,
                                        final Optional<C> resource,
                                        final Map<HttpRequestAttribute<?>, Object> parameters) {
        Objects.requireNonNull(list, "list");
        checkResource(resource);
        checkParameters(parameters);

        throw new UnsupportedOperationException();
    }

    @Override
    public final Optional<V> handleNone(final Optional<V> resource,
                                        final Map<HttpRequestAttribute<?>, Object> parameters) {
        checkResource(resource);
        checkParameters(parameters);

        throw new UnsupportedOperationException();
    }

    final SpreadsheetEngine engine;
    final SpreadsheetEngineContext context;

    // Optional<RESOURCE>...............................................................................................

    /**
     * Complains if the resource is null.
     */
    static void checkRange(final Range<?> range) {
        Objects.requireNonNull(range, "range");
    }

    /**
     * Complains if the resource is null.
     */
    static void checkResource(final Optional<?> resource) {
        Objects.requireNonNull(resource, "resource");
    }

    /**
     * Complains if the resource is null or present.
     */
    static void checkResourceEmpty(final Optional<?> resource) {
        checkResource(resource);
        resource.ifPresent((r) -> {
            throw new IllegalArgumentException("Resource not allowed=" + r);
        });
    }

    /**
     * Complains if the resource is absent.
     */
    static SpreadsheetDelta checkResourceNotEmpty(final Optional<SpreadsheetDelta> resource) {
        checkResource(resource);
        return resource.orElseThrow(() -> new IllegalArgumentException("Required resource missing"));
    }

    static void checkWithoutCells(final Optional<SpreadsheetDelta> delta) {
        delta.ifPresent(SpreadsheetEngineHateosHandler::checkWithoutCells0);
    }

    private static void checkWithoutCells0(final SpreadsheetDelta delta) {
        if (!delta.cells().isEmpty()) {
            throw new IllegalArgumentException("Expected delta without cells: " + delta);
        }
    }

    // parameters.......................................................................................................

    /**
     * Checks parameters are present.
     */
    static void checkParameters(final Map<HttpRequestAttribute<?>, Object> parameters) {
        Objects.requireNonNull(parameters, "parameters");
    }
}

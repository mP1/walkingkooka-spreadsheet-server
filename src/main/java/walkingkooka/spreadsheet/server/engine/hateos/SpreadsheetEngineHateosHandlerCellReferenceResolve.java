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
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.text.CharSequences;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A {@link HateosHandler} that resolves {@link String text} holding a cell-reference or label or range to a cell-reference.
 */
final class SpreadsheetEngineHateosHandlerCellReferenceResolve extends SpreadsheetEngineHateosHandler<String, SpreadsheetCellReference, SpreadsheetCellReference> {

    static SpreadsheetEngineHateosHandlerCellReferenceResolve with(final SpreadsheetEngine engine,
                                                                   final SpreadsheetEngineContext context) {
        check(engine, context);
        return new SpreadsheetEngineHateosHandlerCellReferenceResolve(engine, context);
    }

    private SpreadsheetEngineHateosHandlerCellReferenceResolve(final SpreadsheetEngine engine,
                                                               final SpreadsheetEngineContext context) {
        super(engine, context);
    }

    @Override
    public Optional<SpreadsheetCellReference> handleOne(final String text,
                                                        final Optional<SpreadsheetCellReference> resource,
                                                        final Map<HttpRequestAttribute<?>, Object> parameters) {
        Objects.requireNonNull(text, "text");
        HateosHandler.checkResourceEmpty(resource);
        HateosHandler.checkParameters(parameters);

        final SpreadsheetExpressionReference reference;
        try {
            reference = SpreadsheetExpressionReference.parse(text);
        } catch (final RuntimeException cause) {
            throw new IllegalArgumentException("Invalid cell, label or range got " + CharSequences.quoteAndEscape(text));
        }

        return Optional.of(
                this.context.resolveCellReference(reference)
        );
    }

    @Override
    public Optional<SpreadsheetCellReference> handleRange(final Range<String> range,
                                                          final Optional<SpreadsheetCellReference> resource,
                                                          final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosHandler.checkRange(range);
        HateosHandler.checkResource(resource);
        HateosHandler.checkParameters(parameters);

        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return SpreadsheetEngineContext.class.getSimpleName() + ".resolveCellReference";
    }
}
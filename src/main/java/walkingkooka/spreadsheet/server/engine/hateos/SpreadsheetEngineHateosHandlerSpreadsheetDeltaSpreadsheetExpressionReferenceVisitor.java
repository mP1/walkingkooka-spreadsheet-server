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

import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReferenceVisitor;
import walkingkooka.spreadsheet.reference.SpreadsheetRange;
import walkingkooka.spreadsheet.reference.SpreadsheetRectangle;
import walkingkooka.spreadsheet.reference.SpreadsheetViewport;

import java.util.List;
import java.util.stream.Collectors;

final class SpreadsheetEngineHateosHandlerSpreadsheetDeltaSpreadsheetExpressionReferenceVisitor extends SpreadsheetExpressionReferenceVisitor {

    static List<SpreadsheetRectangle> transform(final List<SpreadsheetRectangle> rectangles,
                                                final SpreadsheetEngine engine,
                                                final SpreadsheetEngineContext context) {
        final SpreadsheetEngineHateosHandlerSpreadsheetDeltaSpreadsheetExpressionReferenceVisitor visitor = new SpreadsheetEngineHateosHandlerSpreadsheetDeltaSpreadsheetExpressionReferenceVisitor(
                engine,
                context
        );
        return rectangles.stream()
                .map(visitor::transform0)
                .collect(Collectors.toList());
    }

    private SpreadsheetEngineHateosHandlerSpreadsheetDeltaSpreadsheetExpressionReferenceVisitor(final SpreadsheetEngine engine,
                                                                                                final SpreadsheetEngineContext context) {
        super();
        this.engine = engine;
        this.context = context;
    }

    private SpreadsheetRange transform0(final SpreadsheetRectangle rectangle) {
        this.accept(rectangle);
        return this.range;
    }

    @Override
    protected void visit(final SpreadsheetViewport viewport) {
        this.range = this.engine.computeRange(viewport, this.context);
    }

    private final SpreadsheetEngine engine;
    private final SpreadsheetEngineContext context;

    @Override
    protected void visit(final SpreadsheetRange range) {
        this.range = range;
    }

    private SpreadsheetRange range;
}

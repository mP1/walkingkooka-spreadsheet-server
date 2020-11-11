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
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReferenceVisitor;
import walkingkooka.spreadsheet.reference.SpreadsheetPixelRectangle;
import walkingkooka.spreadsheet.reference.SpreadsheetRange;
import walkingkooka.spreadsheet.reference.SpreadsheetRectangle;

import java.util.List;
import java.util.stream.Collectors;

final class SpreadsheetEngineHateosHandlerSpreadsheetExpressionReferenceVisitor extends SpreadsheetExpressionReferenceVisitor {

    static List<SpreadsheetRectangle> transform(final List<SpreadsheetRectangle> rectangles,
                                                final SpreadsheetEngine engine) {
        final SpreadsheetEngineHateosHandlerSpreadsheetExpressionReferenceVisitor visitor = new SpreadsheetEngineHateosHandlerSpreadsheetExpressionReferenceVisitor(engine);
        return rectangles.stream()
                .map(visitor::transform0)
                .collect(Collectors.toList());
    }

    private SpreadsheetEngineHateosHandlerSpreadsheetExpressionReferenceVisitor(final SpreadsheetEngine engine) {
        super();
        this.engine = engine;
    }

    private SpreadsheetRange transform0(final SpreadsheetRectangle rectangle) {
        this.accept(rectangle);
        return this.range;
    }

    @Override
    protected void visit(final SpreadsheetPixelRectangle rectangle) {
        this.range = this.engine.computeRange(rectangle);
    }

    private final SpreadsheetEngine engine;

    @Override
    protected void visit(final SpreadsheetRange range) {
        this.range = range;
    }

    private SpreadsheetRange range;
}

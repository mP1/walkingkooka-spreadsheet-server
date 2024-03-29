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

import walkingkooka.ToStringTesting;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.http.server.hateos.HateosHandlerTesting;
import walkingkooka.predicate.PredicateTesting;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetFormula;
import walkingkooka.spreadsheet.SpreadsheetViewportWindows;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;

import java.util.List;
import java.util.Set;

public abstract class SpreadsheetEngineHateosHandlerSpreadsheetDeltaTestCase<H extends SpreadsheetEngineHateosHandlerSpreadsheetDelta<I>,
        I extends Comparable<I>>
        extends SpreadsheetEngineHateosHandlerTestCase2<H, I, SpreadsheetDelta, SpreadsheetDelta>
        implements HateosHandlerTesting<H, I, SpreadsheetDelta, SpreadsheetDelta>,
        PredicateTesting,
        ToStringTesting<H> {

    SpreadsheetEngineHateosHandlerSpreadsheetDeltaTestCase() {
        super();
    }

    final SpreadsheetCell cell() {
        return this.cell("A99", "1+2");
    }

    final SpreadsheetCell cell(final String cellReference, final String formula) {
        return SpreadsheetSelection.parseCell(cellReference)
                .setFormula(
                        SpreadsheetFormula.EMPTY
                                .setText(formula)
                );
    }

    final Set<SpreadsheetCell> cells() {
        return Sets.of(this.cell(), this.cellOutsideWindow());
    }

    final Set<SpreadsheetCell> cellsWithinWindow() {
        return Sets.of(this.cell());
    }

    final Set<SpreadsheetLabelMapping> labels() {
        return Sets.of(
                this.label().mapping(
                        this.cell().reference()
                )
        );
    }

    final SpreadsheetLabelName label() {
        return SpreadsheetSelection.labelName("Label1a");
    }

    final SpreadsheetViewportWindows window() {
        final SpreadsheetViewportWindows window = SpreadsheetViewportWindows.parse("A1:B99");

        this.checkEquals(
                true,
                window.test(this.cell().reference())
        );

        this.checkEquals(
                false,
                window.test(cellOutsideWindow().reference())
        );

        return window;
    }

    final SpreadsheetCell cellOutsideWindow() {
        return this.cell("Z99", "99");
    }

    @Override
    public List<I> list() {
        return Lists.of(this.id());
    }
}

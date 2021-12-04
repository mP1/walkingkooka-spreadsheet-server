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

package walkingkooka.spreadsheet.server.parse;

import org.junit.jupiter.api.Test;
import walkingkooka.ToStringTesting;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.util.FunctionTesting;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class ParseCellOrLabelAndResolveLabelsFunctionTest implements FunctionTesting<ParseCellOrLabelAndResolveLabelsFunction, String, SpreadsheetCellReference>,
        ToStringTesting<ParseCellOrLabelAndResolveLabelsFunction> {

    private final static SpreadsheetCellReference CELL = SpreadsheetSelection.parseCell("Z99");
    private final static SpreadsheetLabelName LABEL = SpreadsheetSelection.labelName("Label123");

    private final Function<SpreadsheetLabelName, SpreadsheetCellReference> LABEL_TO_CELL = (label) -> {
        this.checkEquals(LABEL, label, "label");
        return CELL;
    };

    @Test
    public void testWithNullLabelToCellFails() {
        assertThrows(NullPointerException.class, () -> {
            ParseCellOrLabelAndResolveLabelsFunction.with(null);
        });
    }

    @Test
    public void testCell() {
        this.applyAndCheck(CELL.toString(), CELL);
    }

    @Test
    public void testLabel() {
        this.applyAndCheck(LABEL.toString(), CELL);
    }

    @Test
    public void testToString() {
        this.toStringAndCheck(this.createFunction(), "parseCellOrLabel " + LABEL_TO_CELL);
    }

    @Override
    public ParseCellOrLabelAndResolveLabelsFunction createFunction() {
        return ParseCellOrLabelAndResolveLabelsFunction.with(LABEL_TO_CELL);
    }

    @Override
    public Class<ParseCellOrLabelAndResolveLabelsFunction> type() {
        return ParseCellOrLabelAndResolveLabelsFunction.class;
    }
}

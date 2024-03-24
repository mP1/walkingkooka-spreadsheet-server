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

import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;

import java.util.Objects;
import java.util.function.Function;

/**
 * Returns a {@link Function} which parses any given text into a cell or label and resolves labels to a {@link SpreadsheetCellReference}
 */
final class ParseCellOrLabelAndResolveLabelsFunction implements Function<String, SpreadsheetCellReference> {

    static ParseCellOrLabelAndResolveLabelsFunction with(final Function<SpreadsheetLabelName, SpreadsheetCellReference> labelToCellReference) {
        Objects.requireNonNull(labelToCellReference, "labelToCellReference");

        return new ParseCellOrLabelAndResolveLabelsFunction(labelToCellReference);
    }

    private ParseCellOrLabelAndResolveLabelsFunction(final Function<SpreadsheetLabelName, SpreadsheetCellReference> labelToCellReference) {
        super();
        this.labelToCellReference = labelToCellReference;
    }

    @Override
    public SpreadsheetCellReference apply(final String cellOrLabelText) {
        final SpreadsheetExpressionReference cellOrLabel = SpreadsheetSelection.parseCellOrLabel(cellOrLabelText);

        return cellOrLabel.isLabelName() ?
                labelToCellReference.apply((SpreadsheetLabelName) cellOrLabel) :
                (SpreadsheetCellReference) cellOrLabel;
    }

    private final Function<SpreadsheetLabelName, SpreadsheetCellReference> labelToCellReference;

    @Override
    public String toString() {
        return "parseCellOrLabel " + this.labelToCellReference.toString();
    }
}

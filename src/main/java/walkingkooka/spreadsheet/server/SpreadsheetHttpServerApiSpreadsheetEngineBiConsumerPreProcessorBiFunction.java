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

package walkingkooka.spreadsheet.server;

import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStore;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.JsonObject;

import java.util.function.BiFunction;

/**
 * Adds support resolving a cell with a label rather than {@link SpreadsheetCellReference reference}.
 */
final class SpreadsheetHttpServerApiSpreadsheetEngineBiConsumerPreProcessorBiFunction implements BiFunction<JsonNode, Class<?>, JsonNode> {

    static SpreadsheetHttpServerApiSpreadsheetEngineBiConsumerPreProcessorBiFunction with(final SpreadsheetLabelStore store) {
        return new SpreadsheetHttpServerApiSpreadsheetEngineBiConsumerPreProcessorBiFunction(store);
    }

    private SpreadsheetHttpServerApiSpreadsheetEngineBiConsumerPreProcessorBiFunction(final SpreadsheetLabelStore store) {
        super();
        this.store = store;
    }

    @Override
    public JsonNode apply(final JsonNode jsonNode,
                          final Class<?> type) {
        return SpreadsheetDelta.class == type ?
                handleSpreadsheetDelta(jsonNode.objectOrFail()) :
                jsonNode;
    }

    private JsonObject handleSpreadsheetDelta(final JsonObject object) {
        return SpreadsheetDelta.resolveCellLabels(
                object,
                this.store::cellReferenceOrFail
        );
    }

    private final SpreadsheetLabelStore store;

    @Override
    public String toString() {
        return this.store.toString();
    }
}

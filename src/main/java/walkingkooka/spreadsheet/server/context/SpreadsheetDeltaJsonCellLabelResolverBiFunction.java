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

package walkingkooka.spreadsheet.server.context;

import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStore;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.JsonObject;

import java.util.Objects;
import java.util.function.BiFunction;

/**
 * A function that resolves labels within a JSON payload if it contains a {@link SpreadsheetDelta}.
 */
final class SpreadsheetDeltaJsonCellLabelResolverBiFunction implements BiFunction<JsonNode, Class<?>, JsonNode> {

    static SpreadsheetDeltaJsonCellLabelResolverBiFunction with(final SpreadsheetLabelStore store) {
        Objects.requireNonNull(store, "store");
        return new SpreadsheetDeltaJsonCellLabelResolverBiFunction(store);
    }

    private SpreadsheetDeltaJsonCellLabelResolverBiFunction(final SpreadsheetLabelStore store) {
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
                (e) -> this.store.cellReferenceOrRangeOrFail(e)
                        .toCell()
        );
    }

    private final SpreadsheetLabelStore store;

    @Override
    public String toString() {
        return this.store.toString();
    }
}

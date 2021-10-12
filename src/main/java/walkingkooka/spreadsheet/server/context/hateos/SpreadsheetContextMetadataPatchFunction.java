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

package walkingkooka.spreadsheet.server.context.hateos;

import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.server.context.SpreadsheetContext;
import walkingkooka.store.LoadStoreException;
import walkingkooka.tree.json.JsonNode;

import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * A {@link UnaryOperator} that accepts a JSON object which represents a patch on the {@link SpreadsheetMetadata}.
 */
final class SpreadsheetContextMetadataPatchFunction implements UnaryOperator<JsonNode> {

    static SpreadsheetContextMetadataPatchFunction with(final SpreadsheetId id,
                                                        final SpreadsheetContext context) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(context, "context");

        return new SpreadsheetContextMetadataPatchFunction(id, context);
    }

    private SpreadsheetContextMetadataPatchFunction(final SpreadsheetId id,
                                                    final SpreadsheetContext context) {
        super();
        this.id = id;
        this.context = context;
    }

    @Override
    public JsonNode apply(final JsonNode json) {
        final SpreadsheetId id = this.id;

        try {
            final SpreadsheetMetadataStore store = this.context.storeRepository(id)
                    .metadatas();

            final SpreadsheetMetadata loadAndPatched = store.loadOrFail(id);
            final SpreadsheetMetadata saved = store.save(loadAndPatched
                    .patch(
                            json,
                            loadAndPatched.jsonNodeUnmarshallContext()
                    )
            );
            return saved.jsonNodeMarshallContext()
                    .marshall(saved);
        } catch (final LoadStoreException cause) {
            throw new LoadStoreException("Unable to load spreadsheet with id=" + id);
        }
    }

    private final SpreadsheetId id;

    private final SpreadsheetContext context;

    @Override
    public String toString() {
        return this.id + " " + this.context;
    }
}

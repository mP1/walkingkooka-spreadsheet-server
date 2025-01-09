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

package walkingkooka.spreadsheet.server.meta;

import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.store.MissingStoreException;
import walkingkooka.tree.json.JsonNode;

import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * A {@link UnaryOperator} that accepts a JSON object which represents a patch on the {@link SpreadsheetMetadata}.
 */
final class SpreadsheetMetadataPatchFunction implements UnaryOperator<JsonNode> {

    static SpreadsheetMetadataPatchFunction with(final SpreadsheetId id,
                                                 final SpreadsheetMetadataHateosResourceHandlerContext context) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(context, "context");

        return new SpreadsheetMetadataPatchFunction(id, context);
    }

    private SpreadsheetMetadataPatchFunction(final SpreadsheetId id,
                                             final SpreadsheetMetadataHateosResourceHandlerContext context) {
        super();
        this.id = id;
        this.context = context;
    }

    @Override
    public JsonNode apply(final JsonNode json) {
        final SpreadsheetId id = this.id;

        try {
            final SpreadsheetMetadataHateosResourceHandlerContext context = this.context;
            final SpreadsheetMetadata loadAndPatched = context.storeRepository(id)
                .metadatas()
                .loadOrFail(id);
            final SpreadsheetMetadata saved = context.saveMetadata(
                loadAndPatched
                    .patch(
                        json,
                        loadAndPatched.jsonNodeUnmarshallContext()
                    )
            );
            return saved.jsonNodeMarshallContext()
                .marshall(saved);
        } catch (final MissingStoreException cause) {
            throw new MissingStoreException("Unable to load spreadsheet with id=" + id);
        }
    }

    private final SpreadsheetId id;

    private final SpreadsheetMetadataHateosResourceHandlerContext context;

    @Override
    public String toString() {
        return this.id + " " + this.context;
    }
}

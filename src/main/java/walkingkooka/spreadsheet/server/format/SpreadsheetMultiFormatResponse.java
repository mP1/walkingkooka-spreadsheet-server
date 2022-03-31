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

package walkingkooka.spreadsheet.server.format;

import walkingkooka.collect.list.Lists;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.List;
import java.util.Objects;

public final class SpreadsheetMultiFormatResponse {

    public static SpreadsheetMultiFormatResponse with(final List<Object> responses) {
        Objects.requireNonNull(responses, "responses");

        return new SpreadsheetMultiFormatResponse(Lists.immutable(responses));
    }

    private SpreadsheetMultiFormatResponse(final List<Object> responses) {
        super();
        this.responses = responses;
    }

    List<Object> responses() {
        return this.responses;
    }

    private final List<Object> responses;

    // Object...........................................................................................................

    @Override
    public int hashCode() {
        return this.responses().hashCode();
    }

    @Override
    public boolean equals(final Object other) {
        return this == other || other instanceof SpreadsheetMultiFormatResponse && this.equals0((SpreadsheetMultiFormatResponse) other);
    }

    private boolean equals0(final SpreadsheetMultiFormatResponse other) {
        return this.responses.equals(other.responses);
    }

    @Override
    public String toString() {
        return this.responses().toString();
    }

    // Json.............................................................................................................

    private JsonNode marshall(final JsonNodeMarshallContext context) {
        return context.marshallWithTypeCollection(this.responses);
    }

    static SpreadsheetMultiFormatResponse unmarshall(final JsonNode node,
                                                     final JsonNodeUnmarshallContext context) {
        return with(context.unmarshallWithTypeList(node));
    }

    static {
        JsonNodeContext.register(
                JsonNodeContext.computeTypeName(SpreadsheetMultiFormatResponse.class),
                SpreadsheetMultiFormatResponse::unmarshall,
                SpreadsheetMultiFormatResponse::marshall,
                SpreadsheetMultiFormatResponse.class
        );
    }

    // for JsonNodeContext.register to happen
    static void init() {
    }
}

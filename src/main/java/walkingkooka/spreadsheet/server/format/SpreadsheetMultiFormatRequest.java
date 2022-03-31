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

public final class SpreadsheetMultiFormatRequest {

    public static SpreadsheetMultiFormatRequest with(final List<SpreadsheetFormatRequest> requests) {
        Objects.requireNonNull(requests, "requests");

        return new SpreadsheetMultiFormatRequest(Lists.immutable(requests));
    }

    private SpreadsheetMultiFormatRequest(final List<SpreadsheetFormatRequest> requests) {
        super();
        this.requests = requests;
    }

    List<SpreadsheetFormatRequest> requests() {
        return this.requests;
    }

    private final List<SpreadsheetFormatRequest> requests;

    // Object...........................................................................................................

    @Override
    public int hashCode() {
        return this.requests().hashCode();
    }

    @Override
    public boolean equals(final Object other) {
        return this == other || other instanceof SpreadsheetMultiFormatRequest && this.equals0((SpreadsheetMultiFormatRequest) other);
    }

    private boolean equals0(final SpreadsheetMultiFormatRequest other) {
        return this.requests.equals(other.requests);
    }

    @Override
    public String toString() {
        return this.requests().toString();
    }

    // Json.............................................................................................................

    private JsonNode marshall(final JsonNodeMarshallContext context) {
        return context.marshallWithTypeCollection(this.requests);
    }

    static SpreadsheetMultiFormatRequest unmarshall(final JsonNode node,
                                                    final JsonNodeUnmarshallContext context) {
        return with(context.unmarshallWithTypeList(node));
    }

    static {
        JsonNodeContext.register(
                JsonNodeContext.computeTypeName(SpreadsheetMultiFormatRequest.class),
                SpreadsheetMultiFormatRequest::unmarshall,
                SpreadsheetMultiFormatRequest::marshall,
                SpreadsheetMultiFormatRequest.class
        );
    }

    // for JsonNodeContext.register to happen
    static void init() {
        SpreadsheetFormatRequest.init();
        SpreadsheetLocaleDefaultDateTimeFormat.init();
    }
}

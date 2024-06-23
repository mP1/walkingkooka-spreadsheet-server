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

import walkingkooka.Cast;
import walkingkooka.collect.list.ImmutableListDefaults;
import walkingkooka.collect.list.Lists;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.AbstractList;
import java.util.List;
import java.util.Objects;

/**
 * An immutable list of {@link SpreadsheetFormatterFormatRequest}. This exists primarily to support marshalling/unmarshalling
 * to/from JSON which requires Classes and does not support generic types because of erasure.
 * <pre>
 * [
 *   {
 *     "selector": "date-format-pattern dd/mm/yyyy",
 *     "value": {
 *       "type": "local-date",
 *       "value": "1999-12-31"
 *     }
 *   },
 *   {
 *     "selector": "text-format-pattern @@",
 *     "value": "Hello"
 *   }
 * ]
 * </pre>
 */
public final class SpreadsheetFormatterFormatRequestList extends AbstractList<SpreadsheetFormatterFormatRequest<?>>
        implements ImmutableListDefaults<SpreadsheetFormatterFormatRequestList, SpreadsheetFormatterFormatRequest<?>> {

    public static SpreadsheetFormatterFormatRequestList with(final List<SpreadsheetFormatterFormatRequest<?>> requests) {
        Objects.requireNonNull(requests, "requests");
        return requests instanceof SpreadsheetFormatterFormatRequestList ?
                (SpreadsheetFormatterFormatRequestList) requests :
                new SpreadsheetFormatterFormatRequestList(
                        Lists.immutable(requests)
                );
    }

    private SpreadsheetFormatterFormatRequestList(final List<SpreadsheetFormatterFormatRequest<?>> requests) {
        this.requests = requests;
    }

    @Override
    public SpreadsheetFormatterFormatRequest<?> get(int index) {
        return this.requests.get(index);
    }

    @Override
    public int size() {
        return this.requests.size();
    }

    private final List<SpreadsheetFormatterFormatRequest<?>> requests;

    @Override
    public SpreadsheetFormatterFormatRequestList setElements(final List<SpreadsheetFormatterFormatRequest<?>> requests) {
        final SpreadsheetFormatterFormatRequestList copy = with(requests);
        return this.equals(copy) ?
                this :
                copy;
    }

    // json.............................................................................................................

    static SpreadsheetFormatterFormatRequestList unmarshall(final JsonNode node,
                                                            final JsonNodeUnmarshallContext context) {
        return with(
                Cast.to(
                        context.unmarshallList(
                                node,
                                SpreadsheetFormatterFormatRequest.class
                        )
                )
        );
    }

    private JsonNode marshall(final JsonNodeMarshallContext context) {
        return context.marshallCollection(this);
    }

    static {
        JsonNodeContext.register(
                JsonNodeContext.computeTypeName(SpreadsheetFormatterFormatRequestList.class),
                SpreadsheetFormatterFormatRequestList::unmarshall,
                SpreadsheetFormatterFormatRequestList::marshall,
                SpreadsheetFormatterFormatRequestList.class
        );
    }
}

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

import walkingkooka.collect.list.ImmutableList;
import walkingkooka.collect.list.Lists;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorInfo;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.AbstractList;
import java.util.List;
import java.util.Objects;

/**
 * An immutable {@link List} that only exists so marshalling the list from {@link SpreadsheetEngineHateosHandlerSpreadsheetComparators}
 * does not include type info for each element.
 */
public final class SpreadsheetComparatorInfoList extends AbstractList<SpreadsheetComparatorInfo>
        implements ImmutableList<SpreadsheetComparatorInfo> {

    static SpreadsheetComparatorInfoList with(final List<SpreadsheetComparatorInfo> infos) {
        return infos instanceof SpreadsheetComparatorInfoList ?
                (SpreadsheetComparatorInfoList) infos :
                new SpreadsheetComparatorInfoList(infos);
    }

    private SpreadsheetComparatorInfoList(final List<SpreadsheetComparatorInfo> infos) {
        Objects.requireNonNull(infos, "infos");
        if (infos.isEmpty()) {
            throw new IllegalArgumentException("List cannot be empty");
        }
        this.infos = Lists.immutable(infos);
    }

    @Override
    public SpreadsheetComparatorInfo get(final int index) {
        return this.infos.get(index);
    }

    @Override
    public int size() {
        return this.infos.size();
    }

    private final List<SpreadsheetComparatorInfo> infos;

    // Json.............................................................................................................

    static SpreadsheetComparatorInfoList unmarshall(final JsonNode node,
                                                    final JsonNodeUnmarshallContext context) {
        return SpreadsheetComparatorInfoList.with(
                context.unmarshallList(
                        node,
                        SpreadsheetComparatorInfo.class
                )
        );
    }

    private JsonNode marshall(final JsonNodeMarshallContext context) {
        return context.marshallCollection(this);
    }

    static {
        JsonNodeContext.register(
                JsonNodeContext.computeTypeName(SpreadsheetComparatorInfoList.class),
                SpreadsheetComparatorInfoList::unmarshall,
                SpreadsheetComparatorInfoList::marshall,
                SpreadsheetComparatorInfoList.class
        );
    }

    // ImmutableList....................................................................................................

    @Override
    public ImmutableList<SpreadsheetComparatorInfo> setElements(final List<SpreadsheetComparatorInfo> infos) {
        final ImmutableList<SpreadsheetComparatorInfo> copy = with(infos);
        return this.equals(copy) ?
                this :
                copy;
    }
}

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

package walkingkooka.spreadsheet.server.label;

import org.junit.jupiter.api.Test;
import walkingkooka.collect.Range;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStores;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class SpreadsheetLabelHateosHandlerLoadTest extends SpreadsheetLabelHateosHandlerTestCase2<SpreadsheetLabelHateosHandlerLoad> {

    @Test
    public void testLoad() {
        final SpreadsheetLabelName labelName = this.id();
        final SpreadsheetLabelMapping mapping = SpreadsheetLabelMapping.with(labelName, SpreadsheetSelection.parseCell("B2"));
        final SpreadsheetLabelStore store = SpreadsheetLabelStores.treeMap();
        store.save(mapping);

        this.handleOneAndCheck(
                SpreadsheetLabelHateosHandlerLoad.with(store),
                labelName,
                Optional.empty(),
                HateosHandler.NO_PARAMETERS,
                Optional.of(mapping)
        );
    }

    @Test
    public void testLoadUnknownSpreadsheetLabel() {
        this.handleOneAndCheck(
                this.id(),
                Optional.empty(),
                HateosHandler.NO_PARAMETERS,
                this.resource()
        );
    }

    // ClassTesting......................................................................................................

    @Override
    public Class<SpreadsheetLabelHateosHandlerLoad> type() {
        return SpreadsheetLabelHateosHandlerLoad.class;
    }

    @Override
    SpreadsheetLabelHateosHandlerLoad createHandler(final SpreadsheetLabelStore store) {
        return SpreadsheetLabelHateosHandlerLoad.with(store);
    }

    @Override
    SpreadsheetLabelStore store() {
        return SpreadsheetLabelStores.treeMap();
    }

    @Override
    public SpreadsheetLabelName id() {
        return SpreadsheetSelection.labelName("label123");
    }

    @Override
    public List<SpreadsheetLabelName> list() {
        return Lists.of(this.id());
    }

    @Override
    public Range<SpreadsheetLabelName> range() {
        return Range.all();
    }

    @Override
    public Optional<SpreadsheetLabelMapping> resource() {
        return Optional.empty();
    }

    @Override
    public Optional<SpreadsheetLabelMapping> collectionResource() {
        return Optional.empty();
    }

    @Override
    public Map<HttpRequestAttribute<?>, Object> parameters() {
        return Maps.empty();
    }
}

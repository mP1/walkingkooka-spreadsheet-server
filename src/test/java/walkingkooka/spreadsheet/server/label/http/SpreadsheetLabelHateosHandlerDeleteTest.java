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

package walkingkooka.spreadsheet.server.label.http;

import org.junit.jupiter.api.Test;
import walkingkooka.collect.Range;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStores;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class SpreadsheetLabelHateosHandlerDeleteTest extends SpreadsheetLabelHateosHandlerTestCase2<SpreadsheetLabelHateosHandlerDelete> {

    @Test
    public void testDeleteWithResourceFails() {
        final SpreadsheetLabelName labelName = this.id();

        this.handleOneFails(
                labelName,
                Optional.of(this.mapping(labelName)),
                HateosHandler.NO_PARAMETERS,
                IllegalArgumentException.class
        );
    }

    @Test
    public void testDelete() {
        final SpreadsheetLabelName labelName = this.id();
        final SpreadsheetLabelMapping mapping = this.mapping(labelName);
        final SpreadsheetLabelStore store = SpreadsheetLabelStores.treeMap();
        store.save(mapping);

        this.handleOneAndCheck(
                SpreadsheetLabelHateosHandlerDelete.with(store),
                labelName,
                Optional.empty(),
                HateosHandler.NO_PARAMETERS,
                Optional.empty()
        );

        assertEquals(Optional.empty(), store.load(labelName));
    }

    private SpreadsheetLabelMapping mapping(final SpreadsheetLabelName labelName) {
        return SpreadsheetLabelMapping.with(labelName, SpreadsheetSelection.parseCell("B2"));
    }

    @Test
    public void testDeleteUnknownSpreadsheetLabel() {
        this.handleOneAndCheck(
                this.id(),
                Optional.empty(),
                HateosHandler.NO_PARAMETERS,
                Optional.empty()
        );
    }

    // ClassTesting......................................................................................................

    @Override
    public Class<SpreadsheetLabelHateosHandlerDelete> type() {
        return SpreadsheetLabelHateosHandlerDelete.class;
    }

    @Override
    SpreadsheetLabelHateosHandlerDelete createHandler(final SpreadsheetLabelStore store) {
        return SpreadsheetLabelHateosHandlerDelete.with(store);
    }

    @Override
    SpreadsheetLabelStore store() {
        return SpreadsheetLabelStores.treeMap();
    }

    @Override
    public SpreadsheetLabelName id() {
        return SpreadsheetLabelName.labelName("label123");
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

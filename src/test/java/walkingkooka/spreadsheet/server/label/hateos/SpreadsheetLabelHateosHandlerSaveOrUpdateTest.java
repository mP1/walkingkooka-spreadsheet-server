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

package walkingkooka.spreadsheet.server.label.hateos;

import org.junit.jupiter.api.Test;
import walkingkooka.collect.Range;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStores;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class SpreadsheetLabelHateosHandlerSaveOrUpdateTest extends SpreadsheetLabelHateosHandlerTestCase2<SpreadsheetLabelHateosHandlerSaveOrUpdate> {

    @Test
    public void testSave() {
        final SpreadsheetLabelName labelName = this.id();
        final SpreadsheetLabelMapping mapping = SpreadsheetLabelMapping.with(labelName, SpreadsheetCellReference.parseCellReference("B2"));
        final SpreadsheetLabelStore store = SpreadsheetLabelStores.treeMap();

        this.handleNoneAndCheck(
                SpreadsheetLabelHateosHandlerSaveOrUpdate.with(store),
                Optional.of(mapping),
                HateosHandler.NO_PARAMETERS,
                Optional.of(mapping)
        );

        assertEquals(mapping, store.loadOrFail(labelName));
    }

    @Test
    public void testUpdate() {
        final SpreadsheetLabelName labelName = this.id();
        final SpreadsheetLabelMapping mapping = mapping(labelName);
        final SpreadsheetLabelStore store = SpreadsheetLabelStores.treeMap();
        store.save(mapping);

        this.handleOneAndCheck(
                SpreadsheetLabelHateosHandlerSaveOrUpdate.with(store),
                labelName,
                Optional.of(mapping),
                HateosHandler.NO_PARAMETERS,
                Optional.of(mapping)
        );

        assertEquals(mapping, store.loadOrFail(labelName));
    }

    @Test
    public void testUpdateIdAndMappingNameDifferentFails() {
        final SpreadsheetLabelName labelName = this.id();
        final SpreadsheetLabelMapping mapping = mapping(labelName);
        final SpreadsheetLabelStore store = SpreadsheetLabelStores.treeMap();
        store.save(mapping);

        this.handleOneFails(
                SpreadsheetLabelHateosHandlerSaveOrUpdate.with(SpreadsheetLabelStores.fake()),
                SpreadsheetLabelName.labelName("different1"),
                Optional.of(SpreadsheetLabelMapping.with(SpreadsheetLabelName.labelName("different2"), SpreadsheetCellReference.parseCellReference("B2"))),
                HateosHandler.NO_PARAMETERS,
                IllegalArgumentException.class
        );
    }

    // ClassTesting......................................................................................................

    @Override
    public Class<SpreadsheetLabelHateosHandlerSaveOrUpdate> type() {
        return SpreadsheetLabelHateosHandlerSaveOrUpdate.class;
    }

    @Override
    SpreadsheetLabelHateosHandlerSaveOrUpdate createHandler(final SpreadsheetLabelStore store) {
        return SpreadsheetLabelHateosHandlerSaveOrUpdate.with(store);
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
        return Optional.of(mapping(this.id()));
    }

    private SpreadsheetLabelMapping mapping(final SpreadsheetLabelName id) {
        return SpreadsheetLabelMapping.with(id, SpreadsheetCellReference.parse("Z99"));
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

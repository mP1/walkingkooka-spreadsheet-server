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
import walkingkooka.collect.set.Sets;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStores;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class SpreadsheetLabelHateosResourceHandlerSaveOrUpdateTest extends SpreadsheetLabelHateosResourceHandlerTestCase2<SpreadsheetLabelHateosResourceHandlerSaveOrUpdate> {

    @Test
    public void testSave() {
        final SpreadsheetLabelName labelName = this.id();
        final SpreadsheetLabelMapping mapping = SpreadsheetLabelMapping.with(labelName, SpreadsheetSelection.parseCell("B2"));
        final SpreadsheetLabelStore store = SpreadsheetLabelStores.treeMap();

        this.handleNoneAndCheck(
                SpreadsheetLabelHateosResourceHandlerSaveOrUpdate.with(store),
                Optional.of(mapping),
                HateosResourceHandler.NO_PARAMETERS,
                Optional.of(mapping)
        );

        this.checkEquals(mapping, store.loadOrFail(labelName));
    }

    @Test
    public void testUpdate() {
        final SpreadsheetLabelName labelName = this.id();
        final SpreadsheetLabelMapping mapping = mapping(labelName);
        final SpreadsheetLabelStore store = SpreadsheetLabelStores.treeMap();
        store.save(mapping);

        this.handleOneAndCheck(
                SpreadsheetLabelHateosResourceHandlerSaveOrUpdate.with(store),
                labelName,
                Optional.of(mapping),
                HateosResourceHandler.NO_PARAMETERS,
                Optional.of(mapping)
        );

        this.checkEquals(mapping, store.loadOrFail(labelName));
    }

    @Test
    public void testRename() {
        final SpreadsheetLabelName oldLabelName = SpreadsheetSelection.labelName("oldLabel1");
        final SpreadsheetLabelName newLabelName = SpreadsheetSelection.labelName("newLabel2");
        final SpreadsheetLabelMapping newMapping = mapping(newLabelName);
        final SpreadsheetLabelStore store = SpreadsheetLabelStores.treeMap();
        store.save(this.mapping(oldLabelName));

        this.handleOneAndCheck(
                SpreadsheetLabelHateosResourceHandlerSaveOrUpdate.with(store),
                oldLabelName,
                Optional.of(newMapping),
                HateosResourceHandler.NO_PARAMETERS,
                Optional.of(newMapping)
        );

        this.checkEquals(
                Lists.of(newMapping),
                store.all()
        );
    }

    @Test
    public void testRenameNewLabelReplacesAnother() {
        final SpreadsheetLabelName oldLabelName = SpreadsheetSelection.labelName("oldLabel1");
        final SpreadsheetLabelName newLabelName = SpreadsheetSelection.labelName("newLabel2");
        final SpreadsheetLabelMapping newMapping = mapping(newLabelName);
        final SpreadsheetLabelStore store = SpreadsheetLabelStores.treeMap();
        store.save(this.mapping(oldLabelName));
        store.save(newLabelName.mapping(SpreadsheetSelection.parseCell("Z99")));

        this.handleOneAndCheck(
                SpreadsheetLabelHateosResourceHandlerSaveOrUpdate.with(store),
                oldLabelName,
                Optional.of(newMapping),
                HateosResourceHandler.NO_PARAMETERS,
                Optional.of(newMapping)
        );

        this.checkEquals(
                Lists.of(newMapping),
                store.all()
        );
    }

    // ClassTesting......................................................................................................

    @Override
    public Class<SpreadsheetLabelHateosResourceHandlerSaveOrUpdate> type() {
        return SpreadsheetLabelHateosResourceHandlerSaveOrUpdate.class;
    }

    @Override
    SpreadsheetLabelHateosResourceHandlerSaveOrUpdate createHandler(final SpreadsheetLabelStore store) {
        return SpreadsheetLabelHateosResourceHandlerSaveOrUpdate.with(store);
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
    public Set<SpreadsheetLabelName> manyIds() {
        return Sets.of(this.id());
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
        return SpreadsheetLabelMapping.with(id, SpreadsheetSelection.parseCell("Z99"));
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

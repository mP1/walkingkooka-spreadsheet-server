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

package walkingkooka.spreadsheet.server.delta;

import org.junit.jupiter.api.Test;
import walkingkooka.collect.Range;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.server.engine.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.engine.SpreadsheetEngineHateosResourceHandlerContexts;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStores;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class SpreadsheetDeltaHateosResourceHandlerDeleteLabelTest extends SpreadsheetDeltaHateosResourceHandlerLabelTestCase<SpreadsheetDeltaHateosResourceHandlerDeleteLabel> {

    @Test
    public void testHandleOneDeleteWithResourceFails() {
        final SpreadsheetLabelName labelName = this.id();

        this.handleOneFails(
                labelName,
                Optional.of(
                        SpreadsheetDelta.EMPTY.setLabels(
                                Sets.of(
                                        this.mapping(labelName)
                                )
                        )
                ),
                HateosResourceHandler.NO_PARAMETERS,
                this.context(),
                IllegalArgumentException.class
        );
    }

    @Test
    public void testHandleOneDelete() {
        final SpreadsheetLabelName labelName = this.id();
        final SpreadsheetLabelMapping mapping = this.mapping(labelName);
        final SpreadsheetLabelStore store = SpreadsheetLabelStores.treeMap();
        store.save(mapping);

        this.handleOneAndCheck(
                labelName,
                Optional.empty(),
                HateosResourceHandler.NO_PARAMETERS,
                this.context(store),
                Optional.of(
                        SpreadsheetDelta.EMPTY.setDeletedLabels(
                                Sets.of(labelName)
                        )
                )
        );

        this.checkEquals(
                Optional.empty(),
                store.load(labelName)
        );
    }

    private SpreadsheetLabelMapping mapping(final SpreadsheetLabelName labelName) {
        return SpreadsheetLabelMapping.with(labelName, SpreadsheetSelection.parseCell("B2"));
    }

    @Test
    public void testHandleOneDeleteUnknownSpreadsheetLabel() {
        this.handleOneAndCheck(
                SpreadsheetSelection.labelName("UnknownLabel123"),
                Optional.empty(),
                HateosResourceHandler.NO_PARAMETERS,
                this.context(SpreadsheetLabelStores.treeMap()),
                Optional.of(
                        SpreadsheetDelta.EMPTY
                )
        );
    }

    @Override
    SpreadsheetDeltaHateosResourceHandlerDeleteLabel createHandler(final SpreadsheetEngine engine) {
        return SpreadsheetDeltaHateosResourceHandlerDeleteLabel.with(engine);
    }

    @Override
    SpreadsheetEngine engine() {
        return new FakeSpreadsheetEngine() {
            @Override
            public SpreadsheetDelta deleteLabel(final SpreadsheetLabelName label,
                                                final SpreadsheetEngineContext context) {
                SpreadsheetDelta response = SpreadsheetDelta.EMPTY;

                final SpreadsheetLabelStore store = context.storeRepository()
                        .labels();
                if (store.load(label).isPresent()) {
                    store.delete(label);

                    response = response.setDeletedLabels(
                            Sets.of(label)
                    );
                }

                return response;
            }
        };
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
    public Optional<SpreadsheetDelta> resource() {
        return Optional.empty();
    }

    @Override
    public Optional<SpreadsheetDelta> collectionResource() {
        return Optional.empty();
    }

    @Override
    public Map<HttpRequestAttribute<?>, Object> parameters() {
        return Maps.empty();
    }

    @Override
    public SpreadsheetEngineHateosResourceHandlerContext context() {
        return SpreadsheetEngineHateosResourceHandlerContexts.fake();
    }

    // Class.............................................................................................................

    @Override
    public Class<SpreadsheetDeltaHateosResourceHandlerDeleteLabel> type() {
        return SpreadsheetDeltaHateosResourceHandlerDeleteLabel.class;
    }
}

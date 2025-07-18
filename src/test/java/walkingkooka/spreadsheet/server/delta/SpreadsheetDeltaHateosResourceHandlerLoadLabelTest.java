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
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.UrlParameterName;
import walkingkooka.net.UrlPath;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStores;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

public final class SpreadsheetDeltaHateosResourceHandlerLoadLabelTest extends SpreadsheetDeltaHateosResourceHandlerLabelTestCase<SpreadsheetDeltaHateosResourceHandlerLoadLabel> {

    private final static int DEFAULT_COUNT = 3;

    @Test
    public void testHandleOneLoad() {
        final SpreadsheetLabelName labelName = this.id();
        final SpreadsheetLabelMapping mapping = SpreadsheetLabelMapping.with(labelName, SpreadsheetSelection.parseCell("B2"));
        final SpreadsheetLabelStore store = SpreadsheetLabelStores.treeMap();
        store.save(mapping);

        this.handleOneAndCheck(
            labelName,
            Optional.empty(),
            HateosResourceHandler.NO_PARAMETERS,
            UrlPath.EMPTY,
            this.context(store),
            Optional.of(
                SpreadsheetDelta.EMPTY.setLabels(
                    Sets.of(mapping)
                )
            )
        );
    }

    @Test
    public void testHandleOneLoadUnknownSpreadsheetLabel() {
        this.handleOneAndCheck(
            this.id(),
            Optional.empty(),
            HateosResourceHandler.NO_PARAMETERS,
            UrlPath.EMPTY,
            this.context(SpreadsheetLabelStores.treeMap()),
            Optional.of(
                SpreadsheetDelta.EMPTY
            )
        );
    }

    @Test
    public void testHandleAll() {
        final SpreadsheetLabelStore store = SpreadsheetLabelStores.treeMap();

        final SpreadsheetLabelMapping mapping1 = store.save(
            SpreadsheetSelection.labelName("Label1")
                .setLabelMappingReference(SpreadsheetSelection.A1)
        );
        final SpreadsheetLabelMapping mapping2 = store.save(
            SpreadsheetSelection.labelName("Label2")
                .setLabelMappingReference(SpreadsheetSelection.parseCell("B2"))
        );
        final SpreadsheetLabelMapping mapping3 = store.save(
            SpreadsheetSelection.labelName("Label3")
                .setLabelMappingReference(SpreadsheetSelection.parseCell("C3"))
        );
        final SpreadsheetLabelMapping mapping4 = store.save(
            SpreadsheetSelection.labelName("Label4")
                .setLabelMappingReference(SpreadsheetSelection.parseCell("D4"))
        );

        this.handleAllAndCheck(
            Optional.empty(),
            HateosResourceHandler.NO_PARAMETERS,
            UrlPath.EMPTY,
            this.context(store),
            Optional.of(
                SpreadsheetDelta.EMPTY.setLabels(
                    Sets.of(
                        mapping1,
                        mapping2,
                        mapping3,
                        mapping4
                    )
                )
            )
        );
    }

    @Test
    public void testHandleAllWithOffsetAndCount() {
        final SpreadsheetLabelStore store = SpreadsheetLabelStores.treeMap();
        store.save(
            SpreadsheetSelection.labelName("Label1")
                .setLabelMappingReference(SpreadsheetSelection.A1)
        );
        final SpreadsheetLabelMapping mapping2 = store.save(
            SpreadsheetSelection.labelName("Label2")
                .setLabelMappingReference(SpreadsheetSelection.parseCell("B2"))
        );
        final SpreadsheetLabelMapping mapping3 = store.save(
            SpreadsheetSelection.labelName("Label3")
                .setLabelMappingReference(SpreadsheetSelection.parseCell("C3"))
        );
        store.save(
            SpreadsheetSelection.labelName("Label4")
                .setLabelMappingReference(SpreadsheetSelection.parseCell("D4"))
        );

        this.handleAllAndCheck(
            Optional.empty(),
            Maps.of(
                UrlParameterName.with("offset"), Lists.of("1"),
                UrlParameterName.with("count"), Lists.of("2")
            ),
            UrlPath.EMPTY,
            this.context(store),
            Optional.of(
                SpreadsheetDelta.EMPTY.setLabels(
                    Sets.of(
                        mapping2,
                        mapping3
                    )
                )
            )
        );
    }

    @Override
    public SpreadsheetDeltaHateosResourceHandlerLoadLabel createHandler() {
        return SpreadsheetDeltaHateosResourceHandlerLoadLabel.INSTANCE;
    }

    private SpreadsheetEngineHateosResourceHandlerContext context(final SpreadsheetLabelStore store) {
        return this.context(
            new FakeSpreadsheetEngine() {

                @Override
                public SpreadsheetDelta loadLabel(final SpreadsheetLabelName name,
                                                  final SpreadsheetEngineContext context) {
                    return SpreadsheetDelta.EMPTY.setLabels(
                        context.storeRepository()
                            .labels()
                            .load(name)
                            .map(Sets::of)
                            .orElse(Sets.empty())
                    );
                }

                @Override
                public SpreadsheetDelta loadLabels(final int offset,
                                                   final int count,
                                                   final SpreadsheetEngineContext context) {
                    return SpreadsheetDelta.EMPTY.setLabels(
                        new TreeSet<>(
                            context.storeRepository()
                                .labels()
                                .values(
                                    offset,
                                    count
                                )
                        )
                    );
                }
            },
            store
        );
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
    public UrlPath path() {
        return UrlPath.EMPTY;
    }

    // ClassTesting......................................................................................................

    @Override
    public Class<SpreadsheetDeltaHateosResourceHandlerLoadLabel> type() {
        return SpreadsheetDeltaHateosResourceHandlerLoadLabel.class;
    }
}

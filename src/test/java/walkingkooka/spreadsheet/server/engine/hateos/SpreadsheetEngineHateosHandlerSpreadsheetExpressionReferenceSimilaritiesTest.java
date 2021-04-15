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

package walkingkooka.spreadsheet.server.engine.hateos;

import org.junit.jupiter.api.Test;
import walkingkooka.ToStringTesting;
import walkingkooka.collect.Range;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStores;
import walkingkooka.spreadsheet.store.repo.FakeSpreadsheetStoreRepository;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SpreadsheetEngineHateosHandlerSpreadsheetExpressionReferenceSimilaritiesTest extends SpreadsheetEngineHateosHandlerTestCase2<SpreadsheetEngineHateosHandlerSpreadsheetExpressionReferenceSimilarities,
        String,
        SpreadsheetExpressionReferenceSimilarities,
        SpreadsheetExpressionReferenceSimilarities> implements ToStringTesting<SpreadsheetEngineHateosHandlerSpreadsheetExpressionReferenceSimilarities> {

    private final static SpreadsheetCellReference A1 = SpreadsheetExpressionReference.parseCellReference("A1");
    private final static SpreadsheetCellReference B2 = SpreadsheetExpressionReference.parseCellReference("B2");
    private final static SpreadsheetCellReference C3 = SpreadsheetExpressionReference.parseCellReference("C3");

    private final static SpreadsheetLabelName LABEL1 = SpreadsheetExpressionReference.labelName("Label123");
    private final static SpreadsheetLabelName LABEL2 = SpreadsheetExpressionReference.labelName("Label456");
    private final static SpreadsheetLabelName LABEL3 = SpreadsheetExpressionReference.labelName("Label789");

    private final static SpreadsheetLabelMapping MAPPING1 = LABEL1.mapping(A1);
    private final static SpreadsheetLabelMapping MAPPING2 = LABEL2.mapping(B2);
    private final static SpreadsheetLabelMapping MAPPING3 = LABEL3.mapping(C3);

    @Test
    public void testMissingCountFails() {
        this.handleOneFails(
                "Abc",
                Optional.empty(),
                Maps.empty(),
                IllegalArgumentException.class
        );
    }

    @Test
    public void testMissingCountFails2() {
        this.handleOneFails(
                "Abc",
                Optional.empty(),
                Maps.of(SpreadsheetEngineHateosHandlerSpreadsheetExpressionReferenceSimilarities.COUNT, Lists.empty()),
                IllegalArgumentException.class
        );
    }

    @Test
    public void testInvalidCount() {
        this.handleOneFails(
                "Abc",
                Optional.empty(),
                Maps.of(SpreadsheetEngineHateosHandlerSpreadsheetExpressionReferenceSimilarities.COUNT, Lists.of("???")),
                IllegalArgumentException.class
        );
    }

    @Test
    public void testHandleCellReference() {
        this.handleOneAndCheck2(
                "" + A1,
                2,
                A1
        );
    }

    @Test
    public void testHandleLabel() {
        this.handleOneAndCheck2(
                "" + LABEL1,
                2,
                null,
                MAPPING1
        );
    }

    @Test
    public void testHandleLabel3() {
        this.handleOneAndCheck2(
                "Label",
                4,
                null,
                MAPPING1,
                MAPPING2,
                MAPPING3
        );
    }

    @Test
    public void testHandleTextCellAndLabels() {
        this.handleOneAndCheck2(
                "A",
                4,
                null,
                MAPPING1,
                MAPPING2,
                MAPPING3
        );
    }

    @Test
    public void testHandleTextCellAndLabels2() {
        this.handleOneAndCheck2(
                "1",
                4,
                null,
                MAPPING1
        );
    }

    private void handleOneAndCheck2(final String text,
                                    final int count,
                                    final SpreadsheetCellReference cellReference,
                                    final SpreadsheetLabelMapping... labels) {
        this.handleOneAndCheck2(
                text,
                count,
                SpreadsheetExpressionReferenceSimilarities.with(Optional.ofNullable(cellReference), Sets.of(labels))
        );
    }

    private void handleOneAndCheck2(final String text,
                                    final int count,
                                    final SpreadsheetExpressionReferenceSimilarities expected) {
        this.handleOneAndCheck(
                text,
                Optional.empty(),
                Maps.of(SpreadsheetEngineHateosHandlerSpreadsheetExpressionReferenceSimilarities.COUNT, Lists.of("" + count)),
                Optional.ofNullable(expected)
        );
    }

    @Test
    public void testToString() {
        this.toStringAndCheck(this.createHandler(), "SpreadsheetLabelStore.findSimilarities");
    }

    @Override
    SpreadsheetEngineHateosHandlerSpreadsheetExpressionReferenceSimilarities createHandler(final SpreadsheetEngine engine,
                                                                                           final SpreadsheetEngineContext context) {
        return SpreadsheetEngineHateosHandlerSpreadsheetExpressionReferenceSimilarities.with(engine, context);
    }

    @Override
    SpreadsheetEngine engine() {
        return SpreadsheetEngines.fake();
    }

    @Override
    SpreadsheetEngineContext engineContext() {
        final SpreadsheetLabelStore store = SpreadsheetLabelStores.treeMap();
        store.save(MAPPING1);
        store.save(MAPPING2);
        store.save(MAPPING3);

        return new FakeSpreadsheetEngineContext() {

            @Override
            public SpreadsheetStoreRepository storeRepository() {
                return new FakeSpreadsheetStoreRepository() {

                    @Override
                    public SpreadsheetLabelStore labels() {
                        return store;
                    }
                };
            }
        };
    }

    @Override
    public String id() {
        return null;
    }

    @Override
    public List<String> list() {
        return Lists.of("B2");
    }

    @Override
    public Range<String> range() {
        return Range.singleton("A1");
    }

    @Override
    public Optional<SpreadsheetExpressionReferenceSimilarities> resource() {
        return Optional.empty();
    }

    @Override
    public Optional<SpreadsheetExpressionReferenceSimilarities> collectionResource() {
        return Optional.empty();
    }

    @Override
    public Map<HttpRequestAttribute<?>, Object> parameters() {
        return Maps.of(SpreadsheetEngineHateosHandlerSpreadsheetExpressionReferenceSimilarities.COUNT, 3);
    }

    @Override
    public Class<SpreadsheetEngineHateosHandlerSpreadsheetExpressionReferenceSimilarities> type() {
        return SpreadsheetEngineHateosHandlerSpreadsheetExpressionReferenceSimilarities.class;
    }
}

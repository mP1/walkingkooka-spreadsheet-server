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
import walkingkooka.ToStringTesting;
import walkingkooka.collect.Range;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerTesting;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContexts;
import walkingkooka.spreadsheet.server.SpreadsheetUrlQueryParameters;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStores;
import walkingkooka.spreadsheet.store.repo.FakeSpreadsheetStoreRepository;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class SpreadsheetExpressionReferenceSimilaritiesHateosResourceHandlerTest implements HateosResourceHandlerTesting<SpreadsheetExpressionReferenceSimilaritiesHateosResourceHandler,
    String,
    SpreadsheetExpressionReferenceSimilarities,
    SpreadsheetExpressionReferenceSimilarities,
    SpreadsheetEngineHateosResourceHandlerContext>,
    ToStringTesting<SpreadsheetExpressionReferenceSimilaritiesHateosResourceHandler> {

    private final static SpreadsheetCellReference A1 = SpreadsheetSelection.A1;
    private final static SpreadsheetCellReference B2 = SpreadsheetSelection.parseCell("B2");
    private final static SpreadsheetCellReference C3 = SpreadsheetSelection.parseCell("C3");

    private final static SpreadsheetLabelName LABEL1 = SpreadsheetExpressionReference.labelName("Label123");
    private final static SpreadsheetLabelName LABEL2 = SpreadsheetExpressionReference.labelName("Label456");
    private final static SpreadsheetLabelName LABEL3 = SpreadsheetExpressionReference.labelName("Label789");

    private final static SpreadsheetLabelMapping MAPPING1 = LABEL1.setLabelMappingReference(A1);
    private final static SpreadsheetLabelMapping MAPPING2 = LABEL2.setLabelMappingReference(B2);
    private final static SpreadsheetLabelMapping MAPPING3 = LABEL3.setLabelMappingReference(C3);

    private final static SpreadsheetEngineContext ENGINE_CONTEXT = new FakeSpreadsheetEngineContext() {

        @Override
        public SpreadsheetStoreRepository storeRepository() {
            return new FakeSpreadsheetStoreRepository() {

                {
                    final SpreadsheetLabelStore store = SpreadsheetLabelStores.treeMap();
                    store.save(MAPPING1);
                    store.save(MAPPING2);
                    store.save(MAPPING3);
                    this.store = store;
                }

                @Override
                public SpreadsheetLabelStore labels() {
                    return this.store;
                }

                private final SpreadsheetLabelStore store;
            };
        }
    };

    @Test
    public void testMissingCountFails() {
        this.handleOneFails(
            "Abc",
            Optional.empty(),
            Maps.empty(),
            this.context(),
            IllegalArgumentException.class
        );
    }

    @Test
    public void testMissingCountFails2() {
        this.handleOneFails(
            "Abc",
            Optional.empty(),
            Maps.of(SpreadsheetUrlQueryParameters.COUNT, Lists.empty()),
            this.context(),
            IllegalArgumentException.class
        );
    }

    @Test
    public void testInvalidCount() {
        this.handleOneFails(
            "Abc",
            Optional.empty(),
            Maps.of(SpreadsheetUrlQueryParameters.COUNT, Lists.of("???")),
            this.context(),
            IllegalArgumentException.class
        );
    }

    @Test
    public void testHandleCellReference() {
        this.handleOneAndCheck2(
            "" + A1,
            2,
            A1,
            null
        );
    }

    @Test
    public void testHandleLabelWithoutMapping() {
        final String label = "UnknownLabel999";

        this.handleOneAndCheck2(
            label,
            2,
            null,
            SpreadsheetSelection.labelName(label)
        );
    }

    @Test
    public void testHandleLabelWithMapping() {
        this.handleOneAndCheck2(
            "" + LABEL1,
            2,
            null,
            null,
            MAPPING1
        );
    }

    @Test
    public void testHandleLabelWithMapping3() {
        this.handleOneAndCheck2(
            "Label",
            4,
            null,
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
            null,
            MAPPING1
        );
    }

    private void handleOneAndCheck2(final String text,
                                    final int count,
                                    final SpreadsheetCellReference cellReference,
                                    final SpreadsheetLabelName label,
                                    final SpreadsheetLabelMapping... mappings) {
        this.handleOneAndCheck2(
            text,
            count,
            SpreadsheetExpressionReferenceSimilarities.with(
                Optional.ofNullable(cellReference),
                Optional.ofNullable(label),
                Sets.of(mappings)
            )
        );
    }

    private void handleOneAndCheck2(final String text,
                                    final int count,
                                    final SpreadsheetExpressionReferenceSimilarities expected) {
        this.handleOneAndCheck(
            text,
            Optional.empty(),
            Maps.of(SpreadsheetUrlQueryParameters.COUNT, Lists.of("" + count)),
            this.context(),
            Optional.ofNullable(expected)
        );
    }

    @Override
    public SpreadsheetExpressionReferenceSimilaritiesHateosResourceHandler createHandler() {
        return SpreadsheetExpressionReferenceSimilaritiesHateosResourceHandler.with(ENGINE_CONTEXT);
    }

    @Override
    public String id() {
        return null;
    }

    @Override
    public Set<String> manyIds() {
        return Sets.of("B2");
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
        return Maps.of(SpreadsheetUrlQueryParameters.COUNT, 3);
    }

    @Override
    public SpreadsheetEngineHateosResourceHandlerContext context() {
        return SpreadsheetEngineHateosResourceHandlerContexts.fake();
    }

    @Test
    public void testToString() {
        this.toStringAndCheck(this.createHandler(), "SpreadsheetLabelStore.findSimilarities");
    }

    // ClassTesting.....................................................................................................

    @Override
    public Class<SpreadsheetExpressionReferenceSimilaritiesHateosResourceHandler> type() {
        return SpreadsheetExpressionReferenceSimilaritiesHateosResourceHandler.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }

    @Override
    public String typeNamePrefix() {
        return SpreadsheetExpressionReferenceSimilarities.class.getSimpleName();
    }
}

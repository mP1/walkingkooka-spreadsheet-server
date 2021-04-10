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
import walkingkooka.collect.Range;
import walkingkooka.collect.list.Lists;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SpreadsheetEngineHateosHandlerCellReferenceResolveTest extends SpreadsheetEngineHateosHandlerTestCase2<SpreadsheetEngineHateosHandlerCellReferenceResolve,
        String,
        SpreadsheetCellReference,
        SpreadsheetCellReference> {

    private final static SpreadsheetCellReference REFERENCE = SpreadsheetExpressionReference.parseCellReference("B1");
    private final static SpreadsheetExpressionReference LABEL = SpreadsheetExpressionReference.labelName("Label123");
    private final static SpreadsheetExpressionReference RANGE = SpreadsheetExpressionReference.parseRange("B1:B2");

    @Test
    public void testHandleCellReference() {
        this.handleOneAndCheck2(REFERENCE, REFERENCE);
    }

    @Test
    public void testHandleLabel() {
        this.handleOneAndCheck2(LABEL, REFERENCE);
    }

    @Test
    public void testHandleRange() {
        this.handleOneAndCheck2(RANGE, REFERENCE);
    }

    private void handleOneAndCheck2(final SpreadsheetExpressionReference id,
                                    final SpreadsheetCellReference cellReference) {
        this.handleOneAndCheck(
                id.toString(),
                Optional.empty(),
                HateosHandler.NO_PARAMETERS,
                Optional.ofNullable(cellReference)
        );
    }


    @Override
    SpreadsheetEngineHateosHandlerCellReferenceResolve createHandler(final SpreadsheetEngine engine,
                                                                     final SpreadsheetEngineContext context) {
        return SpreadsheetEngineHateosHandlerCellReferenceResolve.with(engine, context);
    }

    @Override
    SpreadsheetEngine engine() {
        return SpreadsheetEngines.fake();
    }

    @Override
    SpreadsheetEngineContext engineContext() {
        return new FakeSpreadsheetEngineContext() {
            @Override
            public SpreadsheetCellReference resolveCellReference(final SpreadsheetExpressionReference reference) {
                return REFERENCE;
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
    public Optional<SpreadsheetCellReference> resource() {
        return Optional.empty();
    }

    @Override
    public Optional<SpreadsheetCellReference> collectionResource() {
        return Optional.empty();
    }

    @Override
    public Map<HttpRequestAttribute<?>, Object> parameters() {
        return HateosHandler.NO_PARAMETERS;
    }

    @Override
    public Class<SpreadsheetEngineHateosHandlerCellReferenceResolve> type() {
        return SpreadsheetEngineHateosHandlerCellReferenceResolve.class;
    }
}

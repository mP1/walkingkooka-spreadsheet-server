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
import walkingkooka.net.UrlPath;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetCellFindQuery;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContexts;

import java.util.Map;
import java.util.Optional;

public final class SpreadsheetDeltaHateosResourceHandlerFindLabelsByNameTest extends SpreadsheetDeltaHateosResourceHandlerTestCase2<SpreadsheetDeltaHateosResourceHandlerFindLabelsByName,
    SpreadsheetLabelName> {

    private final static int OFFSET = 12;
    private final static int COUNT = 34;

    private final static SpreadsheetLabelName LABEL = SpreadsheetSelection.labelName("Label123");

    @Test
    public void testHandleAll() {
        final SpreadsheetDelta expected = SpreadsheetDelta.EMPTY.setLabels(
            Sets.of(
                LABEL.setLabelMappingReference(SpreadsheetSelection.A1)
            )
        );

        final String text = "label123";

        this.handleAllAndCheck(
            Optional.empty(), // resource
            Maps.of(
                SpreadsheetCellFindQuery.OFFSET, Lists.of("" + OFFSET),
                SpreadsheetCellFindQuery.COUNT, Lists.of("" + COUNT)
            ), // parameters
            UrlPath.parse("/" + text),
            this.context(
                new FakeSpreadsheetEngine() {

                    @Override
                    public SpreadsheetDelta findLabelsByName(final String t,
                                                             final int offset,
                                                             final int count,
                                                             final SpreadsheetEngineContext context) {
                        checkEquals(text, t);
                        checkEquals(OFFSET, offset);
                        checkEquals(COUNT, count);
                        return expected;
                    }
                }
            ),
            Optional.of(
                expected
            )
        );
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(
            this.createHandler(),
            SpreadsheetEngine.class.getSimpleName() + ".findLabelsByName"
        );
    }

    @Override
    public SpreadsheetDeltaHateosResourceHandlerFindLabelsByName createHandler() {
        return SpreadsheetDeltaHateosResourceHandlerFindLabelsByName.INSTANCE;
    }

    @Override
    public Map<HttpRequestAttribute<?>, Object> parameters() {
        return Maps.empty();
    }

    @Override
    public UrlPath path() {
        return UrlPath.EMPTY;
    }

    @Override
    public SpreadsheetLabelName id() {
        return LABEL;
    }

    @Override
    public Range<SpreadsheetLabelName> range() {
        return Range.singleton(LABEL);
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
    public SpreadsheetEngineHateosResourceHandlerContext context() {
        return SpreadsheetEngineHateosResourceHandlerContexts.fake();
    }

    // ClassTesting.....................................................................................................

    @Override
    public Class<SpreadsheetDeltaHateosResourceHandlerFindLabelsByName> type() {
        return SpreadsheetDeltaHateosResourceHandlerFindLabelsByName.class;
    }
}

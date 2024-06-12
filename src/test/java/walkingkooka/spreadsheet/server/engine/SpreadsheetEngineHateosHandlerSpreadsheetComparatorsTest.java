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

package walkingkooka.spreadsheet.server.engine;

import org.junit.jupiter.api.Test;
import walkingkooka.ToStringTesting;
import walkingkooka.collect.Range;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.Url;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorInfo;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorInfoSet;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorName;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class SpreadsheetEngineHateosHandlerSpreadsheetComparatorsTest extends SpreadsheetEngineHateosHandlerTestCase2<SpreadsheetEngineHateosHandlerSpreadsheetComparators,
        SpreadsheetComparatorName,
        SpreadsheetComparatorInfo,
        SpreadsheetComparatorInfoSet> implements ToStringTesting<SpreadsheetEngineHateosHandlerSpreadsheetComparators> {

    private final static SpreadsheetComparatorInfo INFO1 = SpreadsheetComparatorInfo.with(
            Url.parseAbsolute("https://example.com/1"),
            SpreadsheetComparatorName.with("comparator-1")
    );

    private final static SpreadsheetComparatorInfo INFO2 = SpreadsheetComparatorInfo.with(
            Url.parseAbsolute("https://example.com/2"),
            SpreadsheetComparatorName.with("comparator-2")
    );

    @Test
    public void testToString() {
        this.toStringAndCheck(
                this.createHandler(),
                "SpreadsheetEngineContext.spreadsheetComparatorInfos"
        );
    }

    @Test
    public void testHandleAll() {
        this.handleAllAndCheck(
                Optional.empty(), // resource
                Maps.empty(), // parameters
                Optional.of(
                        SpreadsheetComparatorInfoSet.with(
                                Sets.of(
                                        INFO1,
                                        INFO2
                                )
                        )
                )
        );
    }

    @Override
    SpreadsheetEngineHateosHandlerSpreadsheetComparators createHandler(final SpreadsheetEngine engine,
                                                                       final SpreadsheetEngineContext context) {
        return SpreadsheetEngineHateosHandlerSpreadsheetComparators.with(
                engine,
                context
        );
    }

    @Override
    SpreadsheetEngine engine() {
        return SpreadsheetEngines.fake();
    }

    @Override
    SpreadsheetEngineContext engineContext() {
        return new FakeSpreadsheetEngineContext() {

            @Override
            public Set<SpreadsheetComparatorInfo> spreadsheetComparatorInfos() {
                return Sets.of(
                        INFO1,
                        INFO2
                );
            }
        };
    }

    @Override
    public SpreadsheetComparatorName id() {
        return SpreadsheetComparatorName.with("id-spreadsheet-comparator-name");
    }

    @Override
    public Set<SpreadsheetComparatorName> manyIds() {
        return Sets.of(
                INFO1.name(),
                INFO2.name()
        );
    }

    @Override
    public Range<SpreadsheetComparatorName> range() {
        return Range.singleton(
                SpreadsheetComparatorName.with("range-spreadsheet-comparator-name")
        );
    }

    @Override
    public Optional<SpreadsheetComparatorInfo> resource() {
        return Optional.empty();
    }

    @Override
    public Optional<SpreadsheetComparatorInfoSet> collectionResource() {
        return Optional.empty();
    }

    @Override
    public Map<HttpRequestAttribute<?>, Object> parameters() {
        return Maps.empty();
    }

    @Override
    public Class<SpreadsheetEngineHateosHandlerSpreadsheetComparators> type() {
        return SpreadsheetEngineHateosHandlerSpreadsheetComparators.class;
    }
}

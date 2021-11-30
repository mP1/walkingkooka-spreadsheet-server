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

package walkingkooka.spreadsheet.server.engine.http;

import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnOrRowReference;

import java.util.Map;
import java.util.Optional;

public abstract class SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertTestCase<H extends SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsert<I>,
        I extends SpreadsheetColumnOrRowReference & Comparable<I>>
        extends SpreadsheetEngineHateosHandlerSpreadsheetDeltaTestCase<H, I> {

    SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertTestCase() {
        super();
    }

    @Override
    final SpreadsheetEngine engine() {
        return SpreadsheetEngines.fake();
    }

    @Override
    public final Optional<SpreadsheetDelta> resource() {
        return Optional.empty();
    }

    @Override
    public final Optional<SpreadsheetDelta> collectionResource() {
        return Optional.empty();
    }

    final static int COUNT = 2;

    @Override
    public final Map<HttpRequestAttribute<?>, Object> parameters() {
        return Maps.of(
                SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsert.COUNT, Lists.of("" + COUNT)
        );
    }
}

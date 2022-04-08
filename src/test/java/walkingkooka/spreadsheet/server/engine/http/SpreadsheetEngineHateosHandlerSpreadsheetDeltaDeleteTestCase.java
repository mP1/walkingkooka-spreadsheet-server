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

import org.junit.jupiter.api.Test;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnOrRowReference;

import java.util.Map;
import java.util.Optional;

public abstract class SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteTestCase<H extends SpreadsheetEngineHateosHandlerSpreadsheetDeltaDelete<I>,
        I extends SpreadsheetColumnOrRowReference & Comparable<I>>
        extends SpreadsheetEngineHateosHandlerSpreadsheetDeltaTestCase<H, I> {

    SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteTestCase() {
        super();
    }

    @Test
    public final void testIdNotEmptyResourceFails() {
        this.handleOneFails(
                this.id(),
                Optional.of(
                        SpreadsheetDelta.EMPTY
                ),
                this.parameters(),
                IllegalArgumentException.class);
    }

    @Test
    public final void testRangeNotEmptyResourceFails() {
        this.handleRangeFails(
                this.range(),
                Optional.of(
                        SpreadsheetDelta.EMPTY
                ),
                this.parameters(),
                IllegalArgumentException.class);
    }

    @Override final SpreadsheetEngine engine() {
        return new FakeSpreadsheetEngine();
    }

    @Override final SpreadsheetEngineContext engineContext() {
        return SpreadsheetEngineContexts.fake();
    }

    @Override final public Optional<SpreadsheetDelta> resource() {
        return Optional.empty();
    }

    @Override final public Optional<SpreadsheetDelta> collectionResource() {
        return Optional.empty();
    }

    @Override final public Map<HttpRequestAttribute<?>, Object> parameters() {
        return HateosHandler.NO_PARAMETERS;
    }
}

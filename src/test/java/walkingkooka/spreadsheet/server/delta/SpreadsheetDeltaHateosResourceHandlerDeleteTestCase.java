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
import walkingkooka.net.UrlPath;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;

import java.util.Map;
import java.util.Optional;

public abstract class SpreadsheetDeltaHateosResourceHandlerDeleteTestCase<H extends SpreadsheetDeltaHateosResourceHandlerDelete<I>,
    I extends SpreadsheetSelection & Comparable<I>>
    extends SpreadsheetDeltaHateosResourceHandlerTestCase2<H, I> {

    SpreadsheetDeltaHateosResourceHandlerDeleteTestCase() {
        super();
    }

    @Test
    public final void testHandleOneIdNotEmptyResourceFails() {
        this.handleOneFails(
            this.id(),
            Optional.of(
                SpreadsheetDelta.EMPTY
            ),
            this.parameters(),
            this.path(),
            this.context(),
            IllegalArgumentException.class);
    }

    @Test
    public final void testHandleRangeNotEmptyResourceFails() {
        this.handleRangeFails(
            this.range(),
            Optional.of(
                SpreadsheetDelta.EMPTY
            ),
            this.parameters(),
            this.path(),
            this.context(),
            IllegalArgumentException.class
        );
    }

    @Override
    final public Optional<SpreadsheetDelta> resource() {
        return Optional.empty();
    }

    @Override
    final public Optional<SpreadsheetDelta> collectionResource() {
        return Optional.empty();
    }

    @Override
    final public Map<HttpRequestAttribute<?>, Object> parameters() {
        return HateosResourceHandler.NO_PARAMETERS;
    }

    @Override
    public UrlPath path() {
        return UrlPath.EMPTY;
    }

    @Override
    public final SpreadsheetEngineHateosResourceHandlerContext context() {
        return CONTEXT;
    }
}

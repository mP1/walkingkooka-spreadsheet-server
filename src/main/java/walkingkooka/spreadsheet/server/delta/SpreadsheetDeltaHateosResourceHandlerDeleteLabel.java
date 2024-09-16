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

import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleAll;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleMany;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleNone;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleRange;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.server.SpreadsheetHateosResourceHandlerContext;

import java.util.Map;
import java.util.Optional;

/**
 * A {@link HateosResourceHandler} that calls {@link SpreadsheetEngine#deleteLabel(SpreadsheetLabelName, SpreadsheetEngineContext)}
 */
final class SpreadsheetDeltaHateosResourceHandlerDeleteLabel extends SpreadsheetDeltaHateosResourceHandler<SpreadsheetLabelName>
        implements UnsupportedHateosResourceHandlerHandleNone<SpreadsheetLabelName, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetHateosResourceHandlerContext>,
        UnsupportedHateosResourceHandlerHandleRange<SpreadsheetLabelName, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetHateosResourceHandlerContext>,
        UnsupportedHateosResourceHandlerHandleMany<SpreadsheetLabelName, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetHateosResourceHandlerContext>,
        UnsupportedHateosResourceHandlerHandleAll<SpreadsheetLabelName, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetHateosResourceHandlerContext> {

    static SpreadsheetDeltaHateosResourceHandlerDeleteLabel with(final SpreadsheetEngine engine) {
        return new SpreadsheetDeltaHateosResourceHandlerDeleteLabel(
                check(engine)
        );
    }

    private SpreadsheetDeltaHateosResourceHandlerDeleteLabel(final SpreadsheetEngine engine) {
        super(engine);
    }

    // handleOne........................................................................................................

    @Override
    public Optional<SpreadsheetDelta> handleOne(final SpreadsheetLabelName label,
                                                final Optional<SpreadsheetDelta> resource,
                                                final Map<HttpRequestAttribute<?>, Object> parameters,
                                                final SpreadsheetHateosResourceHandlerContext context) {
        checkLabel(label);
        HateosResourceHandler.checkResourceEmpty(resource);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkContext(context);

        return Optional.of(
                this.prepareResponse(
                        resource,
                        parameters,
                        context,
                        this.engine.deleteLabel(
                                label,
                                context
                        )
                )
        );
    }

    @Override
    String operation() {
        return "removeLabel";
    }
}
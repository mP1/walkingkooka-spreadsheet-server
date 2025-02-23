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
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A {@link HateosResourceHandler} that calls {@link SpreadsheetEngine#saveLabel(SpreadsheetLabelMapping, SpreadsheetEngineContext)}.
 */
final class SpreadsheetDeltaHateosResourceHandlerSaveLabel extends SpreadsheetDeltaHateosResourceHandler<SpreadsheetLabelName>
    implements UnsupportedHateosResourceHandlerHandleNone<SpreadsheetLabelName, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleRange<SpreadsheetLabelName, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleMany<SpreadsheetLabelName, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleAll<SpreadsheetLabelName, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetEngineHateosResourceHandlerContext> {

    static SpreadsheetDeltaHateosResourceHandlerSaveLabel with(final SpreadsheetEngine engine) {
        return new SpreadsheetDeltaHateosResourceHandlerSaveLabel(engine);
    }

    // handleNone.......................................................................................................

    private SpreadsheetDeltaHateosResourceHandlerSaveLabel(final SpreadsheetEngine engine) {
        super(engine);
    }

    @Override
    public Optional<SpreadsheetDelta> handleNone(final Optional<SpreadsheetDelta> resource,
                                                 final Map<HttpRequestAttribute<?>, Object> parameters,
                                                 final SpreadsheetEngineHateosResourceHandlerContext context) {
        return this.saveOrUpdate(
            resource,
            parameters,
            context
        );
    }

    // handleOne........................................................................................................

    @Override
    public Optional<SpreadsheetDelta> handleOne(final SpreadsheetLabelName label,
                                                final Optional<SpreadsheetDelta> resource,
                                                final Map<HttpRequestAttribute<?>, Object> parameters,
                                                final SpreadsheetEngineHateosResourceHandlerContext context) {
        checkLabel(label);

        return this.saveOrUpdate(
            resource,
            parameters,
            context
        );
    }

    private Optional<SpreadsheetDelta> saveOrUpdate(final Optional<SpreadsheetDelta> resource,
                                                    final Map<HttpRequestAttribute<?>, Object> parameters,
                                                    final SpreadsheetEngineHateosResourceHandlerContext context) {
        final SpreadsheetDelta delta = HateosResourceHandler.checkResourceNotEmpty(resource);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkContext(context);

        final Set<SpreadsheetLabelMapping> mappings = delta.labels();
        final int count = mappings.size();

        final SpreadsheetLabelMapping labelMapping;

        switch (count) {
            case 0:
                throw new IllegalArgumentException("Missing label mappings");
            case 1:
                labelMapping = mappings.iterator()
                    .next();
                break;
            default:
                throw new IllegalArgumentException("Got " + count + " labels expected 1");
        }

        return Optional.of(
            this.engine.saveLabel(
                labelMapping,
                context
            )
        );
    }

    @Override
    String operation() {
        return "saveOrUpdateLabel";
    }
}

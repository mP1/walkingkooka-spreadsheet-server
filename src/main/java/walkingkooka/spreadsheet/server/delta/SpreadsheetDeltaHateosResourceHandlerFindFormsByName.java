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

import walkingkooka.net.UrlPath;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleMany;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleNone;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleOne;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleRange;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.net.SpreadsheetUrlPathTemplate;
import walkingkooka.spreadsheet.server.net.SpreadsheetUrlQueryParameters;
import walkingkooka.validation.form.FormName;

import java.util.Map;
import java.util.Optional;

/**
 * A {@link HateosResourceHandler} that finds {@link walkingkooka.validation.form.Form} for a given {@link String form name}.
 * <pre>
 * /api/spreadsheet/SpreadsheetName/form/&star;/findByName/QUERY
 * </pre>
 */
final class SpreadsheetDeltaHateosResourceHandlerFindFormsByName extends SpreadsheetDeltaHateosResourceHandler<FormName>
    implements UnsupportedHateosResourceHandlerHandleNone<FormName, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleOne<FormName, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleRange<FormName, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleMany<FormName, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetEngineHateosResourceHandlerContext> {

    /**
     * Singleton
     */
    final static SpreadsheetDeltaHateosResourceHandlerFindFormsByName INSTANCE = new SpreadsheetDeltaHateosResourceHandlerFindFormsByName();

    private SpreadsheetDeltaHateosResourceHandlerFindFormsByName() {
        super();
    }

    // /api/spreadsheet/SpreadsheetId/form/*/findByName/query-here
    @Override
    public Optional<SpreadsheetDelta> handleAll(final Optional<SpreadsheetDelta> resource,
                                                final Map<HttpRequestAttribute<?>, Object> parameters,
                                                final UrlPath path,
                                                final SpreadsheetEngineHateosResourceHandlerContext context) {
        HateosResourceHandler.checkResourceEmpty(resource);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkPath(path);
        HateosResourceHandler.checkContext(context);

        return Optional.of(
            context.spreadsheetEngine()
                .findFormsByName(
                    SpreadsheetUrlPathTemplate.removeRootSlashIfNecessary(
                        path.value()
                    ), // skip leading slash
                    SpreadsheetUrlQueryParameters.offset(parameters)
                        .orElse(0),
                    SpreadsheetUrlQueryParameters.count(parameters)
                        .orElse(DEFAULT_COUNT),
                    context
                )
        );
    }

    final static int DEFAULT_COUNT = 50;

    @Override
    String operation() {
        return "findFormsByName";
    }
}

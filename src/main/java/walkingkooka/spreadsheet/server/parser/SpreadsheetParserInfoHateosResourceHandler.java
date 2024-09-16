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

package walkingkooka.spreadsheet.server.parser;

import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleMany;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleNone;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleRange;
import walkingkooka.spreadsheet.parser.SpreadsheetParserInfo;
import walkingkooka.spreadsheet.parser.SpreadsheetParserInfoSet;
import walkingkooka.spreadsheet.parser.SpreadsheetParserName;
import walkingkooka.spreadsheet.server.SpreadsheetHateosResourceHandlerContext;

import java.util.Map;
import java.util.Optional;

/**
 * Provides a single end point to retrieve ALL the {@link SpreadsheetParserInfo} available to this spreadsheet.
 * GETS for individual or a range are not supported and throw {@link UnsupportedOperationException}.
 */
final class SpreadsheetParserInfoHateosResourceHandler implements HateosResourceHandler<SpreadsheetParserName, SpreadsheetParserInfo, SpreadsheetParserInfoSet, SpreadsheetHateosResourceHandlerContext>,
        UnsupportedHateosResourceHandlerHandleMany<SpreadsheetParserName, SpreadsheetParserInfo, SpreadsheetParserInfoSet, SpreadsheetHateosResourceHandlerContext>,
        UnsupportedHateosResourceHandlerHandleNone<SpreadsheetParserName, SpreadsheetParserInfo, SpreadsheetParserInfoSet, SpreadsheetHateosResourceHandlerContext>,
        UnsupportedHateosResourceHandlerHandleRange<SpreadsheetParserName, SpreadsheetParserInfo, SpreadsheetParserInfoSet, SpreadsheetHateosResourceHandlerContext> {

    /**
     * Singleton
     */
    final static SpreadsheetParserInfoHateosResourceHandler INSTANCE = new SpreadsheetParserInfoHateosResourceHandler();

    private SpreadsheetParserInfoHateosResourceHandler() {
        super();
    }

    @Override
    public Optional<SpreadsheetParserInfoSet> handleAll(final Optional<SpreadsheetParserInfoSet> infos,
                                                        final Map<HttpRequestAttribute<?>, Object> parameters,
                                                        final SpreadsheetHateosResourceHandlerContext context) {
        HateosResourceHandler.checkResourceEmpty(infos);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkContext(context);

        return Optional.of(
                context.spreadsheetParserInfos()
        );
    }

    @Override
    public Optional<SpreadsheetParserInfo> handleOne(final SpreadsheetParserName name,
                                                     final Optional<SpreadsheetParserInfo> info,
                                                     final Map<HttpRequestAttribute<?>, Object> parameters,
                                                     final SpreadsheetHateosResourceHandlerContext context) {
        HateosResourceHandler.checkId(name);
        HateosResourceHandler.checkResource(info);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkContext(context);

        return context.spreadsheetParserInfos()
                .stream()
                .filter(i -> i.name().equals(name))
                .findFirst();
    }

    @Override
    public String toString() {
        return "SpreadsheetEngineContext.spreadsheetParserInfos";
    }
}

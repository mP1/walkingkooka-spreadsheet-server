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

package walkingkooka.spreadsheet.server.formatter;

import walkingkooka.net.UrlPath;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleMany;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleNone;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleRange;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterInfo;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterInfoSet;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterName;
import walkingkooka.spreadsheet.server.SpreadsheetProviderHateosResourceHandlerContext;

import java.util.Map;
import java.util.Optional;

/**
 * Provides a single end point to retrieve ALL the {@link SpreadsheetFormatterInfo} available to this spreadsheet.
 * GETS for individual or a range are not supported and throw {@link UnsupportedOperationException}.
 */
final class SpreadsheetFormatterInfoHateosResourceHandler implements HateosResourceHandler<SpreadsheetFormatterName, SpreadsheetFormatterInfo, SpreadsheetFormatterInfoSet, SpreadsheetProviderHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleMany<SpreadsheetFormatterName, SpreadsheetFormatterInfo, SpreadsheetFormatterInfoSet, SpreadsheetProviderHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleNone<SpreadsheetFormatterName, SpreadsheetFormatterInfo, SpreadsheetFormatterInfoSet, SpreadsheetProviderHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleRange<SpreadsheetFormatterName, SpreadsheetFormatterInfo, SpreadsheetFormatterInfoSet, SpreadsheetProviderHateosResourceHandlerContext> {

    /**
     * Singleton
     */
    final static SpreadsheetFormatterInfoHateosResourceHandler INSTANCE = new SpreadsheetFormatterInfoHateosResourceHandler();

    private SpreadsheetFormatterInfoHateosResourceHandler() {
        super();
    }

    @Override
    public Optional<SpreadsheetFormatterInfoSet> handleAll(final Optional<SpreadsheetFormatterInfoSet> infos,
                                                           final Map<HttpRequestAttribute<?>, Object> parameters,
                                                           final UrlPath path,
                                                           final SpreadsheetProviderHateosResourceHandlerContext context) {
        HateosResourceHandler.checkResourceEmpty(infos);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkPathEmpty(path);
        HateosResourceHandler.checkContext(context);

        return Optional.of(
            context.spreadsheetProvider()
                .spreadsheetFormatterInfos()
        );
    }

    @Override
    public Optional<SpreadsheetFormatterInfo> handleOne(final SpreadsheetFormatterName name,
                                                        final Optional<SpreadsheetFormatterInfo> info,
                                                        final Map<HttpRequestAttribute<?>, Object> parameters,
                                                        final UrlPath path,
                                                        final SpreadsheetProviderHateosResourceHandlerContext context) {
        HateosResourceHandler.checkId(name);
        HateosResourceHandler.checkResource(info);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkPathEmpty(path);
        HateosResourceHandler.checkContext(context);

        return context.spreadsheetProvider()
            .spreadsheetFormatterInfos()
            .stream()
            .filter(i -> i.name().equals(name))
            .findFirst();
    }

    @Override
    public String toString() {
        return "SpreadsheetProvider.spreadsheetFormatterInfos";
    }
}

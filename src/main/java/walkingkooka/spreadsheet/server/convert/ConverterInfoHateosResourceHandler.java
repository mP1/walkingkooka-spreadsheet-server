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

package walkingkooka.spreadsheet.server.convert;

import walkingkooka.convert.provider.ConverterInfo;
import walkingkooka.convert.provider.ConverterInfoSet;
import walkingkooka.convert.provider.ConverterName;
import walkingkooka.net.UrlPath;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleMany;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleNone;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleRange;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;

import java.util.Map;
import java.util.Optional;

/**
 * Provides a single end point to retrieve ALL the {@link ConverterInfo} available to this spreadsheet.
 * GETS for individual or a range are not supported and throw {@link UnsupportedOperationException}.
 */
final class ConverterInfoHateosResourceHandler implements HateosResourceHandler<ConverterName, ConverterInfo, ConverterInfoSet, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleMany<ConverterName, ConverterInfo, ConverterInfoSet, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleNone<ConverterName, ConverterInfo, ConverterInfoSet, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleRange<ConverterName, ConverterInfo, ConverterInfoSet, SpreadsheetEngineHateosResourceHandlerContext> {

    final static ConverterInfoHateosResourceHandler INSTANCE = new ConverterInfoHateosResourceHandler();

    private ConverterInfoHateosResourceHandler() {
        super();
    }

    @Override
    public Optional<ConverterInfoSet> handleAll(final Optional<ConverterInfoSet> infos,
                                                final Map<HttpRequestAttribute<?>, Object> parameters,
                                                final UrlPath path,
                                                final SpreadsheetEngineHateosResourceHandlerContext context) {
        HateosResourceHandler.checkResourceEmpty(infos);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkPathEmpty(path);
        HateosResourceHandler.checkContext(context);

        return Optional.of(
            ConverterInfoSet.with(
                context.systemSpreadsheetProvider()
                    .converterInfos()
            )
        );
    }

    @Override
    public Optional<ConverterInfo> handleOne(final ConverterName name,
                                             final Optional<ConverterInfo> info,
                                             final Map<HttpRequestAttribute<?>, Object> parameters,
                                             final UrlPath path,
                                             final SpreadsheetEngineHateosResourceHandlerContext context) {
        HateosResourceHandler.checkId(name);
        HateosResourceHandler.checkResource(info);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkPathEmpty(path);
        HateosResourceHandler.checkContext(context);

        return context.systemSpreadsheetProvider()
            .converterInfos()
            .stream()
            .filter(i -> i.name().equals(name))
            .findFirst();
    }

    @Override
    public String toString() {
        return "SpreadsheetEngineContext.converterInfos";
    }
}

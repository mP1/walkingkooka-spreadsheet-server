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

package walkingkooka.spreadsheet.server.comparator;

import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleMany;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleNone;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleRange;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorInfo;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorInfoSet;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorName;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Provides a single end point to retrieve ALL the {@link SpreadsheetComparatorInfo} available to this spreadsheet.
 * GETS for individual or a range are not supported and throw {@link UnsupportedOperationException}.
 */
final class SpreadsheetComparatorInfoHateosResourceHandler implements HateosResourceHandler<SpreadsheetComparatorName, SpreadsheetComparatorInfo, SpreadsheetComparatorInfoSet>,
        UnsupportedHateosResourceHandlerHandleMany<SpreadsheetComparatorName, SpreadsheetComparatorInfo, SpreadsheetComparatorInfoSet>,
        UnsupportedHateosResourceHandlerHandleNone<SpreadsheetComparatorName, SpreadsheetComparatorInfo, SpreadsheetComparatorInfoSet>,
        UnsupportedHateosResourceHandlerHandleRange<SpreadsheetComparatorName, SpreadsheetComparatorInfo, SpreadsheetComparatorInfoSet> {

    static SpreadsheetComparatorInfoHateosResourceHandler with(final SpreadsheetEngineContext context) {
        return new SpreadsheetComparatorInfoHateosResourceHandler(
                Objects.requireNonNull(context, "context")
        );
    }

    private SpreadsheetComparatorInfoHateosResourceHandler(final SpreadsheetEngineContext context) {
        super();
        this.context = context;
    }

    @Override
    public Optional<SpreadsheetComparatorInfoSet> handleAll(final Optional<SpreadsheetComparatorInfoSet> infos,
                                                             final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosResourceHandler.checkResourceEmpty(infos);
        HateosResourceHandler.checkParameters(parameters);

        return Optional.of(
                SpreadsheetComparatorInfoSet.with(
                        this.context.spreadsheetComparatorInfos()
                )
        );
    }

    @Override
    public Optional<SpreadsheetComparatorInfo> handleOne(final SpreadsheetComparatorName name,
                                                         final Optional<SpreadsheetComparatorInfo> info,
                                                         final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosResourceHandler.checkId(name);
        HateosResourceHandler.checkResource(info);
        HateosResourceHandler.checkParameters(parameters);

        return this.context.spreadsheetComparatorInfos()
                .stream()
                .filter(i -> i.name().equals(name))
                .findFirst();
    }

    private final SpreadsheetEngineContext context;

    @Override
    public String toString() {
        return "SpreadsheetEngineContext.spreadsheetComparatorInfos";
    }
}

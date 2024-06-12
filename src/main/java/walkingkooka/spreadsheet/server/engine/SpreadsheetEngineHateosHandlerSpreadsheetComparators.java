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

package walkingkooka.spreadsheet.server.engine;

import walkingkooka.collect.Range;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorInfo;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorInfoSet;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorName;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;

import java.util.Map;
import java.util.Optional;

/**
 * Provides a single end point to retrieve ALL the {@link SpreadsheetComparatorInfo} available to this spreadsheet.
 * GETS for individual or a range are not supported and throw {@link UnsupportedOperationException}.
 */
final class SpreadsheetEngineHateosHandlerSpreadsheetComparators extends SpreadsheetEngineHateosHandler<SpreadsheetComparatorName, SpreadsheetComparatorInfo, SpreadsheetComparatorInfoSet> {

    static SpreadsheetEngineHateosHandlerSpreadsheetComparators with(final SpreadsheetEngine engine,
                                                                     final SpreadsheetEngineContext context) {
        check(
                engine,
                context
        );
        return new SpreadsheetEngineHateosHandlerSpreadsheetComparators(engine, context);
    }

    private SpreadsheetEngineHateosHandlerSpreadsheetComparators(final SpreadsheetEngine engine,
                                                                 final SpreadsheetEngineContext context) {
        super(engine, context);
    }

    @Override
    public Optional<SpreadsheetComparatorInfoSet> handleAll(final Optional<SpreadsheetComparatorInfoSet> infos,
                                                             final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosHandler.checkResourceEmpty(infos);
        HateosHandler.checkParameters(parameters);

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
        HateosHandler.checkId(name);
        HateosHandler.checkResource(info);
        HateosHandler.checkParameters(parameters);

        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<SpreadsheetComparatorInfoSet> handleRange(final Range<SpreadsheetComparatorName> nameRange,
                                                              final Optional<SpreadsheetComparatorInfoSet> infos,
                                                               final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosHandler.checkIdRange(nameRange);
        HateosHandler.checkResource(infos);
        HateosHandler.checkParameters(parameters);

        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "SpreadsheetEngineContext.spreadsheetComparatorInfos";
    }
}

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

package walkingkooka.spreadsheet.server.validation;

import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleMany;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleNone;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleRange;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.validation.provider.ValidatorInfo;
import walkingkooka.validation.provider.ValidatorInfoSet;
import walkingkooka.validation.provider.ValidatorName;

import java.util.Map;
import java.util.Optional;

/**
 * Provides a single end point to retrieve ALL the {@link ValidatorInfo} available to this spreadsheet.
 * GETS for individual or a range are not supported and throw {@link UnsupportedOperationException}.
 */
final class ValidatorInfoHateosResourceHandler implements HateosResourceHandler<ValidatorName, ValidatorInfo, ValidatorInfoSet, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleMany<ValidatorName, ValidatorInfo, ValidatorInfoSet, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleNone<ValidatorName, ValidatorInfo, ValidatorInfoSet, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleRange<ValidatorName, ValidatorInfo, ValidatorInfoSet, SpreadsheetEngineHateosResourceHandlerContext> {

    /**
     * Singleton
     */
    final static ValidatorInfoHateosResourceHandler INSTANCE = new ValidatorInfoHateosResourceHandler();

    private ValidatorInfoHateosResourceHandler() {
        super();
    }

    @Override
    public Optional<ValidatorInfoSet> handleAll(final Optional<ValidatorInfoSet> infos,
                                                final Map<HttpRequestAttribute<?>, Object> parameters,
                                                final SpreadsheetEngineHateosResourceHandlerContext context) {
        HateosResourceHandler.checkResourceEmpty(infos);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkContext(context);

        return Optional.of(
            context.systemSpreadsheetProvider()
                .validatorInfos()
        );
    }

    @Override
    public Optional<ValidatorInfo> handleOne(final ValidatorName name,
                                             final Optional<ValidatorInfo> info,
                                             final Map<HttpRequestAttribute<?>, Object> parameters,
                                             final SpreadsheetEngineHateosResourceHandlerContext context) {
        HateosResourceHandler.checkId(name);
        HateosResourceHandler.checkResource(info);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkContext(context);

        return context.systemSpreadsheetProvider()
            .validatorInfos()
            .stream()
            .filter(i -> i.name().equals(name))
            .findFirst();
    }

    @Override
    public String toString() {
        return "systemSpreadsheetProvider.validatorInfos";
    }
}

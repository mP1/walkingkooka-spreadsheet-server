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

package walkingkooka.spreadsheet.server.form;

import walkingkooka.net.UrlPath;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleMany;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleNone;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleRange;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.validation.form.provider.FormHandlerInfo;
import walkingkooka.validation.form.provider.FormHandlerInfoSet;
import walkingkooka.validation.form.provider.FormHandlerName;

import java.util.Map;
import java.util.Optional;

/**
 * Provides a single end point to retrieve ALL the {@link FormHandlerInfo} available to this spreadsheet.
 * GETS for individual or a range are not supported and throw {@link UnsupportedOperationException}.
 */
final class FormHandlerInfoHateosResourceHandler implements HateosResourceHandler<FormHandlerName, FormHandlerInfo, FormHandlerInfoSet, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleMany<FormHandlerName, FormHandlerInfo, FormHandlerInfoSet, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleNone<FormHandlerName, FormHandlerInfo, FormHandlerInfoSet, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleRange<FormHandlerName, FormHandlerInfo, FormHandlerInfoSet, SpreadsheetEngineHateosResourceHandlerContext> {

    /**
     * Singleton
     */
    final static FormHandlerInfoHateosResourceHandler INSTANCE = new FormHandlerInfoHateosResourceHandler();

    private FormHandlerInfoHateosResourceHandler() {
        super();
    }

    @Override
    public Optional<FormHandlerInfoSet> handleAll(final Optional<FormHandlerInfoSet> infos,
                                                final Map<HttpRequestAttribute<?>, Object> parameters,
                                                final UrlPath path,
                                                final SpreadsheetEngineHateosResourceHandlerContext context) {
        HateosResourceHandler.checkResourceEmpty(infos);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkPathEmpty(path);
        HateosResourceHandler.checkContext(context);

        return Optional.of(
            context.systemSpreadsheetProvider()
                .formHandlerInfos()
        );
    }

    @Override
    public Optional<FormHandlerInfo> handleOne(final FormHandlerName name,
                                             final Optional<FormHandlerInfo> info,
                                             final Map<HttpRequestAttribute<?>, Object> parameters,
                                             final UrlPath path,
                                             final SpreadsheetEngineHateosResourceHandlerContext context) {
        HateosResourceHandler.checkId(name);
        HateosResourceHandler.checkResource(info);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkPathEmpty(path);
        HateosResourceHandler.checkContext(context);

        return context.systemSpreadsheetProvider()
            .formHandlerInfos()
            .stream()
            .filter(i -> i.name().equals(name))
            .findFirst();
    }

    @Override
    public String toString() {
        return "systemSpreadsheetProvider.formHandlerInfos";
    }
}

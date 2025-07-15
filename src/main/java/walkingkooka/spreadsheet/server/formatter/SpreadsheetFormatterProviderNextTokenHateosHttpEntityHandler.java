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
import walkingkooka.net.header.CharsetName;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHttpEntityHandler;
import walkingkooka.net.http.server.hateos.HateosResourceMappings;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleMany;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleNone;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleOne;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleRange;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterName;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSelector;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSelectorToken;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;

import java.util.Map;

/**
 * A handler that takes the {@link SpreadsheetFormatterName} and request body {@link String} to make a {@link SpreadsheetFormatterSelector} and then invokes {@link walkingkooka.spreadsheet.format.SpreadsheetFormatterProvider#spreadsheetFormatter(SpreadsheetFormatterSelector, ProviderContext)}.
 */
final class SpreadsheetFormatterProviderNextTokenHateosHttpEntityHandler implements HateosHttpEntityHandler<SpreadsheetFormatterName, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosHttpEntityHandlerHandleMany<SpreadsheetFormatterName, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosHttpEntityHandlerHandleNone<SpreadsheetFormatterName, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosHttpEntityHandlerHandleOne<SpreadsheetFormatterName, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosHttpEntityHandlerHandleRange<SpreadsheetFormatterName, SpreadsheetEngineHateosResourceHandlerContext> {

    static SpreadsheetFormatterProviderNextTokenHateosHttpEntityHandler instance() {
        return INSTANCE;
    }

    private final static SpreadsheetFormatterProviderNextTokenHateosHttpEntityHandler INSTANCE = new SpreadsheetFormatterProviderNextTokenHateosHttpEntityHandler();

    private SpreadsheetFormatterProviderNextTokenHateosHttpEntityHandler() {
    }

    @Override
    public HttpEntity handleAll(final HttpEntity httpEntity,
                                final Map<HttpRequestAttribute<?>, Object> parameters,
                                final UrlPath path,
                                final SpreadsheetEngineHateosResourceHandlerContext context) {
        HateosHttpEntityHandler.checkHttpEntity(httpEntity);
        HateosHttpEntityHandler.checkParameters(parameters);
        HateosHttpEntityHandler.checkPath(path);
        HateosHttpEntityHandler.checkContext(context);

        final MediaType requiredContentType = context.contentType();
        requiredContentType.requireContentType(
            HttpHeaderName.CONTENT_TYPE.header(httpEntity)
                .orElse(null)
        );
        HttpHeaderName.ACCEPT.headerOrFail(httpEntity)
            .testOrFail(requiredContentType);

        // the path contains a selector
        final String text = path.isRoot() ?
            "" :
            path.value()
                .substring(1);

        return HttpEntity.EMPTY.setContentType(
            requiredContentType.setCharset(CharsetName.UTF_8)
        ).addHeader(
            HateosResourceMappings.X_CONTENT_TYPE_NAME,
            SpreadsheetFormatterSelectorToken.class.getSimpleName()
        ).setBodyText(
            context.marshallOptional(
                    context.spreadsheetFormatterNextToken(
                        SpreadsheetFormatterSelector.parse(text)
                    )
                )
                .toString()
        ).setContentLength();
    }

    @Override
    public String toString() {
        return SpreadsheetFormatterProviderNextTokenHateosHttpEntityHandler.class.getSimpleName();
    }
}

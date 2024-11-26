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

import walkingkooka.collect.list.Lists;
import walkingkooka.net.header.CharsetName;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHttpEntityHandler;
import walkingkooka.net.http.server.hateos.HateosResourceMapping;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleMany;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleNone;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleOne;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleRange;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterName;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProviderSamplesContexts;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;

import java.util.Map;

/**
 * A handler that returns all available {@link SpreadsheetFormatterSelectorMenu}
 */
final class SpreadsheetFormatterMenuHateosHttpEntityHandler implements HateosHttpEntityHandler<SpreadsheetFormatterName, SpreadsheetEngineHateosResourceHandlerContext>,
        UnsupportedHateosHttpEntityHandlerHandleMany<SpreadsheetFormatterName, SpreadsheetEngineHateosResourceHandlerContext>,
        UnsupportedHateosHttpEntityHandlerHandleNone<SpreadsheetFormatterName, SpreadsheetEngineHateosResourceHandlerContext>,
        UnsupportedHateosHttpEntityHandlerHandleOne<SpreadsheetFormatterName, SpreadsheetEngineHateosResourceHandlerContext>,
        UnsupportedHateosHttpEntityHandlerHandleRange<SpreadsheetFormatterName, SpreadsheetEngineHateosResourceHandlerContext> {

    static {
        SpreadsheetFormatterSelectorMenuList.with(
                Lists.of(
                        SpreadsheetFormatterSelectorMenu.with(
                                "Label123",
                                SpreadsheetFormatterName.TEXT_FORMAT_PATTERN.setValueText("@")
                        )
                )
        ); // force json registry
    }

    static SpreadsheetFormatterMenuHateosHttpEntityHandler instance() {
        return INSTANCE;
    }

    /**
     * Singleton
     */
    private final static SpreadsheetFormatterMenuHateosHttpEntityHandler INSTANCE = new SpreadsheetFormatterMenuHateosHttpEntityHandler();

    private SpreadsheetFormatterMenuHateosHttpEntityHandler() {
    }

    @Override
    public HttpEntity handleAll(final HttpEntity httpEntity,
                                final Map<HttpRequestAttribute<?>, Object> parameters,
                                final SpreadsheetEngineHateosResourceHandlerContext context) {
        HateosHttpEntityHandler.checkHttpEntity(httpEntity);
        HateosHttpEntityHandler.checkParameters(parameters);
        HateosHttpEntityHandler.checkContext(context);

        final MediaType requiredContentType = context.contentType();
        HttpHeaderName.ACCEPT.headerOrFail(httpEntity)
                .testOrFail(requiredContentType);

        final SpreadsheetFormatterSelectorMenuList response = SpreadsheetFormatterSelectorMenu.prepare(
                SpreadsheetFormatterSelectorMenuContexts.basic(
                        context, // SpreadsheetFormatProvider
                        SpreadsheetFormatterProviderSamplesContexts.basic(
                                context.spreadsheetMetadata()
                                        .spreadsheetFormatterContext(
                                                context::now,
                                                context, // SpreadsheetLabelNameResolver
                                                context, // ConverterProvider
                                                context, // SpreadsheetFormatterProvider
                                                context // ProviderContext
                                        )
                        )
                )
        );

        // write TextNodes as JSON response
        return HttpEntity.EMPTY.setContentType(
                requiredContentType.setCharset(CharsetName.UTF_8)
        ).addHeader(
                HateosResourceMapping.X_CONTENT_TYPE_NAME,
                response.getClass().getSimpleName()
        ).setBodyText(
                context.marshall(response)
                        .toString()
        ).setContentLength();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}

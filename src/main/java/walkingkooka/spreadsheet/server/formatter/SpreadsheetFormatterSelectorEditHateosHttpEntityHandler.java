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
import walkingkooka.spreadsheet.format.SpreadsheetFormatterName;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelNameResolvers;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.tree.json.JsonNode;

import java.util.Map;

/**
 * A handler that accepts a request with a possible {@link walkingkooka.spreadsheet.format.SpreadsheetFormatterSelector} and returns a {@link SpreadsheetFormatterSelectorEdit}
 */
final class SpreadsheetFormatterSelectorEditHateosHttpEntityHandler implements HateosHttpEntityHandler<SpreadsheetFormatterName, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosHttpEntityHandlerHandleMany<SpreadsheetFormatterName, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosHttpEntityHandlerHandleNone<SpreadsheetFormatterName, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosHttpEntityHandlerHandleOne<SpreadsheetFormatterName, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosHttpEntityHandlerHandleRange<SpreadsheetFormatterName, SpreadsheetEngineHateosResourceHandlerContext> {

    static {
        try {
            SpreadsheetFormatterSelectorEdit.parse(
                "date-format-pattern yyyy",
                null
            ); // force json registry
        } catch (final NullPointerException ignore) {
            // dont care just want to force json registry
        }
    }

    static SpreadsheetFormatterSelectorEditHateosHttpEntityHandler instance() {
        return INSTANCE;
    }

    /**
     * Singleton
     */
    private final static SpreadsheetFormatterSelectorEditHateosHttpEntityHandler INSTANCE = new SpreadsheetFormatterSelectorEditHateosHttpEntityHandler();

    private SpreadsheetFormatterSelectorEditHateosHttpEntityHandler() {
    }

    @Override
    public HttpEntity handleAll(final HttpEntity httpEntity,
                                final Map<HttpRequestAttribute<?>, Object> parameters,
                                final UrlPath path,
                                final SpreadsheetEngineHateosResourceHandlerContext context) {
        HateosHttpEntityHandler.checkHttpEntity(httpEntity);
        HateosHttpEntityHandler.checkParameters(parameters);
        HateosHttpEntityHandler.checkPathEmpty(path);
        HateosHttpEntityHandler.checkContext(context);

        final MediaType requiredContentType = context.contentType();
        requiredContentType.requireContentType(
            HttpHeaderName.CONTENT_TYPE.header(httpEntity)
                .orElse(null)
        );

        HttpHeaderName.ACCEPT.headerOrFail(httpEntity)
            .testOrFail(requiredContentType);

        // read the string from the request holding the SpreadsheetFormatterSelector
        final String selector = context.unmarshall(
            JsonNode.parse(
                httpEntity.bodyText()
            ),
            String.class
        );

        final SpreadsheetFormatterSelectorEdit response = SpreadsheetFormatterSelectorEdit.parse(
            selector,
            SpreadsheetFormatterSelectorEditContexts.basic(
                context.spreadsheetMetadata()
                    .spreadsheetFormatterContext(
                        SpreadsheetMetadata.NO_CELL,
                        context::spreadsheetExpressionEvaluationContext,
                        SpreadsheetLabelNameResolvers.fake(),
                        context, // ConverterProvider
                        context, // // SpreadsheetFormatterProvider
                        context, // LocaleContext
                        context // ProviderContext
                    ),
                context, // SpreadsheetLabelNameResolver
                context // ProviderContext
            )
        );

        // write TextNodes as JSON response
        return HttpEntity.EMPTY.setContentType(
            requiredContentType.setCharset(CharsetName.UTF_8)
        ).addHeader(
            HateosResourceMappings.X_CONTENT_TYPE_NAME,
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

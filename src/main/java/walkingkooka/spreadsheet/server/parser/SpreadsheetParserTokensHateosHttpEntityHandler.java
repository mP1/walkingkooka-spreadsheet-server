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

import walkingkooka.collect.list.Lists;
import walkingkooka.net.UrlPath;
import walkingkooka.net.header.CharsetName;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHttpEntityHandler;
import walkingkooka.net.http.server.hateos.HateosResourceMappings;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleAll;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleMany;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleNone;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleRange;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.parser.SpreadsheetParserName;
import walkingkooka.spreadsheet.parser.SpreadsheetParserSelector;
import walkingkooka.spreadsheet.parser.SpreadsheetParserSelectorToken;
import walkingkooka.spreadsheet.parser.SpreadsheetParserSelectorTokenList;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.tree.json.JsonNode;

import java.util.List;
import java.util.Map;

/**
 * A handler that takes the {@link SpreadsheetParserName} and request body {@link String} to make a {@link SpreadsheetParserSelector} and then invokes {@link walkingkooka.spreadsheet.parser.SpreadsheetParserProvider#spreadsheetParser(SpreadsheetParserSelector, ProviderContext)}.
 */
final class SpreadsheetParserTokensHateosHttpEntityHandler implements HateosHttpEntityHandler<SpreadsheetParserName, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosHttpEntityHandlerHandleAll<SpreadsheetParserName, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosHttpEntityHandlerHandleMany<SpreadsheetParserName, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosHttpEntityHandlerHandleNone<SpreadsheetParserName, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosHttpEntityHandlerHandleRange<SpreadsheetParserName, SpreadsheetEngineHateosResourceHandlerContext> {

    static {
        SpreadsheetParserSelectorTokenList.with(Lists.empty()); // force json registry
    }

    static SpreadsheetParserTokensHateosHttpEntityHandler instance() {
        return INSTANCE;
    }

    private final static SpreadsheetParserTokensHateosHttpEntityHandler INSTANCE = new SpreadsheetParserTokensHateosHttpEntityHandler();

    private SpreadsheetParserTokensHateosHttpEntityHandler() {
    }

    @Override
    public HttpEntity handleOne(final SpreadsheetParserName formatterName,
                                final HttpEntity httpEntity,
                                final Map<HttpRequestAttribute<?>, Object> parameters,
                                final UrlPath path,
                                final SpreadsheetEngineHateosResourceHandlerContext context) {
        HateosHttpEntityHandler.checkId(formatterName);
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

        // read request body text
        final String text = context.unmarshall(
            JsonNode.parse(
                httpEntity.bodyText()
            ),
            String.class
        );

        // format all the individual requests
        final List<SpreadsheetParserSelectorToken> response = tokens(
            formatterName.setValueText(text), // selector
            context
        );

        return HttpEntity.EMPTY.setContentType(
            requiredContentType.setCharset(CharsetName.UTF_8)
        ).addHeader(
            HateosResourceMappings.X_CONTENT_TYPE_NAME,
            SpreadsheetParserSelectorTokenList.class.getSimpleName()
        ).setBodyText(
            context.marshall(
                SpreadsheetParserSelectorTokenList.with(response)
            ).toString()
        ).setContentLength();
    }

    private List<SpreadsheetParserSelectorToken> tokens(final SpreadsheetParserSelector selector,
                                                        final SpreadsheetEngineHateosResourceHandlerContext context) {
        return context.spreadsheetParser(
            selector,
            context
        ).tokens(
            context.spreadsheetMetadata()
                .spreadsheetParserContext(
                    SpreadsheetMetadata.NO_CELL,
                    context, // LocaleContext
                    context::now // now provider
                )
        );
    }

    @Override
    public String toString() {
        return SpreadsheetParserTokensHateosHttpEntityHandler.class.getSimpleName();
    }
}

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
import walkingkooka.net.header.CharsetName;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHttpEntityHandler;
import walkingkooka.net.http.server.hateos.HateosResourceMapping;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleAll;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleMany;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleNone;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleRange;
import walkingkooka.spreadsheet.parser.SpreadsheetParserName;
import walkingkooka.spreadsheet.parser.SpreadsheetParserSelector;
import walkingkooka.spreadsheet.parser.SpreadsheetParserSelectorToken;
import walkingkooka.spreadsheet.parser.SpreadsheetParserSelectorTokenList;
import walkingkooka.spreadsheet.server.SpreadsheetHateosResourceHandlerContext;
import walkingkooka.tree.json.JsonNode;

import java.util.Map;
import java.util.Optional;

/**
 * A handler that takes the {@link SpreadsheetParserName} and request body {@link String} to make a {@link SpreadsheetParserSelector} and then invokes {@link walkingkooka.spreadsheet.parser.SpreadsheetParserProvider#spreadsheetParserNextToken(SpreadsheetParserSelector)}.
 */
final class SpreadsheetParserProviderNextTokenHateosHttpEntityHandler implements HateosHttpEntityHandler<SpreadsheetParserName, SpreadsheetHateosResourceHandlerContext>,
        UnsupportedHateosHttpEntityHandlerHandleAll<SpreadsheetParserName, SpreadsheetHateosResourceHandlerContext>,
        UnsupportedHateosHttpEntityHandlerHandleMany<SpreadsheetParserName, SpreadsheetHateosResourceHandlerContext>,
        UnsupportedHateosHttpEntityHandlerHandleNone<SpreadsheetParserName, SpreadsheetHateosResourceHandlerContext>,
        UnsupportedHateosHttpEntityHandlerHandleRange<SpreadsheetParserName, SpreadsheetHateosResourceHandlerContext> {

    static {
        SpreadsheetParserSelectorTokenList.with(Lists.empty()); // force json registry
    }

    static SpreadsheetParserProviderNextTokenHateosHttpEntityHandler instance() {
        return INSTANCE;
    }

    private final static SpreadsheetParserProviderNextTokenHateosHttpEntityHandler INSTANCE = new SpreadsheetParserProviderNextTokenHateosHttpEntityHandler();

    private SpreadsheetParserProviderNextTokenHateosHttpEntityHandler() {
    }

    @Override
    public HttpEntity handleOne(final SpreadsheetParserName formatterName,
                                final HttpEntity httpEntity,
                                final Map<HttpRequestAttribute<?>, Object> parameters,
                                final SpreadsheetHateosResourceHandlerContext context) {
        HateosHttpEntityHandler.checkId(formatterName);
        HateosHttpEntityHandler.checkHttpEntity(httpEntity);
        HateosHttpEntityHandler.checkParameters(parameters);
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
        final Optional<SpreadsheetParserSelectorToken> response = nextTextComponent(
                formatterName.setText(text), // selector
                context
        );

        return HttpEntity.EMPTY.setContentType(
                requiredContentType.setCharset(CharsetName.UTF_8)
        ).addHeader(
                HateosResourceMapping.X_CONTENT_TYPE_NAME,
                SpreadsheetParserSelectorToken.class.getSimpleName()
        ).setBodyText(
                response.map(r -> context.marshall(r).toString())
                        .orElse("")
        ).setContentLength();
    }

    private Optional<SpreadsheetParserSelectorToken> nextTextComponent(final SpreadsheetParserSelector selector,
                                                                       final SpreadsheetHateosResourceHandlerContext context) {
        return context.spreadsheetParserNextToken(
                selector
        );
    }

    @Override
    public String toString() {
        return SpreadsheetParserProviderNextTokenHateosHttpEntityHandler.class.getSimpleName();
    }
}

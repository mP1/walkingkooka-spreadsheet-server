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
import walkingkooka.spreadsheet.parser.SpreadsheetParserName;
import walkingkooka.spreadsheet.parser.SpreadsheetParserSelector;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelNameResolvers;
import walkingkooka.spreadsheet.server.engine.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.tree.json.JsonNode;

import java.util.Map;

/**
 * A handler that accepts a request with a possible {@link SpreadsheetParserSelector} and returns a {@link SpreadsheetParserSelectorEdit}
 */
final class SpreadsheetParserEditHateosHttpEntityHandler implements HateosHttpEntityHandler<SpreadsheetParserName, SpreadsheetEngineHateosResourceHandlerContext>,
        UnsupportedHateosHttpEntityHandlerHandleMany<SpreadsheetParserName, SpreadsheetEngineHateosResourceHandlerContext>,
        UnsupportedHateosHttpEntityHandlerHandleNone<SpreadsheetParserName, SpreadsheetEngineHateosResourceHandlerContext>,
        UnsupportedHateosHttpEntityHandlerHandleOne<SpreadsheetParserName, SpreadsheetEngineHateosResourceHandlerContext>,
        UnsupportedHateosHttpEntityHandlerHandleRange<SpreadsheetParserName, SpreadsheetEngineHateosResourceHandlerContext> {

    static {
        try {
            SpreadsheetParserSelectorEdit.parse(
                    SpreadsheetParserName.DATE_PARSER_PATTERN + " yyyy",
                    null
            ); // force json registry
        } catch (final NullPointerException ignore) {
            // nop
        }
    }

    static SpreadsheetParserEditHateosHttpEntityHandler instance() {
        return INSTANCE;
    }

    /**
     * Singleton
     */
    private final static SpreadsheetParserEditHateosHttpEntityHandler INSTANCE = new SpreadsheetParserEditHateosHttpEntityHandler();

    private SpreadsheetParserEditHateosHttpEntityHandler() {
    }

    @Override
    public HttpEntity handleAll(final HttpEntity httpEntity,
                                final Map<HttpRequestAttribute<?>, Object> parameters,
                                final SpreadsheetEngineHateosResourceHandlerContext context) {
        HateosHttpEntityHandler.checkHttpEntity(httpEntity);
        HateosHttpEntityHandler.checkParameters(parameters);
        HateosHttpEntityHandler.checkContext(context);

        final MediaType requiredContentType = context.contentType();
        final MediaType contentType = HttpHeaderName.CONTENT_TYPE.headerOrFail(httpEntity);
        if (false == requiredContentType.equalsIgnoringParameters(contentType)) {
            throw new IllegalArgumentException("Got " + contentType + " expected " + requiredContentType);
        }

        final String selector = context.unmarshall(
                JsonNode.parse(
                        httpEntity.bodyText()
                ),
                String.class
        );

        final SpreadsheetParserSelectorEdit response = SpreadsheetParserSelectorEdit.parse(
                selector,
                SpreadsheetParserSelectorEditContexts.basic(
                        context, // SpreadsheetParserProvider
                        context.spreadsheetMetadata().parserContext(context::now), // SpreadsheetParserContext,
                        context.spreadsheetMetadata().formatterContext(
                                context, // ConverterProvider
                                context, // // SpreadsheetFormatterProvider
                                context::now,
                                SpreadsheetLabelNameResolvers.fake(),
                                context // ProviderContext
                        ),
                        context, // SpreadsheetFormatterProvider
                        context // ProviderContext
                )
        );

        // write TextNodes as JSON response
        return HttpEntity.EMPTY.addHeader(
                HttpHeaderName.CONTENT_TYPE,
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

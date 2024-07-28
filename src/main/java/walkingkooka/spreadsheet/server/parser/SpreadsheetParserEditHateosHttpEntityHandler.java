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
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContexts;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterName;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProvider;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProviders;
import walkingkooka.spreadsheet.parser.SpreadsheetParserContexts;
import walkingkooka.spreadsheet.parser.SpreadsheetParserName;
import walkingkooka.spreadsheet.parser.SpreadsheetParserProviders;
import walkingkooka.spreadsheet.parser.SpreadsheetParserSelector;
import walkingkooka.spreadsheet.parser.edit.SpreadsheetParserSelectorEdit;
import walkingkooka.spreadsheet.parser.edit.SpreadsheetParserSelectorEditContexts;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelNameResolvers;
import walkingkooka.spreadsheet.server.engine.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.tree.json.JsonNode;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;

/**
 * A handler that accepts a request with a possible {@link SpreadsheetParserSelector} and returns a {@link SpreadsheetParserSelectorEdit}
 */
final class SpreadsheetParserEditHateosHttpEntityHandler implements HateosHttpEntityHandler<SpreadsheetFormatterName, SpreadsheetEngineHateosResourceHandlerContext>,
        UnsupportedHateosHttpEntityHandlerHandleMany<SpreadsheetFormatterName, SpreadsheetEngineHateosResourceHandlerContext>,
        UnsupportedHateosHttpEntityHandlerHandleNone<SpreadsheetFormatterName, SpreadsheetEngineHateosResourceHandlerContext>,
        UnsupportedHateosHttpEntityHandlerHandleOne<SpreadsheetFormatterName, SpreadsheetEngineHateosResourceHandlerContext>,
        UnsupportedHateosHttpEntityHandlerHandleRange<SpreadsheetFormatterName, SpreadsheetEngineHateosResourceHandlerContext> {

    static {
        final SpreadsheetFormatterProvider spreadsheetFormatterProvider = SpreadsheetFormatterProviders.spreadsheetFormatPattern(
                Locale.forLanguageTag("EN-AU"),
                LocalDateTime::now
        );

        SpreadsheetParserSelectorEdit.parse(
                SpreadsheetParserName.DATE_PARSER_PATTERN + " yyyy",
                SpreadsheetParserSelectorEditContexts.basic(
                        SpreadsheetParserProviders.spreadsheetParsePattern(spreadsheetFormatterProvider),
                        SpreadsheetParserContexts.fake(),
                        SpreadsheetFormatterContexts.fake(),
                        spreadsheetFormatterProvider
                )
        ); // force json registry
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
                                SpreadsheetLabelNameResolvers.fake()
                        ),
                        context // SpreadsheetFormatterProvider
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

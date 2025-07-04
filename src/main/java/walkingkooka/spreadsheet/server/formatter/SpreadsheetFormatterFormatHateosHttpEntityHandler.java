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
import walkingkooka.spreadsheet.formula.SpreadsheetFormula;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.text.TextNode;
import walkingkooka.tree.text.TextNodeList;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A handler that accepts a request with {@link SpreadsheetFormatterFormatRequestList} and proceeds to format each value with the given formatter.
 */
final class SpreadsheetFormatterFormatHateosHttpEntityHandler implements HateosHttpEntityHandler<SpreadsheetFormatterName, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosHttpEntityHandlerHandleMany<SpreadsheetFormatterName, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosHttpEntityHandlerHandleNone<SpreadsheetFormatterName, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosHttpEntityHandlerHandleOne<SpreadsheetFormatterName, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosHttpEntityHandlerHandleRange<SpreadsheetFormatterName, SpreadsheetEngineHateosResourceHandlerContext> {

    static {
        SpreadsheetFormatterFormatRequestList.with(Lists.empty()); // force json registry
    }

    static SpreadsheetFormatterFormatHateosHttpEntityHandler instance() {
        return INSTANCE;
    }

    private final static SpreadsheetFormatterFormatHateosHttpEntityHandler INSTANCE = new SpreadsheetFormatterFormatHateosHttpEntityHandler();

    private SpreadsheetFormatterFormatHateosHttpEntityHandler() {
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

        // read json into SpreadsheetFormatterFormatRequestList
        final SpreadsheetFormatterFormatRequestList requests = context.unmarshall(
            JsonNode.parse(
                httpEntity.bodyText()
            ),
            SpreadsheetFormatterFormatRequestList.class
        );

        // format all the individual requests
        final List<TextNode> response = requests.stream()
            .map(r -> this.format(r, context))
            .collect(Collectors.toList());

        // write TextNodes as JSON response
        return HttpEntity.EMPTY.setContentType(
            requiredContentType.setCharset(CharsetName.UTF_8)
        ).addHeader(
            HateosResourceMappings.X_CONTENT_TYPE_NAME,
            TextNodeList.class.getSimpleName()
        ).setBodyText(
            context.marshall(response)
                .toString()
        ).setContentLength();
    }

    private TextNode format(final SpreadsheetFormatterFormatRequest<?> request,
                            final SpreadsheetEngineHateosResourceHandlerContext context) {
        return context.formatValue(
            SpreadsheetSelection.A1.setFormula(SpreadsheetFormula.EMPTY),
            Optional.ofNullable(
                request.value()
            ),
            Optional.of(
                request.selector()
            )
        ).orElse(TextNode.EMPTY_TEXT);
    }

    @Override
    public String toString() {
        return SpreadsheetFormatterFormatHateosHttpEntityHandler.class.getSimpleName();
    }
}

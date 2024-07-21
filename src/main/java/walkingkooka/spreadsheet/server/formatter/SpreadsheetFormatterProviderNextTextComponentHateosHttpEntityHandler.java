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
import walkingkooka.spreadsheet.format.SpreadsheetFormatterName;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSelector;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSelectorTextComponent;
import walkingkooka.spreadsheet.server.engine.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.tree.json.JsonNode;

import java.util.Map;
import java.util.Optional;

/**
 * A handler that takes the {@link SpreadsheetFormatterName} and request body {@link String} to make a {@link SpreadsheetFormatterSelector} and then invokes {@link walkingkooka.spreadsheet.format.SpreadsheetFormatterProvider#spreadsheetFormatter(SpreadsheetFormatterSelector)}.
 */
final class SpreadsheetFormatterProviderNextTextComponentHateosHttpEntityHandler implements HateosHttpEntityHandler<SpreadsheetFormatterName, SpreadsheetEngineHateosResourceHandlerContext>,
        UnsupportedHateosHttpEntityHandlerHandleAll<SpreadsheetFormatterName, SpreadsheetEngineHateosResourceHandlerContext>,
        UnsupportedHateosHttpEntityHandlerHandleMany<SpreadsheetFormatterName, SpreadsheetEngineHateosResourceHandlerContext>,
        UnsupportedHateosHttpEntityHandlerHandleNone<SpreadsheetFormatterName, SpreadsheetEngineHateosResourceHandlerContext>,
        UnsupportedHateosHttpEntityHandlerHandleRange<SpreadsheetFormatterName, SpreadsheetEngineHateosResourceHandlerContext> {

    static SpreadsheetFormatterProviderNextTextComponentHateosHttpEntityHandler instance() {
        return INSTANCE;
    }

    private final static SpreadsheetFormatterProviderNextTextComponentHateosHttpEntityHandler INSTANCE = new SpreadsheetFormatterProviderNextTextComponentHateosHttpEntityHandler();

    private SpreadsheetFormatterProviderNextTextComponentHateosHttpEntityHandler() {
    }

    @Override
    public HttpEntity handleOne(final SpreadsheetFormatterName formatterName,
                                final HttpEntity httpEntity,
                                final Map<HttpRequestAttribute<?>, Object> parameters,
                                final SpreadsheetEngineHateosResourceHandlerContext context) {
        HateosHttpEntityHandler.checkId(formatterName);
        HateosHttpEntityHandler.checkHttpEntity(httpEntity);
        HateosHttpEntityHandler.checkParameters(parameters);
        HateosHttpEntityHandler.checkContext(context);

        final MediaType requiredContentType = context.contentType();
        final MediaType contentType = HttpHeaderName.CONTENT_TYPE.headerOrFail(httpEntity);
        if (false == requiredContentType.equalsIgnoringParameters(contentType)) {
            throw new IllegalArgumentException("Got " + contentType + " expected " + requiredContentType);
        }

        // read request body text
        final String text = context.unmarshall(
                JsonNode.parse(
                        httpEntity.bodyText()
                ),
                String.class
        );

        // format all the individual requests
        final Optional<SpreadsheetFormatterSelectorTextComponent> response = nextTextComponent(
                formatterName.setText(text), // selector
                context
        );

        return HttpEntity.EMPTY.addHeader(
                HttpHeaderName.CONTENT_TYPE,
                requiredContentType.setCharset(CharsetName.UTF_8)
        ).addHeader(
                HateosResourceMapping.X_CONTENT_TYPE_NAME,
                SpreadsheetFormatterSelectorTextComponent.class.getSimpleName()
        ).setBodyText(
                context.marshall(
                        response
                ).toString()
        ).setContentLength();
    }

    private Optional<SpreadsheetFormatterSelectorTextComponent> nextTextComponent(final SpreadsheetFormatterSelector selector,
                                                                                  final SpreadsheetEngineHateosResourceHandlerContext context) {
        return context.spreadsheetFormatterNextTextComponent(
                selector
        );
    }

    @Override
    public String toString() {
        return SpreadsheetFormatterProviderNextTextComponentHateosHttpEntityHandler.class.getSimpleName();
    }
}

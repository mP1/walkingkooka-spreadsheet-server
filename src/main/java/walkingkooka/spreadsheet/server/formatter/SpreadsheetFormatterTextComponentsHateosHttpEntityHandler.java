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
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleAll;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleMany;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleNone;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleRange;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterName;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSelector;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSelectorTextComponent;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSelectorTextComponentList;
import walkingkooka.spreadsheet.server.engine.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.tree.json.JsonNode;

import java.util.List;
import java.util.Map;

/**
 * A handler that takes the {@link SpreadsheetFormatterName} and request body {@link String} to make a {@link walkingkooka.spreadsheet.format.SpreadsheetFormatterSelector} and then invokes {@link walkingkooka.spreadsheet.format.SpreadsheetFormatterProvider#spreadsheetFormatter(SpreadsheetFormatterSelector)}.
 */
final class SpreadsheetFormatterTextComponentsHateosHttpEntityHandler implements HateosHttpEntityHandler<SpreadsheetFormatterName, SpreadsheetEngineHateosResourceHandlerContext>,
        UnsupportedHateosHttpEntityHandlerHandleAll<SpreadsheetFormatterName, SpreadsheetEngineHateosResourceHandlerContext>,
        UnsupportedHateosHttpEntityHandlerHandleMany<SpreadsheetFormatterName, SpreadsheetEngineHateosResourceHandlerContext>,
        UnsupportedHateosHttpEntityHandlerHandleNone<SpreadsheetFormatterName, SpreadsheetEngineHateosResourceHandlerContext>,
        UnsupportedHateosHttpEntityHandlerHandleRange<SpreadsheetFormatterName, SpreadsheetEngineHateosResourceHandlerContext> {

    static {
        SpreadsheetFormatterSelectorTextComponentList.with(Lists.empty()); // force json registry
    }

    static SpreadsheetFormatterTextComponentsHateosHttpEntityHandler instance() {
        return INSTANCE;
    }

    private final static SpreadsheetFormatterTextComponentsHateosHttpEntityHandler INSTANCE = new SpreadsheetFormatterTextComponentsHateosHttpEntityHandler();

    private SpreadsheetFormatterTextComponentsHateosHttpEntityHandler() {
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
        requiredContentType.testOrFail(
                HttpHeaderName.CONTENT_TYPE.headerOrFail(httpEntity)
        );

        // read request body text
        final String text = context.unmarshall(
                JsonNode.parse(
                        httpEntity.bodyText()
                ),
                String.class
        );

        // format all the individual requests
        final List<SpreadsheetFormatterSelectorTextComponent> response = textComponents(
                formatterName.setText(text), // selector
                context
        );

        return HttpEntity.EMPTY.setContentType(
                requiredContentType.setCharset(CharsetName.UTF_8)
        ).addHeader(
                HateosResourceMapping.X_CONTENT_TYPE_NAME,
                SpreadsheetFormatterSelectorTextComponentList.class.getSimpleName()
        ).setBodyText(
                context.marshall(
                        SpreadsheetFormatterSelectorTextComponentList.with(response)
                ).toString()
        ).setContentLength();
    }

    private List<SpreadsheetFormatterSelectorTextComponent> textComponents(final SpreadsheetFormatterSelector selector,
                                                                           final SpreadsheetEngineHateosResourceHandlerContext context) {
        return context.spreadsheetFormatter(
                selector,
                context
        ).textComponents(
                context.spreadsheetMetadata()
                        .formatterContext(
                                context, // ConverterProvider
                                context, // SpreadsheetFormatterProvider
                                context::now, // now provider
                                context, // SpreadsheetLabelNameResolver
                                context // ProviderContext
                        )
        );
    }

    @Override
    public String toString() {
        return SpreadsheetFormatterTextComponentsHateosHttpEntityHandler.class.getSimpleName();
    }
}

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

package walkingkooka.spreadsheet.server.convert;

import walkingkooka.Cast;
import walkingkooka.collect.list.Lists;
import walkingkooka.convert.Converter;
import walkingkooka.convert.provider.ConverterName;
import walkingkooka.convert.provider.ConverterSelector;
import walkingkooka.net.UrlPath;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHttpEntityHandler;
import walkingkooka.net.http.server.hateos.HateosResourceMappings;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleMany;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleNone;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleOne;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleRange;
import walkingkooka.spreadsheet.convert.MissingConverter;
import walkingkooka.spreadsheet.convert.MissingConverterSet;
import walkingkooka.spreadsheet.convert.SpreadsheetConverterContext;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetServerMediaTypes;
import walkingkooka.tree.json.JsonNode;

import java.util.Map;

/**
 * A handler which eventually calls {@link MissingConverter#verify(Converter, SpreadsheetMetadataPropertyName, SpreadsheetConverterContext)}.
 */
final class ConverterSelectorVerifyHateosHttpEntityHandler implements HateosHttpEntityHandler<ConverterName, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosHttpEntityHandlerHandleOne<ConverterName, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosHttpEntityHandlerHandleMany<ConverterName, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosHttpEntityHandlerHandleNone<ConverterName, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosHttpEntityHandlerHandleRange<ConverterName, SpreadsheetEngineHateosResourceHandlerContext> {

    /**
     * Singleton
     */
    final static ConverterSelectorVerifyHateosHttpEntityHandler INSTANCE = new ConverterSelectorVerifyHateosHttpEntityHandler();

    private ConverterSelectorVerifyHateosHttpEntityHandler() {
        super();
    }

    // POST /api/spreadsheet/SpreadsheetId/converter/*/findConverter
    //
    // BODY = ConverterSelector
    @Override
    public HttpEntity handleAll(final HttpEntity entity,
                                final Map<HttpRequestAttribute<?>, Object> parameters,
                                final UrlPath path,
                                final SpreadsheetEngineHateosResourceHandlerContext context) {
        HateosHttpEntityHandler.checkHttpEntity(entity);
        HateosHttpEntityHandler.checkParameters(parameters);

        final SpreadsheetMetadataPropertyName<?> propertyName = propertyName(
            HateosHttpEntityHandler.checkPath(path)
        );
        HateosHttpEntityHandler.checkContext(context);

        if (false == propertyName.isConverterSelector()) {
            throw new IllegalArgumentException("Not a converter selector");
        }
        final ConverterSelector converterSelector = context.unmarshall(
            JsonNode.parse(
                entity.bodyText()
            ),
            ConverterSelector.class
        );

        final MissingConverterSet response = MissingConverterSet.with(
            MissingConverter.verify(
                context.converter(
                    converterSelector,
                    context // ProviderContext
                ),
                Cast.to(propertyName),
                context // SpreadsheetConverterContext
            )
        );

        return HttpEntity.EMPTY.setContentType(
            SpreadsheetServerMediaTypes.CONTENT_TYPE
        ).setHeader(
            HateosResourceMappings.X_CONTENT_TYPE_NAME,
            Lists.of(
                MissingConverterSet.class.getSimpleName()
            )
        ).setBodyText(
            context.marshall(response)
                .toString()
        ).setContentLength();
    }

    // POST /api/spreadsheet/SpreadsheetId/converter/*/findConverter
    //      1 2  3           4             5         6 7
    static SpreadsheetMetadataPropertyName<?> propertyName(final UrlPath path) {
        return SpreadsheetMetadataPropertyName.with(
            path.name()
                .value()
        );
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}

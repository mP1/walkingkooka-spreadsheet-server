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

package walkingkooka.spreadsheet.server.meta;

import walkingkooka.Cast;
import walkingkooka.collect.list.Lists;
import walkingkooka.convert.Converter;
import walkingkooka.convert.provider.ConverterSelector;
import walkingkooka.net.UrlPath;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHttpEntityHandler;
import walkingkooka.net.http.server.hateos.HateosResourceMappings;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleAll;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleMany;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleNone;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleRange;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.spreadsheet.convert.SpreadsheetConverterContext;
import walkingkooka.spreadsheet.convert.provider.MissingConverter;
import walkingkooka.spreadsheet.convert.provider.MissingConverterSet;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelNameResolvers;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetServerMediaTypes;
import walkingkooka.tree.json.JsonNode;

import java.util.Map;
import java.util.Objects;

/**
 * A handler which eventually calls {@link MissingConverter#verify(Converter, SpreadsheetMetadataPropertyName, SpreadsheetConverterContext)}.
 */
final class ConverterSelectorVerifyHateosHttpEntityHandler implements HateosHttpEntityHandler<SpreadsheetMetadataPropertyName<?>, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosHttpEntityHandlerHandleAll<SpreadsheetMetadataPropertyName<?>, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosHttpEntityHandlerHandleMany<SpreadsheetMetadataPropertyName<?>, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosHttpEntityHandlerHandleNone<SpreadsheetMetadataPropertyName<?>, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosHttpEntityHandlerHandleRange<SpreadsheetMetadataPropertyName<?>, SpreadsheetEngineHateosResourceHandlerContext> {

    /**
     * Singleton
     */
    final static ConverterSelectorVerifyHateosHttpEntityHandler INSTANCE = new ConverterSelectorVerifyHateosHttpEntityHandler();

    private ConverterSelectorVerifyHateosHttpEntityHandler() {
        super();
    }

    // POST /api/spreadsheet/SpreadsheetId/metadata/findConverter/verify
    //
    // BODY = ConverterSelector
    @Override
    public HttpEntity handleOne(final SpreadsheetMetadataPropertyName<?> propertyName,
                                final HttpEntity entity,
                                final Map<HttpRequestAttribute<?>, Object> parameters,
                                final UrlPath path,
                                final SpreadsheetEngineHateosResourceHandlerContext context) {
        Objects.requireNonNull(propertyName, "propertyName");
        HateosHttpEntityHandler.checkHttpEntity(entity);
        HateosHttpEntityHandler.checkParameters(parameters);
        HateosHttpEntityHandler.checkPathEmpty(path);
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

        final SpreadsheetMetadataPropertyName<ConverterSelector> converterSelectorSpreadsheetMetadataPropertyName = propertyName.toConverterSelector();
        final ProviderContext providerContext = context.providerContext();

        final MissingConverterSet response = MissingConverterSet.with(
            MissingConverter.verify(
                context.converter(
                    converterSelector,
                    providerContext
                ),
                converterSelectorSpreadsheetMetadataPropertyName,
                context.spreadsheetMetadata()
                    .spreadsheetConverterContext(
                        SpreadsheetMetadata.NO_CELL,
                        SpreadsheetMetadata.NO_VALIDATION_REFERENCE,
                        Cast.to(propertyName),
                        SpreadsheetLabelNameResolvers.empty(),
                        context, // ConverterProvider
                        context, // LocaleContext
                        providerContext // ProviderContext
                    ) // SpreadsheetConverterContext
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
            context.toJsonText(
                context.marshall(response)
            )
        ).setContentLength();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}

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

import walkingkooka.net.UrlPath;
import walkingkooka.net.header.AcceptLanguage;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleAll;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleMany;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleRange;
import walkingkooka.spreadsheet.SpreadsheetContext;
import walkingkooka.spreadsheet.meta.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * A {@link HateosResourceHandler} that invokes {@link walkingkooka.spreadsheet.server.SpreadsheetServerContext}
 * methods to create, or save/update.
 */
final class SpreadsheetMetadataHateosResourceHandlerCreateOrSaveOrUpdate extends SpreadsheetMetadataHateosResourceHandler
    implements UnsupportedHateosResourceHandlerHandleAll<SpreadsheetId, SpreadsheetMetadata, SpreadsheetMetadataSet, SpreadsheetMetadataHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleMany<SpreadsheetId, SpreadsheetMetadata, SpreadsheetMetadataSet, SpreadsheetMetadataHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleRange<SpreadsheetId, SpreadsheetMetadata, SpreadsheetMetadataSet, SpreadsheetMetadataHateosResourceHandlerContext> {

    final static SpreadsheetMetadataHateosResourceHandlerCreateOrSaveOrUpdate INSTANCE = new SpreadsheetMetadataHateosResourceHandlerCreateOrSaveOrUpdate();

    private SpreadsheetMetadataHateosResourceHandlerCreateOrSaveOrUpdate() {
    }

    @Override
    public Optional<SpreadsheetMetadata> handleOne(final SpreadsheetId id,
                                                   final Optional<SpreadsheetMetadata> resource,
                                                   final Map<HttpRequestAttribute<?>, Object> parameters,
                                                   final UrlPath path,
                                                   final SpreadsheetMetadataHateosResourceHandlerContext context) {
        HateosResourceHandler.checkId(id);
        final SpreadsheetMetadata metadata = HateosResourceHandler.checkResourceNotEmpty(resource);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkPathEmpty(path);
        HateosResourceHandler.checkContext(context);

        final SpreadsheetId metadataId = metadata.getOrFail(SpreadsheetMetadataPropertyName.SPREADSHEET_ID);
        if (false == id.equals(metadataId)) {
            throw new IllegalArgumentException("Resource id " + id + " does not match metadata id" + metadataId);
        }

        return Optional.of(
            context.spreadsheetContextOrFail(metadataId)
                .spreadsheetEngineContext()
                .saveMetadata(metadata)
        );
    }

    @Override
    public Optional<SpreadsheetMetadata> handleNone(final Optional<SpreadsheetMetadata> resource,
                                                    final Map<HttpRequestAttribute<?>, Object> parameters,
                                                    final UrlPath path,
                                                    final SpreadsheetMetadataHateosResourceHandlerContext context) {
        HateosResourceHandler.checkResourceEmpty(resource);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkPathEmpty(path);
        HateosResourceHandler.checkContext(context);

        final SpreadsheetContext spreadsheetContext = context.createEmptySpreadsheet(
            HttpHeaderName.ACCEPT_LANGUAGE.parameterValue(parameters)
                .flatMap(this::preferredLocale)
        );

        return Optional.of(
            spreadsheetContext.spreadsheetMetadata()
        );
    }

    private Optional<Locale> preferredLocale(final AcceptLanguage language) {
        return language.value()
            .get(0)
            .value()
            .locale();
    }

    @Override
    String operation() {
        return "createSaveUpdateMetadata";
    }
}

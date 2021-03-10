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
package walkingkooka.spreadsheet.server.context.hateos;

import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.hateos.HateosContentType;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.server.context.SpreadsheetContext;

import java.util.function.BiConsumer;

/**
 * A collection of factory methods to create various {@link HateosHandler}.
 */
public final class SpreadsheetContextHateosHandlers implements PublicStaticHelper {

    /**
     * {@see SpreadsheetContextHateosHandlerMetadataSaveOrUpdate}
     */
    public static HateosHandler<SpreadsheetId, SpreadsheetMetadata, SpreadsheetMetadata> createAndSaveMetadata(final SpreadsheetContext context) {
        return SpreadsheetContextHateosHandlerMetadataSaveOrUpdate.with(context);
    }

    /**
     * {@see SpreadsheetContextHateosHandlerMetadataLoad}
     */
    public static HateosHandler<SpreadsheetId, SpreadsheetMetadata, SpreadsheetMetadata> loadMetadata(final SpreadsheetContext context) {
        return SpreadsheetContextHateosHandlerMetadataLoad.with(context);
    }

    /**
     * {@see SpreadsheetContextHateosHandlersRouter}
     */
    public static Router<HttpRequestAttribute<?>, BiConsumer<HttpRequest, HttpResponse>> router(final AbsoluteUrl baseUrl,
                                                                                                final HateosContentType contentType,
                                                                                                final HateosHandler<SpreadsheetId, SpreadsheetMetadata, SpreadsheetMetadata> createAndSaveMetadata,
                                                                                                final HateosHandler<SpreadsheetId, SpreadsheetMetadata, SpreadsheetMetadata> loadMetadata) {
        return SpreadsheetContextHateosHandlersRouter.with(baseUrl,
                contentType,
                createAndSaveMetadata,
                loadMetadata);
    }

    /**
     * Stop creation.
     */
    private SpreadsheetContextHateosHandlers() {
        throw new UnsupportedOperationException();
    }
}

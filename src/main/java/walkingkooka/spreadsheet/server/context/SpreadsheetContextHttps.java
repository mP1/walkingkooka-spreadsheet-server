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
package walkingkooka.spreadsheet.server.context;

import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosContentType;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;
import walkingkooka.tree.json.JsonNode;

import java.util.function.UnaryOperator;

/**
 * A collection of factory methods to create various {@link HateosResourceHandler}, {@link Router} etc.
 */
public final class SpreadsheetContextHttps implements PublicStaticHelper {

    /**
     * {@see SpreadsheetContextHateosResourceHandlerMetadataDelete}
     */
    public static HateosResourceHandler<SpreadsheetId, SpreadsheetMetadata, SpreadsheetMetadataSet> deleteMetadata(final SpreadsheetContext context) {
        return SpreadsheetContextHateosResourceHandlerMetadataDelete.with(context);
    }

    /**
     * {@see SpreadsheetContextHateosResourceHandlerMetadataSaveOrUpdate}
     */
    public static HateosResourceHandler<SpreadsheetId, SpreadsheetMetadata, SpreadsheetMetadataSet> saveOrUpdateMetadata(final SpreadsheetContext context) {
        return SpreadsheetContextHateosResourceHandlerMetadataSaveOrUpdate.with(context);
    }

    /**
     * {@see SpreadsheetContextHateosResourceHandlerMetadataLoad}
     */
    public static HateosResourceHandler<SpreadsheetId, SpreadsheetMetadata, SpreadsheetMetadataSet> loadMetadata(final SpreadsheetContext context) {
        return SpreadsheetContextHateosResourceHandlerMetadataLoad.with(context);
    }

    /**
     * {@see SpreadsheetContextMetadataPatchFunction}
     */
    public static UnaryOperator<JsonNode> patch(final SpreadsheetId id,
                                                final SpreadsheetContext context) {
        return SpreadsheetContextMetadataPatchFunction.with(id, context);
    }

    /**
     * {@see SpreadsheetContextHateosResourceHandlersRouter}
     */
    public static Router<HttpRequestAttribute<?>, HttpHandler> router(final AbsoluteUrl baseUrl,
                                                                      final HateosContentType contentType,
                                                                      final Indentation indentation,
                                                                      final LineEnding lineEnding,
                                                                      final HateosResourceHandler<SpreadsheetId, SpreadsheetMetadata, SpreadsheetMetadataSet> createAndSaveMetadata,
                                                                      final HateosResourceHandler<SpreadsheetId, SpreadsheetMetadata, SpreadsheetMetadataSet> deleteMetadata,
                                                                      final HateosResourceHandler<SpreadsheetId, SpreadsheetMetadata, SpreadsheetMetadataSet> loadMetadata) {
        return SpreadsheetContextHateosResourceHandlersRouter.with(
                baseUrl,
                contentType,
                indentation,
                lineEnding,
                createAndSaveMetadata,
                deleteMetadata,
                loadMetadata
        );
    }

    /**
     * Stop creation.
     */
    private SpreadsheetContextHttps() {
        throw new UnsupportedOperationException();
    }
}

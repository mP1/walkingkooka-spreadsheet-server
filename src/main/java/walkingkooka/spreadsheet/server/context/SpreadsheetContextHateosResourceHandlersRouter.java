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

import walkingkooka.collect.set.Sets;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.header.LinkRelation;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosContentType;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.net.http.server.hateos.HateosResourceHandlers;
import walkingkooka.net.http.server.hateos.HateosResourceMapping;
import walkingkooka.net.http.server.hateos.HateosResourceName;
import walkingkooka.net.http.server.hateos.HateosResourceSelection;
import walkingkooka.reflect.StaticHelper;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.text.CharSequences;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;

import java.util.Objects;

/**
 * A collection of factory methods to create various {@link HateosResourceHandler}.
 */
final class SpreadsheetContextHateosResourceHandlersRouter implements StaticHelper {

    static {
        // force static initializers for JsonContext
        SpreadsheetMetadata.EMPTY.isEmpty();
    }

    /**
     * A {@link HateosResourceName} with <code>metadata</code>.
     */
    private final static HateosResourceName SPREADSHEET = HateosResourceName.with("spreadsheet");

    /**
     * Used to form the metadata load and save services
     * <pre>
     * /api/spreadsheet/$spreadsheet-id
     * </pre>
     */
    private final static LinkRelation<?> METADATA_LINK_RELATION = LinkRelation.SELF;

    /**
     * Builds a {@link Router} that handles all operations, using the given {@link HateosResourceHandler handlers}.
     */
    static Router<HttpRequestAttribute<?>, HttpHandler> with(final AbsoluteUrl baseUrl,
                                                             final HateosContentType contentType,
                                                             final Indentation indentation,
                                                             final LineEnding lineEnding,
                                                             final HateosResourceHandler<SpreadsheetId, SpreadsheetMetadata, SpreadsheetMetadataSet> createAndSaveMetadata,
                                                             final HateosResourceHandler<SpreadsheetId, SpreadsheetMetadata, SpreadsheetMetadataSet> deleteMetadata,
                                                             final HateosResourceHandler<SpreadsheetId, SpreadsheetMetadata, SpreadsheetMetadataSet> loadMetadata) {
        Objects.requireNonNull(baseUrl, "baseUrl");
        Objects.requireNonNull(contentType, "contentType");
        Objects.requireNonNull(indentation, "indentation");
        Objects.requireNonNull(lineEnding, "lineEnding");
        Objects.requireNonNull(createAndSaveMetadata, "createAndSaveMetadata");
        Objects.requireNonNull(deleteMetadata, "deleteMetadata");
        Objects.requireNonNull(loadMetadata, "loadMetadata");

        // metadata GET, POST...........................................................................................

        return HateosResourceMapping.router(
                baseUrl,
                contentType,
                Sets.of(
                        HateosResourceMapping.with(
                                        SPREADSHEET,
                                        SpreadsheetContextHateosResourceHandlersRouter::parse,
                                        SpreadsheetMetadata.class,
                                        SpreadsheetMetadataSet.class,
                                        SpreadsheetMetadata.class
                                )
                                .set(METADATA_LINK_RELATION, HttpMethod.DELETE, deleteMetadata)
                                .set(METADATA_LINK_RELATION, HttpMethod.GET, loadMetadata)
                                .set(METADATA_LINK_RELATION, HttpMethod.POST, createAndSaveMetadata)
                                .set(METADATA_LINK_RELATION, HttpMethod.PATCH, HateosResourceHandlers.fake())
                ),
                indentation,
                lineEnding
        );
    }

    private static HateosResourceSelection<SpreadsheetId> parse(final String text) {
        try {
            final HateosResourceSelection<SpreadsheetId> selection;

            switch (text) {
                case "":
                    selection = HateosResourceSelection.none();
                    break;
                case "*":
                    selection = HateosResourceSelection.all();
                    break;
                default:
                    selection = HateosResourceSelection.one(
                            SpreadsheetId.parse(text)
                    );
                    break;
            }

            return selection;
        } catch (final Exception cause) {
            throw new IllegalArgumentException("Invalid id " + CharSequences.quoteAndEscape(text));
        }
    }

    /**
     * Stop creation.
     */
    private SpreadsheetContextHateosResourceHandlersRouter() {
        throw new UnsupportedOperationException();
    }
}
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
package walkingkooka.spreadsheet.server.label.hateos;

import walkingkooka.collect.set.Sets;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.header.LinkRelation;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.hateos.HateosContentType;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.net.http.server.hateos.HateosResourceMapping;
import walkingkooka.net.http.server.hateos.HateosResourceName;
import walkingkooka.net.http.server.hateos.HateosResourceSelection;
import walkingkooka.reflect.StaticHelper;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.text.CharSequences;

import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * A collection of factory methods to create various {@link HateosHandler}.
 */
final class SpreadsheetLabelHateosHandlersRouter implements StaticHelper {

    /**
     * A {@link HateosResourceName} with <code>metadata</code>.
     */
    private final static HateosResourceName LABEL = HateosResourceName.with("label");

    /**
     * Used to form the metadata load and save services
     * <pre>
     * /api/spreadsheet/$spreadsheet-id/label
     * </pre>
     */
    private final static LinkRelation<?> METADATA_LINK_RELATION = LinkRelation.SELF;

    /**
     * Builds a {@link Router} that handles all {@link SpreadsheetLabelMapping}, using the given {@link HateosHandler handlers}.
     */
    static Router<HttpRequestAttribute<?>, BiConsumer<HttpRequest, HttpResponse>> with(final AbsoluteUrl baseUrl,
                                                                                       final HateosContentType contentType,
                                                                                       final HateosHandler<SpreadsheetLabelName, SpreadsheetLabelMapping, SpreadsheetLabelMapping> delete,
                                                                                       final HateosHandler<SpreadsheetLabelName, SpreadsheetLabelMapping, SpreadsheetLabelMapping> load,
                                                                                       final HateosHandler<SpreadsheetLabelName, SpreadsheetLabelMapping, SpreadsheetLabelMapping> saveOrUpdate) {
        Objects.requireNonNull(baseUrl, "baseUrl");
        Objects.requireNonNull(contentType, "contentType");
        Objects.requireNonNull(delete, "delete");
        Objects.requireNonNull(load, "load");
        Objects.requireNonNull(saveOrUpdate, "saveOrUpdate");

        return HateosResourceMapping.router(baseUrl,
                contentType,
                Sets.of(HateosResourceMapping.with(LABEL,
                        SpreadsheetLabelHateosHandlersRouter::parse,
                        SpreadsheetLabelMapping.class,
                        SpreadsheetLabelMapping.class,
                        SpreadsheetLabelMapping.class)
                        .set(METADATA_LINK_RELATION, HttpMethod.DELETE, delete)
                        .set(METADATA_LINK_RELATION, HttpMethod.GET, load)
                        .set(METADATA_LINK_RELATION, HttpMethod.POST, saveOrUpdate)));
    }

    private static HateosResourceSelection<SpreadsheetLabelName> parse(final String text) {
        try {
            HateosResourceSelection<SpreadsheetLabelName> selection;

            if (text.isEmpty()) {
                selection = HateosResourceSelection.none();
            } else {
                selection = HateosResourceSelection.one(SpreadsheetLabelName.labelName(text));
            }

            return selection;
        } catch (final Exception cause) {
            throw new IllegalArgumentException("Invalid label name " + CharSequences.quoteAndEscape(text));
        }
    }

    /**
     * Stop creation.
     */
    private SpreadsheetLabelHateosHandlersRouter() {
        throw new UnsupportedOperationException();
    }
}

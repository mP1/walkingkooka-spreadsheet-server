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
package walkingkooka.spreadsheet.server.label;

import walkingkooka.net.header.LinkRelation;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.net.http.server.hateos.HateosResourceMapping;
import walkingkooka.net.http.server.hateos.HateosResourceName;
import walkingkooka.net.http.server.hateos.HateosResourceSelection;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.text.CharSequences;

import java.util.Objects;

/**
 * A collection of factory methods to create various {@link HateosHandler}.
 */
public final class SpreadsheetLabelHateosResourceMappings implements PublicStaticHelper {

    public static HateosResourceMapping<SpreadsheetLabelName, SpreadsheetLabelMapping, SpreadsheetLabelMapping, SpreadsheetLabelMapping> with(final HateosHandler<SpreadsheetLabelName, SpreadsheetLabelMapping, SpreadsheetLabelMapping> delete,
                                                                                                                                              final HateosHandler<SpreadsheetLabelName, SpreadsheetLabelMapping, SpreadsheetLabelMapping> load,
                                                                                                                                              final HateosHandler<SpreadsheetLabelName, SpreadsheetLabelMapping, SpreadsheetLabelMapping> saveOrUpdate) {
        Objects.requireNonNull(delete, "delete");
        Objects.requireNonNull(load, "load");
        Objects.requireNonNull(saveOrUpdate, "saveOrUpdate");

        return HateosResourceMapping.with(LABEL,
                SpreadsheetLabelHateosResourceMappings::parse,
                SpreadsheetLabelMapping.class,
                SpreadsheetLabelMapping.class,
                SpreadsheetLabelMapping.class)
                .set(METADATA_LINK_RELATION, HttpMethod.DELETE, delete)
                .set(METADATA_LINK_RELATION, HttpMethod.GET, load)
                .set(METADATA_LINK_RELATION, HttpMethod.POST, saveOrUpdate);
    }

    /**
     * A {@link HateosResourceName} with <code>metadata</code>.
     */
    private final static HateosResourceName LABEL = HateosResourceName.with("label");

    private static HateosResourceSelection<SpreadsheetLabelName> parse(final String text) {
        try {
            HateosResourceSelection<SpreadsheetLabelName> selection;

            if (text.isEmpty()) {
                selection = HateosResourceSelection.none();
            } else {
                selection = HateosResourceSelection.one(SpreadsheetSelection.labelName(text));
            }

            return selection;
        } catch (final Exception cause) {
            throw new IllegalArgumentException("Invalid label name " + CharSequences.quoteAndEscape(text));
        }
    }

    /**
     * Used to form the metadata load and save services
     * <pre>
     * /api/spreadsheet/$spreadsheet-id/label
     * </pre>
     */
    private final static LinkRelation<?> METADATA_LINK_RELATION = LinkRelation.SELF;

    /**
     * Stop creation.
     */
    private SpreadsheetLabelHateosResourceMappings() {
        throw new UnsupportedOperationException();
    }
}

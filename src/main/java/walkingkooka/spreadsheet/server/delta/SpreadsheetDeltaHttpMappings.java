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
package walkingkooka.spreadsheet.server.delta;

import walkingkooka.NeverError;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.header.LinkRelation;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.net.http.server.hateos.HateosResourceMappings;
import walkingkooka.net.http.server.hateos.HateosResourceSelection;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetColumn;
import walkingkooka.spreadsheet.SpreadsheetHateosResourceNames;
import walkingkooka.spreadsheet.SpreadsheetRow;
import walkingkooka.spreadsheet.SpreadsheetValueType;
import walkingkooka.spreadsheet.engine.SpreadsheetCellQuery;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetRowRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetServerLinkRelations;
import walkingkooka.spreadsheet.validation.form.SpreadsheetForms;
import walkingkooka.text.CharSequences;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;
import walkingkooka.tree.expression.Expression;
import walkingkooka.validation.form.Form;
import walkingkooka.validation.form.FormName;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A collection of factory methods to create various {@link HateosResourceHandler}.
 */
public final class SpreadsheetDeltaHttpMappings implements PublicStaticHelper {

    // cell.............................................................................................................

    public static HateosResourceMappings<SpreadsheetCellReference,
        SpreadsheetDelta,
        SpreadsheetDelta,
        SpreadsheetCell,
        SpreadsheetEngineHateosResourceHandlerContext> cell(final SpreadsheetEngine engine,
                                                            final int defaultMax,
                                                            final Indentation indentation,
                                                            final LineEnding lineEnding,
                                                            final SpreadsheetEngineHateosResourceHandlerContext handlerContext) {
        Objects.requireNonNull(engine, "engine");
        // cell GET, POST...............................................................................................

        HateosResourceMappings<SpreadsheetCellReference,
            SpreadsheetDelta,
            SpreadsheetDelta,
            SpreadsheetCell,
            SpreadsheetEngineHateosResourceHandlerContext> cell = HateosResourceMappings.with(
            SpreadsheetHateosResourceNames.CELL,
            SpreadsheetDeltaHttpMappings::parseCell,
            SpreadsheetDelta.class,
            SpreadsheetDelta.class,
            SpreadsheetCell.class,
            SpreadsheetEngineHateosResourceHandlerContext.class
        ).setHateosResourceHandler(
            LinkRelation.SELF,
            HttpMethod.GET,
            SpreadsheetDeltaHateosResourceHandlerLoadCell.with(
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY // default SpreadsheetEngineEvaluation
            )
        ).setHateosResourceHandler(
            LinkRelation.SELF,
            HttpMethod.POST,
            SpreadsheetDeltaHateosResourceHandlerSaveCell.INSTANCE
        ).setHateosResourceHandler(
            LinkRelation.SELF,
            HttpMethod.DELETE,
            SpreadsheetDeltaHateosResourceHandlerDeleteCell.INSTANCE
        ).setHateosHttpEntityHandler(
            LinkRelation.SELF,
            HttpMethod.PATCH,
            SpreadsheetDeltaPatchHateosHttpEntityHandler.cell(
                engine
            )
        );

        // cell/SpreadsheetEngineEvaluation GET.........................................................................

        for (final SpreadsheetEngineEvaluation evaluation : SpreadsheetEngineEvaluation.values()) {
            cell = cell.setHateosResourceHandler(
                evaluation.toLinkRelation(),
                HttpMethod.GET,
                SpreadsheetDeltaHateosResourceHandlerLoadCell.with(evaluation)
            );
        }

        // cell/fill/find/sort POST.....................................................................................
        cell = cell.setHateosResourceHandler(
            SpreadsheetServerLinkRelations.FILL,
            HttpMethod.POST,
            SpreadsheetDeltaHateosResourceHandlerFillCells.INSTANCE
        );

        cell = cell.setHateosResourceHandler(
            SpreadsheetServerLinkRelations.FIND,
            HttpMethod.GET,
            SpreadsheetDeltaHateosResourceHandlerFindCells.INSTANCE
        );

        cell = cell.setHttpHandler(
            FormName.HATEOS_RESOURCE_NAME.toUrlPathName(),
            SpreadsheetDeltaHttpMappingsFormHttpHandler.with(
                engine,
                indentation,
                lineEnding,
                handlerContext
            )
        );

        cell = cell.setHateosResourceHandler(
            SpreadsheetServerLinkRelations.LABELS,
            HttpMethod.GET,
            SpreadsheetDeltaHateosResourceHandlerFindLabelsWithReference.INSTANCE
        );

        cell = cell.setHateosResourceHandler(
            SpreadsheetServerLinkRelations.REFERENCES,
            HttpMethod.GET,
            SpreadsheetDeltaHateosResourceHandlerFindCellsWithReferences.INSTANCE
        );

        cell = cell.setHateosResourceHandler(
            SpreadsheetServerLinkRelations.SORT,
            HttpMethod.GET,
            SpreadsheetDeltaHateosResourceHandlerSortCells.INSTANCE
        );

        return cell;
    }

    /**
     * Handles parsing just a cell or label or a range with either, always resolving labels to cells. Labels will never be returned.
     */
    private static HateosResourceSelection<SpreadsheetCellReference> parseCell(final String cellOrLabel,
                                                                               final SpreadsheetEngineHateosResourceHandlerContext context) {
        final HateosResourceSelection<SpreadsheetCellReference> result;

        switch (cellOrLabel) {
            case HateosResourceSelection.NONE:
                result = HateosResourceSelection.none();
                break;
            case HateosResourceSelection.ALL:
                result = HateosResourceSelection.all();
                break;
            default:
                SpreadsheetSelection reference = context.resolveIfLabelOrFail(
                    SpreadsheetSelection.parseExpressionReference(cellOrLabel)
                );
                if (reference.isCell()) {
                    result = HateosResourceSelection.one(
                        reference.toCell()
                    );
                } else {
                    if (reference.isCellRange()) {
                        result = HateosResourceSelection.range(
                            reference.toCellRange()
                                .range()
                        );
                    } else {
                        throw new NeverError("Expected cell or cell-range got " + reference);
                    }
                }
                break;
        }

        return result;
    }

    // column...........................................................................................................

    public static HateosResourceMappings<SpreadsheetColumnReference,
        SpreadsheetDelta,
        SpreadsheetDelta,
        SpreadsheetColumn,
        SpreadsheetEngineHateosResourceHandlerContext> column(final SpreadsheetEngine engine) {
        return HateosResourceMappings.with(
            SpreadsheetHateosResourceNames.COLUMN,
            SpreadsheetDeltaHttpMappings::parseColumn,
            SpreadsheetDelta.class,
            SpreadsheetDelta.class,
            SpreadsheetColumn.class,
            SpreadsheetEngineHateosResourceHandlerContext.class
        ).setHateosResourceHandler(
            SpreadsheetServerLinkRelations.CLEAR,
            HttpMethod.POST,
            SpreadsheetDeltaHateosResourceHandlerClearColumns.INSTANCE
        ).setHateosResourceHandler(
            SpreadsheetServerLinkRelations.INSERT_AFTER,
            HttpMethod.POST,
            SpreadsheetDeltaHateosResourceHandlerInsertAfterColumn.INSTANCE
        ).setHateosResourceHandler(
            SpreadsheetServerLinkRelations.INSERT_BEFORE,
            HttpMethod.POST,
            SpreadsheetDeltaHateosResourceHandlerInsertBeforeColumn.INSTANCE
        ).setHateosResourceHandler(
            LinkRelation.SELF,
            HttpMethod.DELETE,
            SpreadsheetDeltaHateosResourceHandlerDeleteColumns.INSTANCE
        ).setHateosHttpEntityHandler(
            LinkRelation.SELF,
            HttpMethod.PATCH,
            SpreadsheetDeltaPatchHateosHttpEntityHandler.column(
                engine
            )
        );
    }

    private static HateosResourceSelection<SpreadsheetColumnReference> parseColumn(final String selection,
                                                                                   final SpreadsheetEngineHateosResourceHandlerContext context) {
        final SpreadsheetColumnRangeReference parsed = SpreadsheetSelection.parseColumnRange(selection);

        return parsed.isUnit() ?
            HateosResourceSelection.one(parsed.begin()) :
            HateosResourceSelection.range(parsed.range());
    }

    // row..............................................................................................................

    public static HateosResourceMappings<SpreadsheetRowReference,
        SpreadsheetDelta,
        SpreadsheetDelta,
        SpreadsheetRow,
        SpreadsheetEngineHateosResourceHandlerContext> row(final SpreadsheetEngine engine) {
        return HateosResourceMappings.with(
            SpreadsheetHateosResourceNames.ROW,
            SpreadsheetDeltaHttpMappings::parseRow,
            SpreadsheetDelta.class,
            SpreadsheetDelta.class,
            SpreadsheetRow.class,
            SpreadsheetEngineHateosResourceHandlerContext.class
        ).setHateosResourceHandler(
            SpreadsheetServerLinkRelations.CLEAR,
            HttpMethod.POST,
            SpreadsheetDeltaHateosResourceHandlerClearRows.INSTANCE
        ).setHateosResourceHandler(
            SpreadsheetServerLinkRelations.INSERT_AFTER,
            HttpMethod.POST,
            SpreadsheetDeltaHateosResourceHandlerInsertAfterRow.INSTANCE
        ).setHateosResourceHandler(
            SpreadsheetServerLinkRelations.INSERT_BEFORE,
            HttpMethod.POST,
            SpreadsheetDeltaHateosResourceHandlerInsertBeforeRow.INSTANCE
        ).setHateosResourceHandler(
            LinkRelation.SELF,
            HttpMethod.DELETE,
            SpreadsheetDeltaHateosResourceHandlerDeleteRows.INSTANCE
        ).setHateosHttpEntityHandler(
            LinkRelation.SELF,
            HttpMethod.PATCH,
            SpreadsheetDeltaPatchHateosHttpEntityHandler.row(
                engine
            )
        );
    }

    private static HateosResourceSelection<SpreadsheetRowReference> parseRow(final String selection,
                                                                             final SpreadsheetEngineHateosResourceHandlerContext context) {
        final SpreadsheetRowRangeReference parsed = SpreadsheetSelection.parseRowRange(selection);

        return parsed.isUnit() ?
            HateosResourceSelection.one(parsed.begin()) :
            HateosResourceSelection.range(parsed.range());
    }

    // FORM.............................................................................................................

    /**
     * <pre>
     * /api/spreadsheet/$spreadsheet-id/form
     * </pre>
     */
    private final static LinkRelation<?> FORM_LINK_RELATION = LinkRelation.SELF;

    /**
     * Factory that creates a form end points
     */
    public static HateosResourceMappings<FormName, SpreadsheetDelta, SpreadsheetDelta, Form<SpreadsheetExpressionReference>, SpreadsheetEngineHateosResourceHandlerContext> form(final int defaultCount) {
        return HateosResourceMappings.with(
            FormName.HATEOS_RESOURCE_NAME,
            SpreadsheetDeltaHttpMappings::parseForm,
            SpreadsheetDelta.class,
            SpreadsheetDelta.class,
            SpreadsheetForms.FORM_CLASS,
            SpreadsheetEngineHateosResourceHandlerContext.class
        ).setHateosResourceHandler(
            FORM_LINK_RELATION,
            HttpMethod.GET,
            SpreadsheetDeltaHateosResourceHandlerLoadForm.with(
                defaultCount
            )
        ).setHateosResourceHandler(
            FORM_LINK_RELATION,
            HttpMethod.POST,
            SpreadsheetDeltaHateosResourceHandlerSaveForm.INSTANCE
        ).setHateosResourceHandler(
            FORM_LINK_RELATION,
            HttpMethod.DELETE,
            SpreadsheetDeltaHateosResourceHandlerDeleteForm.INSTANCE
        );
    }

    static HateosResourceSelection<FormName> parseForm(final String text,
                                                       final SpreadsheetEngineHateosResourceHandlerContext context) {
        try {
            HateosResourceSelection<FormName> selection;

            switch (text) {
                case HateosResourceSelection.NONE:
                    selection = HateosResourceSelection.none();
                    break;
                case HateosResourceSelection.ALL:
                    selection = HateosResourceSelection.all();
                    break;
                default:
                    selection = HateosResourceSelection.one(
                        FormName.with(text)
                    );
                    break;
            }

            return selection;
        } catch (final Exception cause) {
            throw new IllegalArgumentException("Invalid form name " + CharSequences.quoteAndEscape(text));
        }
    }

    // LABEL............................................................................................................

    /**
     * Used to support CRUD operations upon labels.
     * <pre>
     * /api/spreadsheet/$spreadsheet-id/label
     * </pre>
     */
    private final static LinkRelation<?> LABEL_LINK_RELATION = LinkRelation.SELF;

    /**
     * /api/spreadsheet/SpreadsheetId/label/&star;/findByName/query-here
     */
    public final static LinkRelation<?> FIND_BY_NAME = LinkRelation.with("findByName");

    /**
     * Factory that creates a labels.
     */
    public static HateosResourceMappings<SpreadsheetLabelName, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetLabelMapping, SpreadsheetEngineHateosResourceHandlerContext> label(final int defaultCount,
                                                                                                                                                                                final SpreadsheetEngine engine) {
        return HateosResourceMappings.with(
            SpreadsheetHateosResourceNames.LABEL,
            SpreadsheetDeltaHttpMappings::parseLabel,
            SpreadsheetDelta.class,
            SpreadsheetDelta.class,
            SpreadsheetLabelMapping.class,
            SpreadsheetEngineHateosResourceHandlerContext.class
        ).setHateosResourceHandler(
            LABEL_LINK_RELATION,
            HttpMethod.DELETE,
            SpreadsheetDeltaHateosResourceHandlerDeleteLabel.INSTANCE
        ).setHateosResourceHandler(
            LABEL_LINK_RELATION,
            HttpMethod.GET,
            SpreadsheetDeltaHateosResourceHandlerLoadLabel.with(
                defaultCount
            )
        ).setHateosResourceHandler(
            LABEL_LINK_RELATION,
            HttpMethod.POST,
            SpreadsheetDeltaHateosResourceHandlerSaveLabel.INSTANCE
        ).setHateosResourceHandler(
            FIND_BY_NAME,
            HttpMethod.GET,
            SpreadsheetDeltaHateosResourceHandlerFindLabelsByName.with(
                defaultCount
            )
        );
    }

    private static HateosResourceSelection<SpreadsheetLabelName> parseLabel(final String text,
                                                                            final SpreadsheetEngineHateosResourceHandlerContext context) {
        try {
            HateosResourceSelection<SpreadsheetLabelName> selection;

            switch (text) {
                case HateosResourceSelection.NONE:
                    selection = HateosResourceSelection.none();
                    break;
                case HateosResourceSelection.ALL:
                    selection = HateosResourceSelection.all();
                    break;
                default:
                    selection = HateosResourceSelection.one(
                        SpreadsheetSelection.labelName(text)
                    );
                    break;
            }

            return selection;
        } catch (final Exception cause) {
            throw new IllegalArgumentException("Invalid label name " + CharSequences.quoteAndEscape(text));
        }
    }

    // prepareResponse..................................................................................................

    /**
     * Prepares a {@link SpreadsheetDelta} response honouring any present query and window query parameters.
     */
    static SpreadsheetDelta prepareResponse(final Optional<SpreadsheetDelta> in,
                                            final Map<HttpRequestAttribute<?>, Object> parameters,
                                            final SpreadsheetDelta out,
                                            final SpreadsheetEngine engine,
                                            final SpreadsheetEngineContext context) {

        final Optional<SpreadsheetCellQuery> query = extractOrMetadataFindHighlightingAndQuery(
            parameters,
            context
        );

        SpreadsheetDelta result = out;

        if (query.isPresent()) {
            result = out.setMatchedCells(
                engine.filterCells(
                        out.cells(),
                        SpreadsheetValueType.ANY,
                        context.toExpression(
                            query.get()
                                .parserToken()
                        ).orElse(DEFAULT_EXPRESSION),
                        context
                    ).stream()
                    .map(
                        SpreadsheetCell::reference
                    ).collect(Collectors.toCollection(Sets::ordered))
            );
        }

        return result.setWindow(
            SpreadsheetDeltaUrlQueryParameters.window(
                parameters,
                in,
                engine,
                context
            )
        );
    }

    private final static Expression DEFAULT_EXPRESSION = Expression.value(Boolean.TRUE);

    /**
     * Helper which attempts to read the {@link SpreadsheetCellQuery} from the given parameters and if that is missing
     * then tries if highlighting is enabled {@link SpreadsheetMetadataPropertyName#FIND_QUERY}.
     */
    private static Optional<SpreadsheetCellQuery> extractOrMetadataFindHighlightingAndQuery(final Map<HttpRequestAttribute<?>, Object> parameters,
                                                                                            final SpreadsheetEngineContext context) {
        Optional<SpreadsheetCellQuery> query = SpreadsheetCellQuery.extract(parameters);
        if (false == query.isPresent()) {
            final SpreadsheetMetadata metadata = context.spreadsheetMetadata();
            if (metadata.get(SpreadsheetMetadataPropertyName.FIND_HIGHLIGHTING).orElse(false)) {
                query = metadata.get(SpreadsheetMetadataPropertyName.FIND_QUERY);
            }
        }

        return query;
    }

    /**
     * Stop creation.
     */
    private SpreadsheetDeltaHttpMappings() {
        throw new UnsupportedOperationException();
    }
}

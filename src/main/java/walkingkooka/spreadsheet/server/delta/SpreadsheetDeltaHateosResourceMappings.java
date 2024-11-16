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

import walkingkooka.collect.set.Sets;
import walkingkooka.net.header.LinkRelation;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.net.http.server.hateos.HateosResourceMapping;
import walkingkooka.net.http.server.hateos.HateosResourceName;
import walkingkooka.net.http.server.hateos.HateosResourceSelection;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetColumn;
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
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetRowRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.server.SpreadsheetHateosResourceHandlerContext;
import walkingkooka.text.CharSequences;
import walkingkooka.tree.expression.Expression;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A collection of factory methods to create various {@link HateosResourceHandler}.
 */
public final class SpreadsheetDeltaHateosResourceMappings implements PublicStaticHelper {

    // cell................................................................................................................

    public static HateosResourceMapping<SpreadsheetCellReference,
            SpreadsheetDelta,
            SpreadsheetDelta,
            SpreadsheetCell,
            SpreadsheetHateosResourceHandlerContext> cell(final SpreadsheetEngine engine,
                                                          final int defaultMax) {
        Objects.requireNonNull(engine, "engine");
        // cell GET, POST...............................................................................................

        HateosResourceMapping<SpreadsheetCellReference,
                SpreadsheetDelta,
                SpreadsheetDelta,
                SpreadsheetCell,
                SpreadsheetHateosResourceHandlerContext> cell = HateosResourceMapping.with(
                CELL,
                SpreadsheetDeltaHateosResourceMappings::parseCell,
                SpreadsheetDelta.class,
                SpreadsheetDelta.class,
                SpreadsheetCell.class,
                SpreadsheetHateosResourceHandlerContext.class
        ).setHateosResourceHandler(
                LinkRelation.SELF,
                HttpMethod.GET,
                SpreadsheetDeltaHateosResourceHandlerLoadCell.with(
                        SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY, // default SpreadsheetEngineEvaluation
                        engine
                )
        ).setHateosResourceHandler(
                LinkRelation.SELF,
                HttpMethod.POST,
                SpreadsheetDeltaHateosResourceHandlerSaveCell.with(
                        engine
                )
        ).setHateosResourceHandler(
                LinkRelation.SELF,
                HttpMethod.DELETE,
                SpreadsheetDeltaHateosResourceHandlerDeleteCell.with(
                        engine
                )
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
                    SpreadsheetDeltaHateosResourceHandlerLoadCell.with(
                            evaluation,
                            engine
                    )
            );
        }

        // cell/fill/find/sort POST.....................................................................................
        cell = cell.setHateosResourceHandler(
                FILL,
                HttpMethod.POST,
                SpreadsheetDeltaHateosResourceHandlerFillCells.with(
                        engine
                )
        );

        cell = cell.setHateosResourceHandler(
                FIND,
                HttpMethod.GET,
                SpreadsheetDeltaHateosResourceHandlerFindCells.with(
                        defaultMax,
                        engine
                )
        );

        cell = cell.setHateosResourceHandler(
                SORT,
                HttpMethod.GET,
                SpreadsheetDeltaHateosResourceHandlerSortCells.with(
                        engine
                )
        );

        return cell;
    }

    /**
     * A {@link HateosResourceName} with <code>cell</code>.
     */
    public static final HateosResourceName CELL = HateosResourceName.with("cell");

    /**
     * Handles parsing just a cell or label or a range with either, always resolving labels to cells. Labels will never be returned.
     */
    private static HateosResourceSelection<SpreadsheetCellReference> parseCell(final String cellOrLabel,
                                                                               final SpreadsheetHateosResourceHandlerContext context) {
        final HateosResourceSelection<SpreadsheetCellReference> result;

        if (cellOrLabel.isEmpty()) {
            result = HateosResourceSelection.none();
        } else {
            if ("*".equals(cellOrLabel)) {
                result = HateosResourceSelection.all();
            } else {
                final int separator = cellOrLabel.indexOf(SpreadsheetSelection.SEPARATOR.character());
                switch (separator) {
                    case -1:
                        result = HateosResourceSelection.one(
                                parseCell0(
                                        cellOrLabel,
                                        context
                                )
                        );
                        break;
                    case 0:
                        throw new IllegalArgumentException("Missing begin");
                    default:
                        final SpreadsheetCellReference begin = parseCell0(
                                cellOrLabel.substring(0, separator),
                                context
                        );

                        if (separator + 1 == cellOrLabel.length()) {
                            throw new IllegalArgumentException("Missing end");
                        }
                        final SpreadsheetCellReference end = parseCell0(
                                cellOrLabel.substring(separator + 1),
                                context
                        );
                        result = HateosResourceSelection.range(begin.range(end));
                        break;
                }
            }
        }

        return result;
    }

    /**
     * Parses the given text as either a cell reference or label name, if the later it is resolved to a {@link SpreadsheetCellReference}.
     */
    private static SpreadsheetCellReference parseCell0(final String cellOrLabelText,
                                                       final SpreadsheetHateosResourceHandlerContext context) {
        return context.resolveIfLabel(
                SpreadsheetSelection.parseCellOrLabel(cellOrLabelText)
        ).toCell();
    }

    /**
     * A {@link LinkRelation} with <code>clear</code>.
     */
    public static final LinkRelation<?> CLEAR = LinkRelation.with("clear");

    /**
     * A {@link LinkRelation} with <code>fill</code>.
     */
    public static final LinkRelation<?> FILL = LinkRelation.with("fill");

    /**
     * A {@link LinkRelation} with <code>find</code>.
     */
    public static final LinkRelation<?> FIND = LinkRelation.with("find");

    /**
     * A {@link LinkRelation} with <code>sort</code>.
     */
    public static final LinkRelation<?> SORT = LinkRelation.with("sort");

    // cellReference....................................................................................................

    public static HateosResourceMapping<String,
            SpreadsheetExpressionReferenceSimilarities,
            SpreadsheetExpressionReferenceSimilarities,
            SpreadsheetExpressionReferenceSimilarities,
            SpreadsheetHateosResourceHandlerContext> cellReference(final SpreadsheetEngineContext context) {

        return HateosResourceMapping.with(
                CELL_REFERENCE,
                SpreadsheetDeltaHateosResourceMappings::parseCellReferenceText,
                SpreadsheetExpressionReferenceSimilarities.class,
                SpreadsheetExpressionReferenceSimilarities.class,
                SpreadsheetExpressionReferenceSimilarities.class,
                SpreadsheetHateosResourceHandlerContext.class
        ).setHateosResourceHandler(
                LinkRelation.SELF,
                HttpMethod.GET,
                SpreadsheetExpressionReferenceSimilaritiesHateosResourceHandler.with(context)
        );
    }

    /**
     * A {@link HateosResourceName} with <code>cell-reference</code>.
     */
    private static final HateosResourceName CELL_REFERENCE = HateosResourceName.with("cell-reference");

    private static HateosResourceSelection<String> parseCellReferenceText(final String text,
                                                                          final SpreadsheetHateosResourceHandlerContext context) {
        return HateosResourceSelection.one(text);
    }

    // column...........................................................................................................

    public static HateosResourceMapping<SpreadsheetColumnReference,
            SpreadsheetDelta,
            SpreadsheetDelta,
            SpreadsheetColumn,
            SpreadsheetHateosResourceHandlerContext> column(final SpreadsheetEngine engine) {
        return HateosResourceMapping.with(
                COLUMN,
                SpreadsheetDeltaHateosResourceMappings::parseColumn,
                SpreadsheetDelta.class,
                SpreadsheetDelta.class,
                SpreadsheetColumn.class,
                SpreadsheetHateosResourceHandlerContext.class
        ).setHateosResourceHandler(
                CLEAR,
                HttpMethod.POST,
                SpreadsheetDeltaHateosResourceHandlerClearColumns.with(
                        engine
                )
        ).setHateosResourceHandler(
                AFTER,
                HttpMethod.POST,
                SpreadsheetDeltaHateosResourceHandlerInsertAfterColumn.with(
                        engine
                )
        ).setHateosResourceHandler(
                BEFORE,
                HttpMethod.POST,
                SpreadsheetDeltaHateosResourceHandlerInsertBeforeColumn.with(
                        engine
                )
        ).setHateosResourceHandler(
                LinkRelation.SELF,
                HttpMethod.DELETE,
                SpreadsheetDeltaHateosResourceHandlerDeleteColumns.with(
                        engine
                )
        ).setHateosHttpEntityHandler(
                LinkRelation.SELF,
                HttpMethod.PATCH,
                SpreadsheetDeltaPatchHateosHttpEntityHandler.column(
                        engine
                )
        );
    }

    /**
     * A {@link HateosResourceName} with <code>column</code>.
     */
    private static final HateosResourceName COLUMN = HateosResourceName.with("column");

    private static HateosResourceSelection<SpreadsheetColumnReference> parseColumn(final String selection,
                                                                                   final SpreadsheetHateosResourceHandlerContext context) {
        final SpreadsheetColumnRangeReference parsed = SpreadsheetSelection.parseColumnRange(selection);

        return parsed.isSingle() ?
                HateosResourceSelection.one(parsed.begin()) :
                HateosResourceSelection.range(parsed.range());
    }
    
    // row..............................................................................................................

    public static HateosResourceMapping<SpreadsheetRowReference,
            SpreadsheetDelta,
            SpreadsheetDelta,
            SpreadsheetRow,
            SpreadsheetHateosResourceHandlerContext> row(final SpreadsheetEngine engine) {
        return HateosResourceMapping.with(
                ROW,
                SpreadsheetDeltaHateosResourceMappings::parseRow,
                SpreadsheetDelta.class,
                SpreadsheetDelta.class,
                SpreadsheetRow.class,
                SpreadsheetHateosResourceHandlerContext.class
        ).setHateosResourceHandler(
                CLEAR,
                HttpMethod.POST,
                SpreadsheetDeltaHateosResourceHandlerClearRows.with(
                        engine
                )
        ).setHateosResourceHandler(
                AFTER,
                HttpMethod.POST,
                SpreadsheetDeltaHateosResourceHandlerInsertAfterRow.with(
                        engine
                )
        ).setHateosResourceHandler(
                BEFORE,
                HttpMethod.POST,
                SpreadsheetDeltaHateosResourceHandlerInsertBeforeRow.with(
                        engine
                )
        ).setHateosResourceHandler(
                LinkRelation.SELF,
                HttpMethod.DELETE,
                SpreadsheetDeltaHateosResourceHandlerDeleteRows.with(
                        engine
                )
        ).setHateosHttpEntityHandler(
                LinkRelation.SELF,
                HttpMethod.PATCH,
                SpreadsheetDeltaPatchHateosHttpEntityHandler.row(
                        engine
                )
        );
    }

    /**
     * A {@link HateosResourceName} with <code>row</code>.
     */
    private static final HateosResourceName ROW = HateosResourceName.with("row");

    private static HateosResourceSelection<SpreadsheetRowReference> parseRow(final String selection,
                                                                             final SpreadsheetHateosResourceHandlerContext context) {
        final SpreadsheetRowRangeReference parsed = SpreadsheetSelection.parseRowRange(selection);

        return parsed.isSingle() ?
                HateosResourceSelection.one(parsed.begin()) :
                HateosResourceSelection.range(parsed.range());
    }

    public final static LinkRelation<?> AFTER = LinkRelation.with("after");

    public final static LinkRelation<?> BEFORE = LinkRelation.with("before");

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
     * Used to form the metadata load and save services
     * <pre>
     * /api/spreadsheet/$spreadsheet-id/label
     * </pre>
     */
    private final static LinkRelation<?> LABEL_LINK_RELATION = LinkRelation.SELF;

    /**
     * Factory that creates a labels.
     */
    public static HateosResourceMapping<SpreadsheetLabelName, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetLabelMapping, SpreadsheetHateosResourceHandlerContext> label(final SpreadsheetEngine engine) {
        return HateosResourceMapping.with(
                LABEL,
                SpreadsheetDeltaHateosResourceMappings::parseLabel,
                SpreadsheetDelta.class,
                SpreadsheetDelta.class,
                SpreadsheetLabelMapping.class,
                SpreadsheetHateosResourceHandlerContext.class
        ).setHateosResourceHandler(
                LABEL_LINK_RELATION,
                HttpMethod.DELETE,
                SpreadsheetDeltaHateosResourceHandlerDeleteLabel.with(engine)
        ).setHateosResourceHandler(
                LABEL_LINK_RELATION,
                HttpMethod.GET,
                SpreadsheetDeltaHateosResourceHandlerLoadLabel.with(engine)
        ).setHateosResourceHandler(
                LABEL_LINK_RELATION,
                HttpMethod.POST,
                SpreadsheetDeltaHateosResourceHandlerSaveLabel.with(engine)
        );
    }

    /**
     * A {@link HateosResourceName} with <code>metadata</code>.
     */
    public final static HateosResourceName LABEL = HateosResourceName.with("label");

    private static HateosResourceSelection<SpreadsheetLabelName> parseLabel(final String text,
                                                                            final SpreadsheetHateosResourceHandlerContext context) {
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
     * Stop creation.
     */
    private SpreadsheetDeltaHateosResourceMappings() {
        throw new UnsupportedOperationException();
    }
}

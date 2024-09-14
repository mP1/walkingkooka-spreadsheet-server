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
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetRowRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.server.engine.SpreadsheetEngineHateosResourceHandlerContext;
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
            SpreadsheetEngineHateosResourceHandlerContext> cell(final SpreadsheetEngine engine,
                                                                final int defaultMax) {
        Objects.requireNonNull(engine, "engine");
        // cell GET, POST...............................................................................................

        HateosResourceMapping<SpreadsheetCellReference,
                SpreadsheetDelta,
                SpreadsheetDelta,
                SpreadsheetCell,
                SpreadsheetEngineHateosResourceHandlerContext> cell = HateosResourceMapping.with(
                CELL,
                SpreadsheetDeltaHateosResourceMappings::parseCell,
                SpreadsheetDelta.class,
                SpreadsheetDelta.class,
                SpreadsheetCell.class,
                SpreadsheetEngineHateosResourceHandlerContext.class
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
                                                                               final SpreadsheetEngineHateosResourceHandlerContext context) {
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
                                                       final SpreadsheetEngineHateosResourceHandlerContext context) {
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
            SpreadsheetEngineHateosResourceHandlerContext> cellReference(final SpreadsheetEngineContext context) {

        return HateosResourceMapping.with(
                CELL_REFERENCE,
                SpreadsheetDeltaHateosResourceMappings::parseCellReferenceText,
                SpreadsheetExpressionReferenceSimilarities.class,
                SpreadsheetExpressionReferenceSimilarities.class,
                SpreadsheetExpressionReferenceSimilarities.class,
                SpreadsheetEngineHateosResourceHandlerContext.class
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
                                                                          final SpreadsheetEngineHateosResourceHandlerContext context) {
        return HateosResourceSelection.one(text);
    }

    // column...........................................................................................................

    public static HateosResourceMapping<SpreadsheetColumnReference,
            SpreadsheetDelta,
            SpreadsheetDelta,
            SpreadsheetColumn,
            SpreadsheetEngineHateosResourceHandlerContext> column(final SpreadsheetEngine engine) {
        return HateosResourceMapping.with(
                COLUMN,
                SpreadsheetDeltaHateosResourceMappings::parseColumn,
                SpreadsheetDelta.class,
                SpreadsheetDelta.class,
                SpreadsheetColumn.class,
                SpreadsheetEngineHateosResourceHandlerContext.class
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
                                                                                   final SpreadsheetEngineHateosResourceHandlerContext context) {
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
            SpreadsheetEngineHateosResourceHandlerContext> row(final SpreadsheetEngine engine) {
        return HateosResourceMapping.with(
                ROW,
                SpreadsheetDeltaHateosResourceMappings::parseRow,
                SpreadsheetDelta.class,
                SpreadsheetDelta.class,
                SpreadsheetRow.class,
                SpreadsheetEngineHateosResourceHandlerContext.class
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
                                                                             final SpreadsheetEngineHateosResourceHandlerContext context) {
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

        final Optional<Expression> maybeExpression = SpreadsheetDeltaUrlQueryParameters.query(
                parameters,
                context
        );

        final Optional<String> maybeValueType = SpreadsheetDeltaUrlQueryParameters.valueType(
                parameters,
                context
        );

        SpreadsheetDelta result = out;

        if (maybeExpression.isPresent()) {
            result = out.setMatchedCells(
                    engine.filterCells(
                                    out.cells(),
                                    maybeValueType.orElse(SpreadsheetValueType.ANY),
                                    maybeExpression.get(),
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

    /**
     * Stop creation.
     */
    private SpreadsheetDeltaHateosResourceMappings() {
        throw new UnsupportedOperationException();
    }
}

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

package walkingkooka.spreadsheet.server.url;

import org.junit.jupiter.api.Test;
import walkingkooka.HashCodeEqualsDefinedTesting2;
import walkingkooka.collect.map.Maps;
import walkingkooka.net.UrlPath;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.SpreadsheetName;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterSelector;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReferenceOrRange;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetRowRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReferenceOrRange;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.server.locale.LocaleTag;
import walkingkooka.template.TemplateContext;
import walkingkooka.template.TemplateContexts;
import walkingkooka.template.TemplateTesting2;
import walkingkooka.template.TemplateValueName;
import walkingkooka.tree.text.TextStylePropertyName;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetUrlPathTemplateTest implements TemplateTesting2<SpreadsheetUrlPathTemplate>,
    HashCodeEqualsDefinedTesting2<SpreadsheetUrlPathTemplate> {

    private final static SpreadsheetId ID = SpreadsheetId.with(0x123);
    private final static SpreadsheetName NAME = SpreadsheetName.with("SpreadsheetName456");
    private final LocaleTag LOCALE_TAG = LocaleTag.with(
        Locale.forLanguageTag("en-AU")
    );
    private final static SpreadsheetEngineEvaluation SPREADSHEET_ENGINE_EVALUATION = SpreadsheetEngineEvaluation.SKIP_EVALUATE;
    private final static SpreadsheetExpressionReference CELL = SpreadsheetSelection.A1;
    private final static SpreadsheetLabelName LABEL = SpreadsheetSelection.labelName("Label123");
    private final static SpreadsheetCellRangeReference CELL_RANGE = SpreadsheetSelection.parseCellRange("A2:A3");

    private final static SpreadsheetColumnReference COLUMN = SpreadsheetSelection.parseColumn("B");
    private final static SpreadsheetColumnRangeReference COLUMN_RANGE = SpreadsheetSelection.parseColumnRange("C:D");

    private final static SpreadsheetRowReference ROW = SpreadsheetSelection.parseRow("2");
    private final static SpreadsheetRowRangeReference ROW_RANGE = SpreadsheetSelection.parseRowRange("3:4");

    private final static SpreadsheetFormatterSelector SPREADSHEET_FORMATTER_SELECTOR = SpreadsheetFormatterSelector.DEFAULT_TEXT_FORMAT;

    private final static SpreadsheetMetadataPropertyName<?> SPREADSHEET_METADATA_PROPERTY_NAME = SpreadsheetMetadataPropertyName.ROUNDING_MODE;

    private final static TextStylePropertyName<?> TEXT_STYLE_PROPERTY_NAME = TextStylePropertyName.TEXT_ALIGN;

    // removeRootSlashIfNecessary.......................................................................................

    @Test
    public void testRemoveRootSlashIfNecessaryWithNullFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetUrlPathTemplate.removeRootSlashIfNecessary(null)
        );
    }

    @Test
    public void testRemoveRootSlashIfNecessaryWithEmpty() {
        this.removeRootSlashIfNecessaryAndCheck(
            ""
        );
    }

    @Test
    public void testRemoveRootSlashIfNecessaryWithMissingRootSlash() {
        this.removeRootSlashIfNecessaryAndCheck(
            "missing start slash"
        );
    }

    private void removeRootSlashIfNecessaryAndCheck(final String text) {
        assertSame(
            text,
            SpreadsheetUrlPathTemplate.removeRootSlashIfNecessary(text),
            text
        );
    }

    @Test
    public void testRemoveRootSlashIfNecessaryWithRootSlash() {
        this.removeRootSlashIfNecessaryAndCheck(
            "/path1/path2/path3",
            "path1/path2/path3"
        );
    }

    private void removeRootSlashIfNecessaryAndCheck(final String text,
                                                    final String expected) {
        this.checkEquals(
            expected,
            SpreadsheetUrlPathTemplate.removeRootSlashIfNecessary(text),
            text
        );
    }

    // get..........................................................................................................

    @Test
    public void testGetWithNullPathFails() {
        assertThrows(
            NullPointerException.class,
            () -> this.createTemplate()
                .get(
                    null,
                    TemplateValueName.with("Hello"),
                    Function.identity()
                )
        );
    }

    @Test
    public void testGetWithNullNameFails() {
        assertThrows(
            NullPointerException.class,
            () -> this.createTemplate()
                .get(
                    UrlPath.EMPTY,
                    null,
                    Function.identity()
                )
        );
    }

    @Test
    public void testGetWithNullTypeFails() {
        assertThrows(
            NullPointerException.class,
            () -> this.createTemplate()
                .get(
                    UrlPath.EMPTY,
                    TemplateValueName.with("Hello"),
                    null
                )
        );
    }

    @Test
    public void testGetSpreadsheetIdExisting() {
        this.getAndCheck(
            "/api/spreadsheet/${SpreadsheetId}/name/${SpreadsheetName}/cell",
            "/api/spreadsheet/123/name/SpreadsheetName456/cell",
            SpreadsheetUrlPathTemplate.SPREADSHEET_ID,
            SpreadsheetId::parse,
            Optional.of(ID)
        );
    }

    @Test
    public void testGetSpreadsheetNameExisting() {
        this.getAndCheck(
            "/api/spreadsheet/${SpreadsheetId}/name/${SpreadsheetName}/cell",
            "/api/spreadsheet/123/name/SpreadsheetName456/cell",
            SpreadsheetUrlPathTemplate.SPREADSHEET_NAME,
            SpreadsheetName::with,
            Optional.of(NAME)
        );
    }

    @Test
    public void testGetLocaleTagExisting() {
        this.getAndCheck(
            "/api/dateTimeSymbols/${LocaleTag}/*",
            "/api/dateTimeSymbols/en-AU/*",
            SpreadsheetUrlPathTemplate.LOCALE_TAG,
            LocaleTag::parse,
            Optional.of(LOCALE_TAG)
        );
    }

    @Test
    public void testGetCustomExisting() {
        this.getAndCheck(
            "/api/${String123}/*",
            "/api/Hello/*",
            TemplateValueName.with("String123"),
            Function.identity(),
            Optional.of("Hello")
        );
    }

    @Test
    public void testGetMissing() {
        this.getAndCheck(
            "/api/spreadsheet/${SpreadsheetId}/name/${SpreadsheetName}/cell",
            "/api/spreadsheet/123/name/SpreadsheetName456/cell",
            TemplateValueName.with("unknown"),
            Function.identity(),
            Optional.empty()
        );
    }

    private <T> void getAndCheck(final String template,
                                 final String path,
                                 final TemplateValueName name,
                                 final Function<String, T> type,
                                 final Optional<T> expected) {
        this.getAndCheck(
            SpreadsheetUrlPathTemplate.parse(template),
            UrlPath.parse(path),
            name,
            type,
            expected
        );
    }

    private <T> void getAndCheck(final SpreadsheetUrlPathTemplate template,
                                 final UrlPath path,
                                 final TemplateValueName name,
                                 final Function<String, T> type,
                                 final Optional<T> expected) {
        this.checkEquals(
            expected,
            template.get(
                path,
                name,
                type
            )
        );
    }

    // localeTag..................................................................................................

    @Test
    public void testLocaleTag() {
        this.checkEquals(
            LOCALE_TAG,
            SpreadsheetUrlPathTemplate.parse("/api/locale/${LocaleTag}/")
                .localeTag(
                    UrlPath.parse("/api/locale/en-AU/"
                    )
                )
        );
    }

    // spreadsheetColumnReferenceOrRange................................................................................

    @Test
    public void testSpreadsheetColumnReferenceOrRangeWithCell() {
        this.spreadsheetColumnReferenceOrRangeAndCheck(
            "/api/spreadsheet/${SpreadsheetId}/column/${SpreadsheetColumnReferenceOrRange}",
            "/api/spreadsheet/123/column/B",
            COLUMN
        );
    }

    @Test
    public void testSpreadsheetColumnReferenceOrRangeWithCellRange() {
        this.spreadsheetColumnReferenceOrRangeAndCheck(
            "/api/spreadsheet/${SpreadsheetId}/column/${SpreadsheetColumnReferenceOrRange}",
            "/api/spreadsheet/123/column/C:D",
            COLUMN_RANGE
        );
    }

    private void spreadsheetColumnReferenceOrRangeAndCheck(final String template,
                                                           final String path,
                                                           final SpreadsheetColumnReferenceOrRange expected) {
        this.spreadsheetColumnReferenceOrRangeAndCheck(
            SpreadsheetUrlPathTemplate.parse(template),
            UrlPath.parse(path),
            expected
        );
    }

    private void spreadsheetColumnReferenceOrRangeAndCheck(final SpreadsheetUrlPathTemplate template,
                                                           final UrlPath path,
                                                           final SpreadsheetColumnReferenceOrRange expected) {
        this.checkEquals(
            expected,
            template.spreadsheetColumnReferenceOrRange(path)
        );
    }

    // spreadsheetEngineEvaluation......................................................................................

    @Test
    public void testSpreadsheetEngineEvaluation() {
        this.spreadsheetEngineEvaluationAndCheck(
            "/api/spreadsheet/${SpreadsheetId}/cell/A1/${SpreadsheetEngineEvaluation}",
            "/api/spreadsheet/123/cell/A1/skip-evaluate",
            SpreadsheetEngineEvaluation.SKIP_EVALUATE
        );
    }

    private void spreadsheetEngineEvaluationAndCheck(final String template,
                                                     final String path,
                                                     final SpreadsheetEngineEvaluation expected) {
        this.spreadsheetEngineEvaluationAndCheck(
            SpreadsheetUrlPathTemplate.parse(template),
            UrlPath.parse(path),
            expected
        );
    }

    private void spreadsheetEngineEvaluationAndCheck(final SpreadsheetUrlPathTemplate template,
                                                     final UrlPath path,
                                                     final SpreadsheetEngineEvaluation expected) {
        this.checkEquals(
            expected,
            template.spreadsheetEngineEvaluation(path)
        );
    }

    // spreadsheetExpressionReference...................................................................................

    @Test
    public void testSpreadsheetExpressionReferenceWithCell() {
        this.spreadsheetExpressionReferenceAndCheck(
            "/api/spreadsheet/${SpreadsheetId}/cell/${SpreadsheetExpressionReference}/${SpreadsheetExpressionReference}",
            "/api/spreadsheet/123/cell/A1/skip-evaluate",
            CELL
        );
    }

    @Test
    public void testSpreadsheetExpressionReferenceWithCellRange() {
        this.spreadsheetExpressionReferenceAndCheck(
            "/api/spreadsheet/${SpreadsheetId}/cell/${SpreadsheetExpressionReference}/${SpreadsheetExpressionReference}",
            "/api/spreadsheet/123/cell/A2:A3/skip-evaluate",
            CELL_RANGE
        );
    }

    @Test
    public void testSpreadsheetExpressionReferenceWithLabel() {
        this.spreadsheetExpressionReferenceAndCheck(
            "/api/spreadsheet/${SpreadsheetId}/cell/${SpreadsheetExpressionReference}/${SpreadsheetExpressionReference}",
            "/api/spreadsheet/123/cell/Label123/skip-evaluate",
            LABEL
        );
    }

    private void spreadsheetExpressionReferenceAndCheck(final String template,
                                                        final String path,
                                                        final SpreadsheetExpressionReference expected) {
        this.spreadsheetExpressionReferenceAndCheck(
            SpreadsheetUrlPathTemplate.parse(template),
            UrlPath.parse(path),
            expected
        );
    }

    private void spreadsheetExpressionReferenceAndCheck(final SpreadsheetUrlPathTemplate template,
                                                        final UrlPath path,
                                                        final SpreadsheetExpressionReference expected) {
        this.checkEquals(
            expected,
            template.spreadsheetExpressionReference(path)
        );
    }

    // spreadsheetFormatterSelector......................................................................................

    @Test
    public void testSpreadsheetFormatterSelector() {
        this.spreadsheetFormatterSelectorAndCheck(
            "/api/spreadsheet/123/cell/A1/formatter/${SpreadsheetFormatterSelector}",
            "/api/spreadsheet/123/cell/A1/formatter/text @",
            SpreadsheetFormatterSelector.DEFAULT_TEXT_FORMAT
        );
    }

    private void spreadsheetFormatterSelectorAndCheck(final String template,
                                                      final String path,
                                                      final SpreadsheetFormatterSelector expected) {
        this.spreadsheetFormatterSelectorAndCheck(
            SpreadsheetUrlPathTemplate.parse(template),
            UrlPath.parse(path),
            expected
        );
    }

    private void spreadsheetFormatterSelectorAndCheck(final SpreadsheetUrlPathTemplate template,
                                                      final UrlPath path,
                                                      final SpreadsheetFormatterSelector expected) {
        this.checkEquals(
            expected,
            template.spreadsheetFormatterSelector(path)
        );
    }
    
    // spreadsheetId....................................................................................................

    @Test
    public void testSpreadsheetId() {
        this.spreadsheetIdAndCheck(
            "/api/spreadsheet/${SpreadsheetId}/",
            "/api/spreadsheet/123/different",
            Optional.of(ID)
        );
    }

    @Test
    public void testSpreadsheetIdMissing() {
        this.spreadsheetIdAndCheck(
            "/api/spreadsheet/${SpreadsheetId}/",
            "/api/different",
            Optional.empty()
        );
    }

    @Test
    public void testSpreadsheetIdDifferentPath() {
        this.spreadsheetIdAndCheck(
            "/api/spreadsheet/${SpreadsheetId}/different",
            "/api/spreadsheet/123",
            Optional.empty()
        );
    }

    private void spreadsheetIdAndCheck(final String template,
                                       final String path,
                                       final Optional<SpreadsheetId> expected) {
        this.spreadsheetIdAndCheck(
            SpreadsheetUrlPathTemplate.parse(template),
            UrlPath.parse(path),
            expected
        );
    }

    private void spreadsheetIdAndCheck(final SpreadsheetUrlPathTemplate template,
                                       final UrlPath path,
                                       final Optional<SpreadsheetId> expected) {
        this.checkEquals(
            expected,
            template.spreadsheetId(path)
        );
    }

    // spreadsheetLabelName............................................................................................

    @Test
    public void testSpreadsheetLabelName() {
        this.spreadsheetLabelNameAndCheck(
            "/api/spreadsheet/${SpreadsheetId}/label/${SpreadsheetLabelName}",
            "/api/spreadsheet/123/label/Label123",
            Optional.of(LABEL)
        );
    }

    @Test
    public void testSpreadsheetLabelNameMissing() {
        this.spreadsheetLabelNameAndCheck(
            "/api/spreadsheet/${SpreadsheetId}/label",
            "/api/spreadsheet/123/label",
            Optional.empty()
        );
    }

    private void spreadsheetLabelNameAndCheck(final String template,
                                              final String path,
                                              final Optional<SpreadsheetLabelName> expected) {
        this.spreadsheetLabelNameAndCheck(
            SpreadsheetUrlPathTemplate.parse(template),
            UrlPath.parse(path),
            expected
        );
    }

    private void spreadsheetLabelNameAndCheck(final SpreadsheetUrlPathTemplate template,
                                              final UrlPath path,
                                              final Optional<SpreadsheetLabelName> expected) {
        this.checkEquals(
            expected,
            template.spreadsheetLabelName(path)
        );
    }

    // spreadsheetMetadataPropertyName..................................................................................

    @Test
    public void testSpreadsheetMetadataPropertyName() {
        this.spreadsheetMetadataPropertyNameAndCheck(
            "/api/spreadsheet/${SpreadsheetMetadataPropertyName}/",
            "/api/spreadsheet/roundingMode/different",
            SPREADSHEET_METADATA_PROPERTY_NAME
        );
    }

    private void spreadsheetMetadataPropertyNameAndCheck(final String template,
                                                         final String path,
                                                         final SpreadsheetMetadataPropertyName<?> expected) {
        this.spreadsheetMetadataPropertyNameAndCheck(
            SpreadsheetUrlPathTemplate.parse(template),
            UrlPath.parse(path),
            expected
        );
    }

    private void spreadsheetMetadataPropertyNameAndCheck(final SpreadsheetUrlPathTemplate template,
                                                         final UrlPath path,
                                                         final SpreadsheetMetadataPropertyName<?> expected) {
        this.checkEquals(
            expected,
            template.spreadsheetMetadataPropertyName(path)
        );
    }

    // spreadsheetName..................................................................................................

    @Test
    public void testSpreadsheetName() {
        this.checkEquals(
            NAME,
            SpreadsheetUrlPathTemplate.parse("/api/spreadsheetName/${SpreadsheetName}/")
                .spreadsheetName(
                    UrlPath.parse("/api/spreadsheetName/SpreadsheetName456/"
                    )
                )
        );
    }

    // spreadsheetRowReferenceOrRange...................................................................................

    @Test
    public void testSpreadsheetRowReferenceOrRangeWithCell() {
        this.spreadsheetRowReferenceOrRangeAndCheck(
            "/api/spreadsheet/${SpreadsheetId}/row/${SpreadsheetRowReferenceOrRange}",
            "/api/spreadsheet/123/row/2",
            ROW
        );
    }

    @Test
    public void testSpreadsheetRowReferenceOrRangeWithCellRange() {
        this.spreadsheetRowReferenceOrRangeAndCheck(
            "/api/spreadsheet/${SpreadsheetId}/row/${SpreadsheetRowReferenceOrRange}",
            "/api/spreadsheet/123/row/3:4",
            ROW_RANGE
        );
    }

    private void spreadsheetRowReferenceOrRangeAndCheck(final String template,
                                                        final String path,
                                                        final SpreadsheetRowReferenceOrRange expected) {
        this.spreadsheetRowReferenceOrRangeAndCheck(
            SpreadsheetUrlPathTemplate.parse(template),
            UrlPath.parse(path),
            expected
        );
    }

    private void spreadsheetRowReferenceOrRangeAndCheck(final SpreadsheetUrlPathTemplate template,
                                                        final UrlPath path,
                                                        final SpreadsheetRowReferenceOrRange expected) {
        this.checkEquals(
            expected,
            template.spreadsheetRowReferenceOrRange(path)
        );
    }

    // textStylePropertyName..................................................................................

    @Test
    public void testTextStylePropertyName() {
        this.textStylePropertyNameAndCheck(
            "/api/spreadsheet/style/${TextStylePropertyName}",
            "/api/spreadsheet/style/text-align",
            TEXT_STYLE_PROPERTY_NAME
        );
    }

    private void textStylePropertyNameAndCheck(final String template,
                                               final String path,
                                               final TextStylePropertyName<?> expected) {
        this.textStylePropertyNameAndCheck(
            SpreadsheetUrlPathTemplate.parse(template),
            UrlPath.parse(path),
            expected
        );
    }

    private void textStylePropertyNameAndCheck(final SpreadsheetUrlPathTemplate template,
                                               final UrlPath path,
                                               final TextStylePropertyName<?> expected) {
        this.checkEquals(
            expected,
            template.textStylePropertyName(path)
        );
    }

    // extract..........................................................................................................

    @Test
    public void testExtractWithNullMapFails() {
        assertThrows(
            NullPointerException.class,
            () -> this.createTemplate().extract(null)
        );
    }

    @Test
    public void testExtract() {
        final Map<TemplateValueName, Object> expected = Maps.sorted();
        expected.put(
            SpreadsheetUrlPathTemplate.SPREADSHEET_ID,
            ID
        );
        expected.put(
            SpreadsheetUrlPathTemplate.SPREADSHEET_NAME,
            NAME
        );
        expected.put(
            SpreadsheetUrlPathTemplate.SPREADSHEET_EXPRESSION_REFERENCE,
            CELL
        );
        expected.put(
            SpreadsheetUrlPathTemplate.SPREADSHEET_ENGINE_EVALUATION,
            SPREADSHEET_ENGINE_EVALUATION
        );
        expected.put(
            SpreadsheetUrlPathTemplate.SPREADSHEET_COLUMN_REFERENCE_OR_RANGE,
            COLUMN
        );
        expected.put(
            SpreadsheetUrlPathTemplate.SPREADSHEET_ROW_REFERENCE_OR_RANGE,
            ROW
        );
        expected.put(
            SpreadsheetUrlPathTemplate.SPREADSHEET_LABEL_NAME,
            LABEL
        );
        expected.put(
            SpreadsheetUrlPathTemplate.SPREADSHEET_METADATA_PROPERTY_NAME,
            SPREADSHEET_METADATA_PROPERTY_NAME
        );
        expected.put(
            SpreadsheetUrlPathTemplate.TEXT_STYLE_PROPERTY_NAME,
            TEXT_STYLE_PROPERTY_NAME
        );
        expected.put(
            SpreadsheetUrlPathTemplate.SPREADSHEET_FORMATTER_SELECTOR,
            SPREADSHEET_FORMATTER_SELECTOR
        );

        this.checkEquals(
            expected,
            this.createTemplate()
                .extract(
                    UrlPath.parse(
                        "/api/spreadsheet/123/name/SpreadsheetName456/cell/A1/skip-evaluate/column/B/row/2" +
                            "/label/Label123/metadata/roundingMode/style/text-align/formatter/text @"
                    )
                )
        );
    }

    @Test
    public void testExtractCustomParameters() {
        final Map<TemplateValueName, Object> expected = Maps.sorted();
        expected.put(
            SpreadsheetUrlPathTemplate.SPREADSHEET_ID,
            ID
        );
        expected.put(
            SpreadsheetUrlPathTemplate.SPREADSHEET_NAME,
            NAME
        );
        expected.put(
            TemplateValueName.with("Custom1"),
            "Value1"
        );
        expected.put(
            TemplateValueName.with("Custom2"),
            "/Value2"
        );

        this.checkEquals(
            expected,
            SpreadsheetUrlPathTemplate.parse("/api/spreadsheet/${SpreadsheetId}/name/${SpreadsheetName}/custom1/${Custom1}/custom2/${Custom2}")
                .extract(
                    UrlPath.parse(
                        "/api/spreadsheet/123/name/SpreadsheetName456/custom1/Value1/custom2/Value2"
                    )
                )
        );
    }

    // render...........................................................................................................

    @Test
    public void testRender() {
        this.renderAndCheck(
            this.createTemplate(),
            TemplateContexts.renderOnly(
                (n) -> {
                    if (n.equals(SpreadsheetUrlPathTemplate.SPREADSHEET_ID)) {
                        return ID.toString();
                    }
                    if (n.equals(SpreadsheetUrlPathTemplate.SPREADSHEET_NAME)) {
                        return NAME.toString();
                    }
                    if (n.equals(SpreadsheetUrlPathTemplate.SPREADSHEET_EXPRESSION_REFERENCE)) {
                        return CELL.toString();
                    }
                    if (n.equals(SpreadsheetUrlPathTemplate.SPREADSHEET_ENGINE_EVALUATION)) {
                        return SPREADSHEET_ENGINE_EVALUATION.toLinkRelation()
                            .toUrlPathName()
                            .get()
                            .value();
                    }
                    if (n.equals(SpreadsheetUrlPathTemplate.SPREADSHEET_COLUMN_REFERENCE_OR_RANGE)) {
                        return COLUMN.toStringMaybeStar();
                    }
                    if (n.equals(SpreadsheetUrlPathTemplate.SPREADSHEET_ROW_REFERENCE_OR_RANGE)) {
                        return ROW.toStringMaybeStar();
                    }
                    if (n.equals(SpreadsheetUrlPathTemplate.SPREADSHEET_FORMATTER_SELECTOR)) {
                        return SPREADSHEET_FORMATTER_SELECTOR.toString();
                    }
                    if (n.equals(SpreadsheetUrlPathTemplate.SPREADSHEET_LABEL_NAME)) {
                        return LABEL.toStringMaybeStar();
                    }
                    if (n.equals(SpreadsheetUrlPathTemplate.SPREADSHEET_METADATA_PROPERTY_NAME)) {
                        return SPREADSHEET_METADATA_PROPERTY_NAME.value();
                    }
                    if (n.equals(SpreadsheetUrlPathTemplate.TEXT_STYLE_PROPERTY_NAME)) {
                        return TEXT_STYLE_PROPERTY_NAME.value();
                    }

                    throw new IllegalArgumentException("Unknown value=" + n);
                }
            ),
            "/api/spreadsheet/123/name/SpreadsheetName456/cell/A1/skip-evaluate/column/B/row/2" +
                "/label/Label123/metadata/roundingMode/style/text-align/formatter/text @"
        );
    }

    // renderPath.......................................................................................................

    @Test
    public void testRenderPath() {
        final Map<TemplateValueName, Object> values = Maps.sorted();
        values.put(
            SpreadsheetUrlPathTemplate.SPREADSHEET_ID,
            ID
        );
        values.put(
            SpreadsheetUrlPathTemplate.SPREADSHEET_NAME,
            NAME
        );
        values.put(
            SpreadsheetUrlPathTemplate.SPREADSHEET_EXPRESSION_REFERENCE,
            CELL
        );
        values.put(
            SpreadsheetUrlPathTemplate.SPREADSHEET_ENGINE_EVALUATION,
            SPREADSHEET_ENGINE_EVALUATION
        );
        values.put(
            SpreadsheetUrlPathTemplate.SPREADSHEET_COLUMN_REFERENCE_OR_RANGE,
            COLUMN
        );
        values.put(
            SpreadsheetUrlPathTemplate.SPREADSHEET_ROW_REFERENCE_OR_RANGE,
            ROW
        );
        values.put(
            SpreadsheetUrlPathTemplate.SPREADSHEET_LABEL_NAME,
            LABEL
        );
        values.put(
            SpreadsheetUrlPathTemplate.LOCALE_TAG,
            LOCALE_TAG
        );
        values.put(
            SpreadsheetUrlPathTemplate.SPREADSHEET_METADATA_PROPERTY_NAME,
            SPREADSHEET_METADATA_PROPERTY_NAME
        );
        values.put(
            SpreadsheetUrlPathTemplate.TEXT_STYLE_PROPERTY_NAME,
            TEXT_STYLE_PROPERTY_NAME
        );
        values.put(
            SpreadsheetUrlPathTemplate.SPREADSHEET_FORMATTER_SELECTOR,
            SPREADSHEET_FORMATTER_SELECTOR
        );

        this.checkEquals(
            UrlPath.parse(
                "/api/spreadsheet/123/name/SpreadsheetName456/cell/A1/localeTag/en-AU/column/B/row/2" +
                    "/label/Label123/metadata/roundingMode/style/text-align/formatter/text @"),
            SpreadsheetUrlPathTemplate.parse(
                "/api/spreadsheet/${SpreadsheetId}/name/${SpreadsheetName}/cell/${SpreadsheetExpressionReference}/localeTag/${LocaleTag}" +
                    "/column/${SpreadsheetColumnReferenceOrRange}/row/${SpreadsheetRowReferenceOrRange}" +
                    "/label/${SpreadsheetLabelName}/metadata/roundingMode/style/${TextStylePropertyName}" +
                    "/formatter/${SpreadsheetFormatterSelector}"
            ).render(values)
        );
    }

    @Override
    public SpreadsheetUrlPathTemplate createTemplate() {
        return SpreadsheetUrlPathTemplate.parse(
            "/api/spreadsheet/${SpreadsheetId}/name/${SpreadsheetName}" +
            "/cell/${SpreadsheetExpressionReference}/${SpreadsheetEngineEvaluation}" +
            "/column/${SpreadsheetColumnReferenceOrRange}/row/${SpreadsheetRowReferenceOrRange}" +
            "/label/${SpreadsheetLabelName}/metadata/${SpreadsheetMetadataPropertyName}/style/${TextStylePropertyName}" +
            "/formatter/${SpreadsheetFormatterSelector}"
        );
    }

    @Override
    public TemplateContext createContext() {
        return TemplateContexts.fake();
    }

    // hashCode/equals..................................................................................................

    @Test
    public void testEqualsDifferentTemplate() {
        this.checkNotEquals(
            this.createContext(),
            SpreadsheetUrlPathTemplate.parse("/different")
        );
    }

    // TreePrintable....................................................................................................

    @Test
    public void testTreePrint() {
        this.treePrintAndCheck(
            this.createTemplate(),
            "SpreadsheetUrlPathTemplate\n" +
                "  UrlPathTemplate\n" +
                "    TemplateCollection\n" +
                "      StringTemplate\n" +
                "        \"/\"\n" +
                "      StringTemplate\n" +
                "        \"api\"\n" +
                "      StringTemplate\n" +
                "        \"/\"\n" +
                "      StringTemplate\n" +
                "        \"spreadsheet\"\n" +
                "      StringTemplate\n" +
                "        \"/\"\n" +
                "      TemplateValueNameTemplate\n" +
                "        ${SpreadsheetId}\n" +
                "      StringTemplate\n" +
                "        \"/\"\n" +
                "      StringTemplate\n" +
                "        \"name\"\n" +
                "      StringTemplate\n" +
                "        \"/\"\n" +
                "      TemplateValueNameTemplate\n" +
                "        ${SpreadsheetName}\n" +
                "      StringTemplate\n" +
                "        \"/\"\n" +
                "      StringTemplate\n" +
                "        \"cell\"\n" +
                "      StringTemplate\n" +
                "        \"/\"\n" +
                "      TemplateValueNameTemplate\n" +
                "        ${SpreadsheetExpressionReference}\n" +
                "      StringTemplate\n" +
                "        \"/\"\n" +
                "      TemplateValueNameTemplate\n" +
                "        ${SpreadsheetEngineEvaluation}\n" +
                "      StringTemplate\n" +
                "        \"/\"\n" +
                "      StringTemplate\n" +
                "        \"column\"\n" +
                "      StringTemplate\n" +
                "        \"/\"\n" +
                "      TemplateValueNameTemplate\n" +
                "        ${SpreadsheetColumnReferenceOrRange}\n" +
                "      StringTemplate\n" +
                "        \"/\"\n" +
                "      StringTemplate\n" +
                "        \"row\"\n" +
                "      StringTemplate\n" +
                "        \"/\"\n" +
                "      TemplateValueNameTemplate\n" +
                "        ${SpreadsheetRowReferenceOrRange}\n" +
                "      StringTemplate\n" +
                "        \"/\"\n" +
                "      StringTemplate\n" +
                "        \"label\"\n" +
                "      StringTemplate\n" +
                "        \"/\"\n" +
                "      TemplateValueNameTemplate\n" +
                "        ${SpreadsheetLabelName}\n" +
                "      StringTemplate\n" +
                "        \"/\"\n" +
                "      StringTemplate\n" +
                "        \"metadata\"\n" +
                "      StringTemplate\n" +
                "        \"/\"\n" +
                "      TemplateValueNameTemplate\n" +
                "        ${SpreadsheetMetadataPropertyName}\n" +
                "      StringTemplate\n" +
                "        \"/\"\n" +
                "      StringTemplate\n" +
                "        \"style\"\n" +
                "      StringTemplate\n" +
                "        \"/\"\n" +
                "      TemplateValueNameTemplate\n" +
                "        ${TextStylePropertyName}\n" +
                "      StringTemplate\n" +
                "        \"/\"\n" +
                "      StringTemplate\n" +
                "        \"formatter\"\n" +
                "      StringTemplate\n" +
                "        \"/\"\n" +
                "      TemplateValueNameTemplate\n" +
                "        ${SpreadsheetFormatterSelector}\n"
        );
    }

    // class............................................................................................................

    @Override
    public Class<SpreadsheetUrlPathTemplate> type() {
        return SpreadsheetUrlPathTemplate.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }
}

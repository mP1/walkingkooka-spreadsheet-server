/*
 * Copyright 2023 Miroslav Pokorny (github.com/mP1)
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

import walkingkooka.Cast;
import walkingkooka.collect.map.Maps;
import walkingkooka.net.UrlPath;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.SpreadsheetName;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSelector;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReferenceOrRange;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReferenceOrRange;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.server.locale.LocaleTag;
import walkingkooka.template.Template;
import walkingkooka.template.TemplateContext;
import walkingkooka.template.TemplateValueName;
import walkingkooka.template.url.UrlPathTemplate;
import walkingkooka.template.url.UrlPathTemplateValues;
import walkingkooka.text.CaseKind;
import walkingkooka.text.LineEnding;
import walkingkooka.text.printer.IndentingPrinter;
import walkingkooka.text.printer.Printer;
import walkingkooka.tree.text.TextStylePropertyName;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * A {@link Template} that supports extracting well known named parameters having values path components automatically converted.
 */
public final class SpreadsheetUrlPathTemplate implements Template {

    public final static TemplateValueName LOCALE_TAG = TemplateValueName.with("LocaleTag");

    public final static TemplateValueName SPREADSHEET_COLUMN_REFERENCE_OR_RANGE = TemplateValueName.with(SpreadsheetColumnReferenceOrRange.class.getSimpleName());

    public final static TemplateValueName SPREADSHEET_ENGINE_EVALUATION = TemplateValueName.with(SpreadsheetEngineEvaluation.class.getSimpleName());

    public final static TemplateValueName SPREADSHEET_EXPRESSION_REFERENCE = TemplateValueName.with(SpreadsheetExpressionReference.class.getSimpleName());

    public final static TemplateValueName SPREADSHEET_FORMATTER_SELECTOR = TemplateValueName.with(SpreadsheetFormatterSelector.class.getSimpleName());

    public final static TemplateValueName SPREADSHEET_ID = TemplateValueName.with(SpreadsheetId.class.getSimpleName());

    public final static TemplateValueName SPREADSHEET_LABEL_NAME = TemplateValueName.with(SpreadsheetLabelName.class.getSimpleName());

    public final static TemplateValueName SPREADSHEET_METADATA_PROPERTY_NAME = TemplateValueName.with(SpreadsheetMetadataPropertyName.class.getSimpleName());

    public final static TemplateValueName SPREADSHEET_NAME = TemplateValueName.with(SpreadsheetName.class.getSimpleName());

    public final static TemplateValueName SPREADSHEET_ROW_REFERENCE_OR_RANGE = TemplateValueName.with(SpreadsheetRowReferenceOrRange.class.getSimpleName());

    public final static TemplateValueName TEXT_STYLE_PROPERTY_NAME = TemplateValueName.with(TextStylePropertyName.class.getSimpleName());

    public static SpreadsheetUrlPathTemplate parse(final String template) {
        return new SpreadsheetUrlPathTemplate(
            UrlPathTemplate.parse(template)
        );
    }

    private SpreadsheetUrlPathTemplate(final UrlPathTemplate template) {
        this.template = template;
    }

    public LocaleTag localeTag(final UrlPath path) {
        return getOrFail(
            path,
            LOCALE_TAG,
            LocaleTag.class
        );
    }

    public SpreadsheetColumnReferenceOrRange spreadsheetColumnReferenceOrRange(final UrlPath path) {
        return Cast.to(
            this.getOrFail(
                path,
                SPREADSHEET_COLUMN_REFERENCE_OR_RANGE,
                SpreadsheetColumnReferenceOrRange.class
            )
        );
    }

    public SpreadsheetEngineEvaluation spreadsheetEngineEvaluation(final UrlPath path) {
        return Cast.to(
            this.getOrFail(
                path,
                SPREADSHEET_ENGINE_EVALUATION,
                SpreadsheetExpressionReference.class
            )
        );
    }

    public Optional<SpreadsheetEngineEvaluation> spreadsheetLabelName(final UrlPath path) {
        return Cast.to(
            this.get(
                path,
                SPREADSHEET_LABEL_NAME,
                SpreadsheetLabelName.class
            )
        );
    }

    public SpreadsheetMetadataPropertyName<?> spreadsheetMetadataPropertyName(final UrlPath path) {
        return getOrFail(
            path,
            SPREADSHEET_METADATA_PROPERTY_NAME,
            SpreadsheetMetadataPropertyName.class
        );
    }

    public SpreadsheetExpressionReference spreadsheetExpressionReference(final UrlPath path) {
        return Cast.to(
            this.getOrFail(
                path,
                SPREADSHEET_EXPRESSION_REFERENCE,
                SpreadsheetExpressionReference.class
            )
        );
    }

    public SpreadsheetFormatterSelector spreadsheetFormatterSelector(final UrlPath path) {
        return Cast.to(
            this.getOrFail(
                path,
                SPREADSHEET_FORMATTER_SELECTOR,
                SpreadsheetFormatterSelector.class
            )
        );
    }

    public Optional<SpreadsheetId> spreadsheetId(final UrlPath path) {
        return this.get(
            path,
            SPREADSHEET_ID,
            SpreadsheetId.class
        );
    }

    public SpreadsheetName spreadsheetName(final UrlPath path) {
        return getOrFail(
            path,
            SPREADSHEET_NAME,
            SpreadsheetName.class
        );
    }

    public SpreadsheetRowReferenceOrRange spreadsheetRowReferenceOrRange(final UrlPath path) {
        return this.getOrFail(
            path,
            SPREADSHEET_ROW_REFERENCE_OR_RANGE,
            SpreadsheetRowReferenceOrRange.class
        );
    }

    public TextStylePropertyName<?> textStylePropertyName(final UrlPath path) {
        return getOrFail(
            path,
            TEXT_STYLE_PROPERTY_NAME,
            TextStylePropertyName.class
        );
    }

    public <T> T getOrFail(final UrlPath path,
                           final TemplateValueName name,
                           final Class<T> type) {
        return this.get(
            path,
            name,
            type
        ).orElseThrow(() -> new IllegalArgumentException("Unknown placeholder: " + name));
    }

    public <T> Optional<T> get(final UrlPath path,
                               final TemplateValueName name,
                               final Class<T> type) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "type");

        return Cast.to(
            this.template.tryPrepareValues(path)
                .flatMap(v -> get(v, name))
        );
    }

    public Map<TemplateValueName, Object> extract(final UrlPath path) {
        Map<TemplateValueName, Object> values;
        final UrlPathTemplateValues templateValues = this.template.tryPrepareValues(path)
            .orElse(null);
        if (null != templateValues) {
            values = Maps.sorted();

            for (final TemplateValueName name : this.template.templateValueNames()) {
                final Object value = get(
                    templateValues,
                    name
                ).orElseThrow(() -> new IllegalArgumentException("Url missing " + name));
                values.put(
                    name,
                    value
                );
            }
            values = Maps.readOnly(values);
        } else {
            values = Maps.empty();
        }

        return values;
    }

    private static Optional<Object> get(final UrlPathTemplateValues values,
                                        final TemplateValueName name) {
        return values.get(
            name,
            (final String s) -> {
                final Object v;

                switch (name.value()) {
                    case "LocaleTag":
                        v = LocaleTag.parse(s);
                        break;
                    case "SpreadsheetColumnReferenceOrRange":
                        v = SpreadsheetSelection.parseColumnOrColumnRange(s);
                        break;
                    case "SpreadsheetEngineEvaluation":
                        v = SpreadsheetEngineEvaluation.valueOf(
                            CaseKind.KEBAB.change(
                                s,
                                CaseKind.SNAKE
                            )
                        );
                        break;
                    case "SpreadsheetExpressionReference":
                        v = SpreadsheetSelection.parseExpressionReference(s);
                        break;
                    case "SpreadsheetFormatterSelector":
                        v = SpreadsheetFormatterSelector.parse(s);
                        break;
                    case "SpreadsheetId":
                        v = SpreadsheetId.parse(s);
                        break;
                    case "SpreadsheetLabelName":
                        v = SpreadsheetSelection.labelName(s);
                        break;
                    case "SpreadsheetMetadataPropertyName":
                        v = SpreadsheetMetadataPropertyName.with(s);
                        break;
                    case "SpreadsheetName":
                        v = SpreadsheetName.with(s);
                        break;
                    case "SpreadsheetRowReferenceOrRange":
                        v = SpreadsheetSelection.parseRowOrRowRange(s);
                        break;
                    case "TextStylePropertyName":
                        v = TextStylePropertyName.with(s);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown placeholder: " + name);
                }
                return v;
            }
        );
    }

    /**
     * Renders this template using the provided {@link Map} as the source of values, using the well known {@link TemplateValueName}.
     */
    public UrlPath render(final Map<TemplateValueName, Object> nameToValue) {
        Objects.requireNonNull(nameToValue, "nameToValue");

        return this.template.renderPath(
            (n) -> {
                final Object value = nameToValue.get(n);
                final String stringValue;

                switch (n.value()) {
                    case "LocaleTag":
                        stringValue = value.toString();
                        break;
                    case "SpreadsheetEngineEvaluation":
                        stringValue = ((SpreadsheetEngineEvaluation) value)
                            .toLinkRelation()
                            .toUrlPathName()
                            .get()
                            .value();
                        break;
                    case "SpreadsheetColumnReferenceOrRange":
                    case "SpreadsheetExpressionReference":
                    case "SpreadsheetLabelName":
                    case "SpreadsheetRowReferenceOrRange":
                        stringValue = ((SpreadsheetSelection) value)
                            .toStringMaybeStar();
                        break;
                    case "SpreadsheetId":
                    case "SpreadsheetFormatterSelector":
                    case "SpreadsheetMetadataPropertyName":
                    case "SpreadsheetName":
                        stringValue = value.toString();
                        break;
                    case "TextStylePropertyName":
                        stringValue = ((TextStylePropertyName)value).text();
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown placeholder: " + n);
                }

                return stringValue;
            }
        );
    }

    @Override
    public void render(final Printer printer,
                       final TemplateContext context) {
        this.template.render(
            printer,
            context
        );
    }

    @Override
    public String renderToString(final LineEnding lineEnding,
                                 final TemplateContext context) {
        return this.template.renderToString(
            lineEnding,
            context
        );
    }

    @Override
    public Set<TemplateValueName> templateValueNames() {
        return this.template.templateValueNames();
    }

    private final UrlPathTemplate template;

    // Object...........................................................................................................

    @Override
    public int hashCode() {
        return this.template.hashCode();
    }

    @Override
    public boolean equals(final Object other) {
        return this == other ||
            other instanceof SpreadsheetUrlPathTemplate && this.equals0((SpreadsheetUrlPathTemplate) other);
    }

    private boolean equals0(final SpreadsheetUrlPathTemplate other) {
        return this.template.equals(other.template);
    }

    @Override
    public String toString() {
        return this.template.toString();
    }

    @Override public Object value() {
        return null;
    }

    // TreePrintable....................................................................................................

    @Override
    public void printTree(final IndentingPrinter printer) {
        printer.println(this.getClass().getSimpleName());
        printer.indent();
        {
            this.template.printTree(printer);
        }
        printer.outdent();
    }
}

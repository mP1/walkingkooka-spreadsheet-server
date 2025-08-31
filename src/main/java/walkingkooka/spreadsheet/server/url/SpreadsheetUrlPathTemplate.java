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
import walkingkooka.text.LineEnding;
import walkingkooka.text.printer.IndentingPrinter;
import walkingkooka.text.printer.Printer;
import walkingkooka.tree.text.TextStylePropertyName;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

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
            removeSlashFirstAndParse(
                LocaleTag::parse
            )
        );
    }

    public SpreadsheetColumnReferenceOrRange spreadsheetColumnReferenceOrRange(final UrlPath path) {
        return this.getOrFail(
                path,
                SPREADSHEET_COLUMN_REFERENCE_OR_RANGE,
                removeSlashFirstAndParse(
                    SpreadsheetSelection::parseColumnOrColumnRange
                )
            );
    }

    public SpreadsheetEngineEvaluation spreadsheetEngineEvaluation(final UrlPath path) {
        return this.getOrFail(
                path,
                SPREADSHEET_ENGINE_EVALUATION,
                removeSlashFirstAndParse(
                    SpreadsheetEngineEvaluation::parse
                )
            );
    }

    public SpreadsheetExpressionReference spreadsheetExpressionReference(final UrlPath path) {
        return this.getOrFail(
                path,
                SPREADSHEET_EXPRESSION_REFERENCE,
                removeSlashFirstAndParse(
                    SpreadsheetSelection::parseExpressionReference
                )
            );
    }

    public SpreadsheetFormatterSelector spreadsheetFormatterSelector(final UrlPath path) {
        return this.getOrFail(
                path,
                SPREADSHEET_FORMATTER_SELECTOR,
                removeSlashFirstAndParse(
                    SpreadsheetFormatterSelector::parse
                )
            );
    }

    public Optional<SpreadsheetId> spreadsheetId(final UrlPath path) {
        return this.get(
            path,
            SPREADSHEET_ID,
            removeSlashFirstAndParse(
                SpreadsheetId::parse
            )
        );
    }

    public Optional<SpreadsheetLabelName> spreadsheetLabelName(final UrlPath path) {
        return this.get(
            path,
            SPREADSHEET_LABEL_NAME,
            removeSlashFirstAndParse(
                SpreadsheetSelection::labelName
            )
        );
    }

    public SpreadsheetMetadataPropertyName<?> spreadsheetMetadataPropertyName(final UrlPath path) {
        return getOrFail(
            path,
            SPREADSHEET_METADATA_PROPERTY_NAME,
            removeSlashFirstAndParse(
                SpreadsheetMetadataPropertyName::with
            )
        );
    }

    public SpreadsheetName spreadsheetName(final UrlPath path) {
        return getOrFail(
            path,
            SPREADSHEET_NAME,
            removeSlashFirstAndParse(
                SpreadsheetName::with
            )
        );
    }

    public SpreadsheetRowReferenceOrRange spreadsheetRowReferenceOrRange(final UrlPath path) {
        return this.getOrFail(
            path,
            SPREADSHEET_ROW_REFERENCE_OR_RANGE,
            removeSlashFirstAndParse(
                SpreadsheetSelection::parseRowOrRowRange
            )
        );
    }

    public TextStylePropertyName<?> textStylePropertyName(final UrlPath path) {
        return getOrFail(
            path,
            TEXT_STYLE_PROPERTY_NAME,
            removeSlashFirstAndParse(
                TextStylePropertyName::with
            )
        );
    }

    private static <T> Function<String, T> removeSlashFirstAndParse(final Function<String, T> function) {
        return s -> function.apply(
            removeRootSlashIfNecessary(s)
        );
    }

    /**
     * Utility that removes the root slash from the given text if necessary.
     */
    public static String removeRootSlashIfNecessary(final String text) {
        Objects.requireNonNull(text, "text");

        return text.startsWith("/") ?
            text.substring(1) :
            text;
    }

    public <T> T getOrFail(final UrlPath path,
                           final TemplateValueName name,
                           final Function<String, T> parser) {
        return this.get(
            path,
            name,
            parser
        ).orElseThrow(() -> new IllegalArgumentException("Unknown placeholder: " + name));
    }

    public <T> Optional<T> get(final UrlPath path,
                               final TemplateValueName name,
                               final Function<String, T> parser) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(parser, "parser");

        return this.template.tryPrepareValues(path)
                .flatMap(v -> v.get(name, parser));
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

                final String without = removeRootSlashIfNecessary(s);
                
                switch (name.value()) {
                    case "LocaleTag":
                        v = LocaleTag.parse(without);
                        break;
                    case "SpreadsheetColumnReferenceOrRange":
                        v = SpreadsheetSelection.parseColumnOrColumnRange(without);
                        break;
                    case "SpreadsheetEngineEvaluation":
                        v = SpreadsheetEngineEvaluation.parse(without);
                        break;
                    case "SpreadsheetExpressionReference":
                        v = SpreadsheetSelection.parseExpressionReference(without);
                        break;
                    case "SpreadsheetFormatterSelector":
                        v = SpreadsheetFormatterSelector.parse(without);
                        break;
                    case "SpreadsheetId":
                        v = SpreadsheetId.parse(without);
                        break;
                    case "SpreadsheetLabelName":
                        v = SpreadsheetSelection.labelName(without);
                        break;
                    case "SpreadsheetMetadataPropertyName":
                        v = SpreadsheetMetadataPropertyName.with(without);
                        break;
                    case "SpreadsheetName":
                        v = SpreadsheetName.with(without);
                        break;
                    case "SpreadsheetRowReferenceOrRange":
                        v = SpreadsheetSelection.parseRowOrRowRange(without);
                        break;
                    case "TextStylePropertyName":
                        v = TextStylePropertyName.with(without);
                        break;
                    default:
                        v = s;
                        break;
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

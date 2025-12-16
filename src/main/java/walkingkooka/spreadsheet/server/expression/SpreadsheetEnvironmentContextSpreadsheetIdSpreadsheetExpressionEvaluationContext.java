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

package walkingkooka.spreadsheet.server.expression;

import walkingkooka.environment.EnvironmentContext;
import walkingkooka.environment.EnvironmentValueName;
import walkingkooka.locale.LocaleContext;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.spreadsheet.SpreadsheetContext;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.engine.SpreadsheetMetadataMode;
import walkingkooka.spreadsheet.environment.SpreadsheetEnvironmentContext;
import walkingkooka.spreadsheet.environment.SpreadsheetEnvironmentContexts;
import walkingkooka.spreadsheet.expression.SpreadsheetExpressionEvaluationContext;
import walkingkooka.spreadsheet.expression.SpreadsheetExpressionEvaluationContextDelegator;
import walkingkooka.spreadsheet.expression.SpreadsheetExpressionEvaluationContexts;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContext;
import walkingkooka.spreadsheet.provider.SpreadsheetProvider;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReferenceLoader;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReferenceLoaders;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelNameResolver;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelNameResolvers;
import walkingkooka.spreadsheet.server.SpreadsheetServerContext;
import walkingkooka.spreadsheet.value.SpreadsheetCell;
import walkingkooka.terminal.TerminalContext;
import walkingkooka.text.printer.Printer;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContextObjectPostProcessor;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;

import java.util.Objects;
import java.util.Optional;

/**
 * A {@link SpreadsheetExpressionEvaluationContext} that delegates to either {@link SpreadsheetExpressionEvaluationContexts#spreadsheetContext(SpreadsheetMetadataMode, Optional, SpreadsheetExpressionReferenceLoader, SpreadsheetLabelNameResolver, SpreadsheetContext, TerminalContext)}
 * if a {@link #SPREADSHEET_ID} is present or {@link SpreadsheetExpressionEvaluationContexts#spreadsheetEnvironmentContext(LocaleContext, SpreadsheetEnvironmentContext, TerminalContext, SpreadsheetProvider, ProviderContext)}.
 */
final class SpreadsheetEnvironmentContextSpreadsheetIdSpreadsheetExpressionEvaluationContext implements SpreadsheetExpressionEvaluationContext,
    SpreadsheetExpressionEvaluationContextDelegator {

    static SpreadsheetEnvironmentContextSpreadsheetIdSpreadsheetExpressionEvaluationContext with(final SpreadsheetEnvironmentContext spreadsheetEnvironmentContext,
                                                                                                 final SpreadsheetServerContext spreadsheetServerContext,
                                                                                                 final TerminalContext terminalContext) {
        return new SpreadsheetEnvironmentContextSpreadsheetIdSpreadsheetExpressionEvaluationContext(
            Objects.requireNonNull(spreadsheetEnvironmentContext, "spreadsheetEnvironmentContext"), // SpreadsheetEnvironmentContext
            Objects.requireNonNull(spreadsheetServerContext, "spreadsheetServerContext"),
            Objects.requireNonNull(terminalContext, "terminalContext")
        );
    }

    private SpreadsheetEnvironmentContextSpreadsheetIdSpreadsheetExpressionEvaluationContext(final SpreadsheetEnvironmentContext spreadsheetEnvironmentContext,
                                                                                             final SpreadsheetServerContext spreadsheetServerContext,
                                                                                             final TerminalContext terminalContext) {
        super();

        this.spreadsheetEnvironmentContext = spreadsheetEnvironmentContext;
        this.spreadsheetServerContext = spreadsheetServerContext;

        this.environment = SpreadsheetExpressionEvaluationContexts.spreadsheetEnvironmentContext(
            spreadsheetServerContext, // LocaleContext
            spreadsheetEnvironmentContext, // SpreadsheetEnvironmentContext
            terminalContext, // TerminalContext
            spreadsheetServerContext, // SpreadsheetProvider
            spreadsheetServerContext.providerContext() // ProviderContext
        );

        this.terminalContext = terminalContext;

        spreadsheetEnvironmentContext.addEventValueWatcher(
            (final EnvironmentValueName<?> name,
             final Optional<?> oldValue,
             final Optional<?> newValue) -> {
                this.spreadsheetExpressionEvaluationContext = null;
            }
        );
    }

    private final SpreadsheetEnvironmentContext spreadsheetEnvironmentContext;

    // SpreadsheetEnvironmentContext....................................................................................

    @Override
    public SpreadsheetFormatterContext spreadsheetFormatterContext(final Optional<SpreadsheetCell> cell) {
        Objects.requireNonNull(cell, "cell");
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetExpressionEvaluationContext cloneEnvironment() {
        final SpreadsheetEnvironmentContext before = this.spreadsheetEnvironmentContext;
        final SpreadsheetEnvironmentContext after = before.cloneEnvironment();

        return before == after ?
            this :
            new SpreadsheetEnvironmentContextSpreadsheetIdSpreadsheetExpressionEvaluationContext(
                after,
                this.spreadsheetServerContext,
                this.terminalContext
            );
    }

    @Override
    public SpreadsheetExpressionEvaluationContext setEnvironmentContext(final EnvironmentContext environmentContext) {
        final SpreadsheetEnvironmentContext before = this.spreadsheetEnvironmentContext;

        SpreadsheetExpressionEvaluationContext after;
        if (before == environmentContext) {
            after = this;
        } else {
            after = new SpreadsheetEnvironmentContextSpreadsheetIdSpreadsheetExpressionEvaluationContext(
                SpreadsheetEnvironmentContexts.basic(environmentContext),
                this.spreadsheetServerContext,
                this.terminalContext
            );
        }

        return after;
    }

    @Override
    public SpreadsheetExpressionEvaluationContext setObjectPostProcessor(final JsonNodeMarshallContextObjectPostProcessor jsonNodeMarshallContextObjectPostProcessor) {
        Objects.requireNonNull(jsonNodeMarshallContextObjectPostProcessor, "jsonNodeMarshallContextObjectPostProcessor");

        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetExpressionEvaluationContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor jsonNodeUnmarshallContextPreProcessor) {
        Objects.requireNonNull(jsonNodeUnmarshallContextPreProcessor, "jsonNodeUnmarshallContextPreProcessor");

        throw new UnsupportedOperationException();
    }

    // SpreadsheetExpressionEvaluationContextDelegator..................................................................

    @Override
    public SpreadsheetExpressionEvaluationContext spreadsheetExpressionEvaluationContext() {
        if (null == this.spreadsheetExpressionEvaluationContext) {
            final SpreadsheetEnvironmentContext spreadsheetEnvironmentContext = this.spreadsheetEnvironmentContext;

            final SpreadsheetId spreadsheetId = spreadsheetEnvironmentContext.environmentValue(SPREADSHEET_ID)
                .orElse(null);
            if (null == spreadsheetId) {
                this.spreadsheetExpressionEvaluationContext = this.environment;
            } else {
                final TerminalContext terminalContext = this.terminalContext;

                try {
                    final SpreadsheetContext spreadsheetContext = this.spreadsheetServerContext.spreadsheetContextOrFail(spreadsheetId);

                    this.spreadsheetExpressionEvaluationContext = SpreadsheetExpressionEvaluationContexts.spreadsheetContext(
                        SpreadsheetMetadataMode.SCRIPTING,
                        SpreadsheetExpressionEvaluationContext.NO_CELL,
                        SpreadsheetExpressionReferenceLoaders.basic(),
                        SpreadsheetLabelNameResolvers.labelStore(
                            spreadsheetContext.storeRepository()
                                .labels()
                        ),
                        spreadsheetContext,
                        terminalContext
                    );
                } catch (final UnsupportedOperationException rethrow) {
                    this.spreadsheetExpressionEvaluationContext = this.environment;
                    throw rethrow;
                } catch (final RuntimeException cause) {
                    final Printer error = terminalContext.error();
                    error.println(cause.getMessage()); // should stacktrace be printed
                    error.flush();

                } finally {
                    this.spreadsheetExpressionEvaluationContext = this.environment;
                }
            }
        }
        return this.spreadsheetExpressionEvaluationContext;
    }

    private transient SpreadsheetExpressionEvaluationContext spreadsheetExpressionEvaluationContext;

    private final SpreadsheetServerContext spreadsheetServerContext;

    private final TerminalContext terminalContext;

    private final SpreadsheetExpressionEvaluationContext environment;

    // Object...........................................................................................................

    @Override
    public String toString() {
        return this.spreadsheetEnvironmentContext.toString();
    }
}

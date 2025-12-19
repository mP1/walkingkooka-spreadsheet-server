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

import org.junit.jupiter.api.Test;
import walkingkooka.Cast;
import walkingkooka.ToStringTesting;
import walkingkooka.environment.EnvironmentValueName;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContexts;
import walkingkooka.spreadsheet.SpreadsheetContext;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.environment.SpreadsheetEnvironmentContext;
import walkingkooka.spreadsheet.environment.SpreadsheetEnvironmentContextFactory;
import walkingkooka.spreadsheet.environment.SpreadsheetEnvironmentContexts;
import walkingkooka.spreadsheet.expression.SpreadsheetExpressionEvaluationContext;
import walkingkooka.spreadsheet.expression.SpreadsheetExpressionEvaluationContextTesting;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataContexts;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStores;
import walkingkooka.spreadsheet.server.SpreadsheetServerContexts;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepositories;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.terminal.TerminalContexts;

import java.math.MathContext;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetEnvironmentContextSpreadsheetIdSpreadsheetExpressionEvaluationContextTest implements SpreadsheetExpressionEvaluationContextTesting<SpreadsheetEnvironmentContextSpreadsheetIdSpreadsheetExpressionEvaluationContext>,
    ToStringTesting<SpreadsheetEnvironmentContextSpreadsheetIdSpreadsheetExpressionEvaluationContext>,
    SpreadsheetMetadataTesting {

    private final static SpreadsheetId SPREADSHEET_ID = SpreadsheetId.with(1);

    static {
        SpreadsheetEnvironmentContext context = SPREADSHEET_ENVIRONMENT_CONTEXT.cloneEnvironment();

        for (final EnvironmentValueName<?> name : SpreadsheetEnvironmentContextFactory.ENVIRONMENT_VALUE_NAMES) {
            if (name.equals(SpreadsheetExpressionEvaluationContext.CONVERTER)) {
                continue;
            }

            context = context.setEnvironmentValue(
                name,
                Cast.to(
                    METADATA_EN_AU.getOrFail(
                        SpreadsheetMetadataPropertyName.fromEnvironmentValueName(name)
                    )
                )
            );
        }

        SPREADSHEET_ENVIRONMENT_CONTEXT2 = context.setEnvironmentValue(
            SpreadsheetEnvironmentContextFactory.CONVERTER,
            METADATA_EN_AU.getOrFail(
                SpreadsheetMetadataPropertyName.VALIDATION_CONVERTER
            )
        ).setEnvironmentValue(
            SpreadsheetEnvironmentContextFactory.DECIMAL_NUMBER_DIGIT_COUNT,
            DECIMAL_NUMBER_DIGIT_COUNT
        ).setEnvironmentValue(
            SpreadsheetMetadataPropertyName.SPREADSHEET_ID.toEnvironmentValueName(),
            SPREADSHEET_ID
        );
    }

    private final static SpreadsheetEnvironmentContext SPREADSHEET_ENVIRONMENT_CONTEXT2;

    // with.............................................................................................................

    @Test
    public void testWithNullSpreadsheetEnvironmentContextFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetEnvironmentContextSpreadsheetIdSpreadsheetExpressionEvaluationContext.with(
                null,
                SpreadsheetServerContexts.fake(),
                TerminalContexts.fake()
            )
        );
    }

    @Test
    public void testWithNullSpreadsheetServerContextFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetEnvironmentContextSpreadsheetIdSpreadsheetExpressionEvaluationContext.with(
                SpreadsheetEnvironmentContexts.fake(),
                null,
                TerminalContexts.fake()
            )
        );
    }

    @Test
    public void testWithNullTerminalContextFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetEnvironmentContextSpreadsheetIdSpreadsheetExpressionEvaluationContext.with(
                SpreadsheetEnvironmentContexts.fake(),
                SpreadsheetServerContexts.fake(),
                null
            )
        );
    }

    // SpreadsheetExpressionEvaluationContext...........................................................................

    @Override
    public void testEvaluateExpressionUnknownFunctionNameFails() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testSetCellWithSame() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testSetSpreadsheetMetadataWithDifferentIdFails() {
        throw new UnsupportedOperationException();
    }

    @Test
    public void testRemoveSpreadsheetIdAndEvaluate() {
        final SpreadsheetEnvironmentContextSpreadsheetIdSpreadsheetExpressionEvaluationContext context = this.createContext();
        context.removeEnvironmentValue(SpreadsheetExpressionEvaluationContext.SPREADSHEET_ID);

        this.evaluateAndCheck(
            context,
            "=1+2",
            EXPRESSION_NUMBER_KIND.create(1+2)
        );
    }

    @Test
    public void testSetSpreadsheetIdAndEvaluate() {
        final SpreadsheetEnvironmentContextSpreadsheetIdSpreadsheetExpressionEvaluationContext context = this.createContext();
        context.setEnvironmentValue(
            SpreadsheetExpressionEvaluationContext.SPREADSHEET_ID,
            SPREADSHEET_ID
        );

        this.evaluateAndCheck(
            context,
            "=1+2+A1",
            EXPRESSION_NUMBER_KIND.create(1+2)
        );
    }

    @Test
    public void testSetSpreadsheetIdRemoveSpreadsheetIdAndEvaluate() {
        final SpreadsheetEnvironmentContextSpreadsheetIdSpreadsheetExpressionEvaluationContext context = this.createContext();
        context.setSpreadsheetId(SPREADSHEET_ID);

    }

    @Override
    public SpreadsheetEnvironmentContextSpreadsheetIdSpreadsheetExpressionEvaluationContext createContext() {
        final SpreadsheetMetadataStore metadataStore = SpreadsheetMetadataStores.treeMap();

        final SpreadsheetStoreRepository repo = SpreadsheetStoreRepositories.treeMap(metadataStore);

        return SpreadsheetEnvironmentContextSpreadsheetIdSpreadsheetExpressionEvaluationContext.with(
            SPREADSHEET_ENVIRONMENT_CONTEXT2.cloneEnvironment(),
            SpreadsheetServerContexts.basic(
                (SpreadsheetId id) -> {
                    checkEquals(SPREADSHEET_ID, id);
                    return repo;
                },
                SPREADSHEET_PROVIDER,
                (SpreadsheetContext c) -> {
                    throw new UnsupportedOperationException();
                },
                SPREADSHEET_ENVIRONMENT_CONTEXT, // global context
                LOCALE_CONTEXT,
                SpreadsheetMetadataContexts.fake(),
                HateosResourceHandlerContexts.fake(),
                PROVIDER_CONTEXT,
                TERMINAL_SERVER_CONTEXT
            ),
            TERMINAL_CONTEXT
        );
    }

    @Override
    public int decimalNumberDigitCount() {
        return DECIMAL_NUMBER_DIGIT_COUNT;
    }

    @Override
    public MathContext mathContext() {
        return SpreadsheetMetadataTesting.METADATA_EN_AU.mathContext();
    }

    @Override
    public String currencySymbol() {
        return DECIMAL_NUMBER_SYMBOLS.currencySymbol();
    }

    @Override
    public char decimalSeparator() {
        return DECIMAL_NUMBER_SYMBOLS.decimalSeparator();
    }

    @Override
    public String exponentSymbol() {
        return DECIMAL_NUMBER_SYMBOLS.exponentSymbol();
    }

    @Override
    public char groupSeparator() {
        return DECIMAL_NUMBER_SYMBOLS.groupSeparator();
    }

    @Override
    public String infinitySymbol() {
        return DECIMAL_NUMBER_SYMBOLS.infinitySymbol();
    }

    @Override
    public char monetaryDecimalSeparator() {
        return DECIMAL_NUMBER_SYMBOLS.monetaryDecimalSeparator();
    }

    @Override
    public String nanSymbol() {
        return DECIMAL_NUMBER_SYMBOLS.nanSymbol();
    }

    @Override
    public char percentSymbol() {
        return DECIMAL_NUMBER_SYMBOLS.percentSymbol();
    }

    @Override
    public char permillSymbol() {
        return DECIMAL_NUMBER_SYMBOLS.permillSymbol();
    }

    @Override
    public char negativeSign() {
        return DECIMAL_NUMBER_SYMBOLS.negativeSign();
    }

    @Override
    public char positiveSign() {
        return DECIMAL_NUMBER_SYMBOLS.positiveSign();
    }

    @Override
    public char zeroDigit() {
        return DECIMAL_NUMBER_SYMBOLS.zeroDigit();
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(
            this.createContext(),
            SPREADSHEET_ENVIRONMENT_CONTEXT2.toString()
        );
    }

    // class............................................................................................................

    @Override
    public Class<SpreadsheetEnvironmentContextSpreadsheetIdSpreadsheetExpressionEvaluationContext> type() {
        return SpreadsheetEnvironmentContextSpreadsheetIdSpreadsheetExpressionEvaluationContext.class;
    }
}

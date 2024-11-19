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

package walkingkooka.spreadsheet.server;

import walkingkooka.net.header.MediaType;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContextDelegator;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContext;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContextDelegator;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProvider;
import walkingkooka.spreadsheet.provider.SpreadsheetProvider;
import walkingkooka.spreadsheet.provider.SpreadsheetProviderDelegator;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.json.marshall.JsonNodeMarshallUnmarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallUnmarshallContextDelegator;

import java.math.MathContext;
import java.time.LocalDateTime;
import java.util.Objects;

final class BasicSpreadsheetHateosResourceHandlerContext implements SpreadsheetHateosResourceHandlerContext,
        SpreadsheetEngineContextDelegator,
        SpreadsheetFormatterContextDelegator,
        SpreadsheetProviderDelegator,
        JsonNodeMarshallUnmarshallContextDelegator {

    static BasicSpreadsheetHateosResourceHandlerContext with(final JsonNodeMarshallUnmarshallContext marshallUnmarshallContext,
                                                             final SpreadsheetEngineContext engineContext,
                                                             final SpreadsheetFormatterContext formatterContext,
                                                             final SpreadsheetProvider systemSpreadsheetProvider) {
        return new BasicSpreadsheetHateosResourceHandlerContext(
                Objects.requireNonNull(marshallUnmarshallContext, "marshallUnmarshallContext"),
                Objects.requireNonNull(engineContext, "engineContext"),
                Objects.requireNonNull(formatterContext, "formatterContext"),
                Objects.requireNonNull(systemSpreadsheetProvider, "systemSpreadsheetProvider")
        );
    }

    private BasicSpreadsheetHateosResourceHandlerContext(final JsonNodeMarshallUnmarshallContext marshallUnmarshallContext,
                                                         final SpreadsheetEngineContext engineContext,
                                                         final SpreadsheetFormatterContext formatterContext,
                                                         final SpreadsheetProvider systemSpreadsheetProvider) {
        this.marshallUnmarshallContext = marshallUnmarshallContext;
        this.engineContext = engineContext;
        this.formatterContext = formatterContext;
        this.systemSpreadsheetProvider = systemSpreadsheetProvider;
    }

    @Override
    public MediaType contentType() {
        return MediaType.APPLICATION_JSON;
    }

    // 4 methods immediately below are required due to clashes between XXXDelegators

    @Override
    public ExpressionNumberKind expressionNumberKind() {
        return this.marshallUnmarshallContext.expressionNumberKind();
    }

    @Override
    public MathContext mathContext() {
        return this.formatterContext.mathContext();
    }

    @Override
    public LocalDateTime now() {
        return this.engineContext.now();
    }

    @Override
    public SpreadsheetSelection resolveLabel(final SpreadsheetLabelName labelName) {
        return this.engineContext.resolveLabel(labelName);
    }

    // JsonNodeMarshallUnmarshallContext................................................................................

    @Override
    public JsonNodeMarshallUnmarshallContext jsonNodeMarshallUnmarshallContext() {
        return this.marshallUnmarshallContext;
    }

    private final JsonNodeMarshallUnmarshallContext marshallUnmarshallContext;

    // SpreadsheetEngineContextDelegator................................................................................

    @Override
    public SpreadsheetEngineContext spreadsheetEngineContext() {
        return this.engineContext;
    }

    // SpreadsheetFormatterProvider.....................................................................................

    @Override
    public SpreadsheetFormatterProvider spreadsheetFormatterProvider() {
        return this.engineContext;
    }

    // SpreadsheetProvider..............................................................................................

    @Override
    public SpreadsheetProvider spreadsheetProvider() {
        return this.engineContext;
    }

    private final SpreadsheetEngineContext engineContext;

    // SpreadsheetFormatterContext......................................................................................

    @Override
    public SpreadsheetFormatterContext spreadsheetFormatterContext() {
        return this.formatterContext;
    }

    private final SpreadsheetFormatterContext formatterContext;

    // SpreadsheetHateosResourceHandlerContext..........................................................................

    @Override
    public SpreadsheetProvider systemSpreadsheetProvider() {
        return this.systemSpreadsheetProvider;
    }

    private final SpreadsheetProvider systemSpreadsheetProvider;
}

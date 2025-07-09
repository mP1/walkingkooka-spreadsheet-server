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

import walkingkooka.net.UrlPath;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleAll;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleNone;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleRange;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.validation.form.Form;
import walkingkooka.validation.form.FormName;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A {@link HateosResourceHandler} that calls {@link SpreadsheetEngine#saveForm(Form, SpreadsheetEngineContext)}.
 */
final class SpreadsheetDeltaHateosResourceHandlerSaveForm extends SpreadsheetDeltaHateosResourceHandler<FormName>
    implements UnsupportedHateosResourceHandlerHandleAll<FormName, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleNone<FormName, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleRange<FormName, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetEngineHateosResourceHandlerContext> {

    static SpreadsheetDeltaHateosResourceHandlerSaveForm with(final SpreadsheetEngine engine) {
        return new SpreadsheetDeltaHateosResourceHandlerSaveForm(engine);
    }

    private SpreadsheetDeltaHateosResourceHandlerSaveForm(final SpreadsheetEngine engine) {
        super(engine);
    }

    // handleOne........................................................................................................

    @Override
    public Optional<SpreadsheetDelta> handleOne(final FormName name,
                                                final Optional<SpreadsheetDelta> resource,
                                                final Map<HttpRequestAttribute<?>, Object> parameters,
                                                final UrlPath path,
                                                final SpreadsheetEngineHateosResourceHandlerContext context) {
        checkFormName(name);
        
        final SpreadsheetDelta delta = HateosResourceHandler.checkResourceNotEmpty(resource);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkPathEmpty(path);
        HateosResourceHandler.checkContext(context);

        final Set<Form<SpreadsheetExpressionReference>> forms = delta.forms();
        final int count = forms.size();

        final Form<SpreadsheetExpressionReference> form;

        switch (count) {
            case 0:
                throw new IllegalArgumentException("Missing form");
            case 1:
                form = forms.iterator()
                    .next();
                break;
            default:
                throw new IllegalArgumentException("Got " + count + " forms expected 1");
        }

        return Optional.of(
            this.engine.saveForm(
                form,
                context
            )
        );
    }

    @Override
    String operation() {
        return "saveForm";
    }
}

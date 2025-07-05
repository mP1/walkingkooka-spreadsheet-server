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

import org.junit.jupiter.api.Test;
import walkingkooka.collect.Range;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.UrlPath;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.validation.form.SpreadsheetForms;
import walkingkooka.spreadsheet.validation.form.store.SpreadsheetFormStore;
import walkingkooka.spreadsheet.validation.form.store.SpreadsheetFormStores;
import walkingkooka.validation.form.Form;
import walkingkooka.validation.form.FormName;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class SpreadsheetDeltaHateosResourceHandlerSaveFormTest extends SpreadsheetDeltaHateosResourceHandlerFormTestCase<SpreadsheetDeltaHateosResourceHandlerSaveForm> {

    @Test
    public void testHandleOneSave() {
        final FormName formName = this.id();
        final Form<SpreadsheetExpressionReference> form = SpreadsheetForms.form(formName)
            .setFields(
                Lists.of(
                    SpreadsheetForms.field(SpreadsheetSelection.A1.toExpressionReference())
                        .setLabel("FieldLabel1")
                )
            );
        final SpreadsheetFormStore store = SpreadsheetFormStores.treeMap();

        this.handleOneAndCheck(
            formName,
            Optional.of(
                SpreadsheetDelta.EMPTY.setForms(
                    Sets.of(form)
                )
            ),
            HateosResourceHandler.NO_PARAMETERS,
            UrlPath.EMPTY,
            this.context(store),
            Optional.of(
                SpreadsheetDelta.EMPTY.setForms(
                    Sets.of(form)
                )
            )
        );
    }

    @Override
    SpreadsheetDeltaHateosResourceHandlerSaveForm createHandler(final SpreadsheetEngine engine) {
        return SpreadsheetDeltaHateosResourceHandlerSaveForm.with(engine);
    }

    @Override
    SpreadsheetEngine engine() {
        return new FakeSpreadsheetEngine() {

            @Override
            public SpreadsheetDelta saveForm(final Form<SpreadsheetExpressionReference> form,
                                             final SpreadsheetEngineContext context) {
                return SpreadsheetDelta.EMPTY.setForms(
                    Sets.of(
                        context.storeRepository()
                            .forms()
                            .save(form)
                    )
                );
            }
        };
    }

    @Override
    public FormName id() {
        return FormName.with("Form123");
    }

    @Override
    public Set<FormName> manyIds() {
        return Sets.of(this.id());
    }

    @Override
    public Range<FormName> range() {
        return Range.all();
    }

    @Override
    public Optional<SpreadsheetDelta> resource() {
        return Optional.of(
            SpreadsheetDelta.EMPTY.setForms(
                Sets.of(
                    SpreadsheetForms.form(
                        FormName.with("Form123")
                    )
                )
            )
        );
    }

    @Override
    public Optional<SpreadsheetDelta> collectionResource() {
        return Optional.empty();
    }

    @Override
    public Map<HttpRequestAttribute<?>, Object> parameters() {
        return HateosResourceHandler.NO_PARAMETERS;
    }

    @Override
    public UrlPath path() {
        return UrlPath.EMPTY;
    }

    // Class............................................................................................................

    @Override
    public Class<SpreadsheetDeltaHateosResourceHandlerSaveForm> type() {
        return SpreadsheetDeltaHateosResourceHandlerSaveForm.class;
    }
}

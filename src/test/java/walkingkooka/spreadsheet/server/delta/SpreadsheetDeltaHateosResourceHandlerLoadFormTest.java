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
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.collect.set.SortedSets;
import walkingkooka.net.UrlParameterName;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.validation.form.store.SpreadsheetFormStore;
import walkingkooka.spreadsheet.validation.form.store.SpreadsheetFormStores;
import walkingkooka.validation.form.Form;
import walkingkooka.validation.form.FormField;
import walkingkooka.validation.form.FormName;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;

public final class SpreadsheetDeltaHateosResourceHandlerLoadFormTest extends SpreadsheetDeltaHateosResourceHandlerFormTestCase<SpreadsheetDeltaHateosResourceHandlerLoadForm> {

    private final static int DEFAULT_COUNT = 3;

    @Test
    public void testHandleOneLoad() {
        final FormName formName = this.id();
        final Form<SpreadsheetExpressionReference> form = Form.<SpreadsheetExpressionReference>with(formName)
            .setFields(
                Lists.of(
                    FormField.with(SpreadsheetSelection.A1.toExpressionReference())
                        .setLabel("FieldLabel1")
                )
            );
        final SpreadsheetFormStore store = SpreadsheetFormStores.treeMap();
        store.save(form);

        this.handleOneAndCheck(
            formName,
            Optional.empty(),
            HateosResourceHandler.NO_PARAMETERS,
            this.context(store),
            Optional.of(
                SpreadsheetDelta.EMPTY.setForms(
                    Sets.of(form)
                )
            )
        );
    }

    @Test
    public void testHandleAll() {
        final SpreadsheetFormStore store = SpreadsheetFormStores.treeMap();

        final Form<SpreadsheetExpressionReference> form1 = Form.<SpreadsheetExpressionReference>with(FormName.with("Form1"))
            .setFields(
                Lists.of(
                    FormField.with(SpreadsheetSelection.A1.toExpressionReference())
                        .setLabel("FieldLabel1")
                )
            );
        store.save(form1);

        final Form<SpreadsheetExpressionReference> form2 = Form.<SpreadsheetExpressionReference>with(FormName.with("Form2"))
            .setFields(
                Lists.of(
                    FormField.with(SpreadsheetSelection.parseCell("B2").toExpressionReference())
                        .setLabel("FieldLabel2")
                )
            );
        store.save(form2);

        this.handleAllAndCheck(
            Optional.empty(),
            HateosResourceHandler.NO_PARAMETERS,
            this.context(store),
            Optional.of(
                SpreadsheetDelta.EMPTY.setForms(
                    Sets.of(
                        form1,
                        form2
                    )
                )
            )
        );
    }

    @Test
    public void testHandleAllWithOffsetAndCount() {
        final SpreadsheetFormStore store = SpreadsheetFormStores.treeMap();

        final Form<SpreadsheetExpressionReference> form1 = Form.<SpreadsheetExpressionReference>with(FormName.with("Form1"))
            .setFields(
                Lists.of(
                    FormField.with(SpreadsheetSelection.A1.toExpressionReference())
                        .setLabel("FieldLabel1")
                )
            );
        store.save(form1);

        final Form<SpreadsheetExpressionReference> form2 = Form.<SpreadsheetExpressionReference>with(FormName.with("Form2"))
            .setFields(
                Lists.of(
                    FormField.with(SpreadsheetSelection.parseCell("B2").toExpressionReference())
                        .setLabel("FieldLabel2")
                )
            );
        store.save(form2);

        final Form<SpreadsheetExpressionReference> form3 = Form.<SpreadsheetExpressionReference>with(FormName.with("Form3"))
            .setFields(
                Lists.of(
                    FormField.with(SpreadsheetSelection.parseCell("C3").toExpressionReference())
                        .setLabel("FieldLabel3")
                )
            );
        store.save(form3);

        final Form<SpreadsheetExpressionReference> form4 = Form.<SpreadsheetExpressionReference>with(FormName.with("Form4"))
            .setFields(
                Lists.of(
                    FormField.with(SpreadsheetSelection.parseCell("D4").toExpressionReference())
                        .setLabel("FieldLabel4")
                )
            );
        store.save(form4);

        this.handleAllAndCheck(
            Optional.empty(),
            HateosResourceHandler.NO_PARAMETERS,
            this.context(store),
            Optional.of(
                SpreadsheetDelta.EMPTY.setForms(
                    Sets.of(
                        form1,
                        form2,
                        form3
                    )
                )
            )
        );

        this.handleAllAndCheck(
            Optional.empty(),
            Maps.of(
                UrlParameterName.with("offset"), Lists.of("1"),
                UrlParameterName.with("count"), Lists.of("2")
            ),
            this.context(store),
            Optional.of(
                SpreadsheetDelta.EMPTY.setForms(
                    Sets.of(
                        form2,
                        form3
                    )
                )
            )
        );
    }

    @Override
    SpreadsheetDeltaHateosResourceHandlerLoadForm createHandler(final SpreadsheetEngine engine) {
        return SpreadsheetDeltaHateosResourceHandlerLoadForm.with(
            DEFAULT_COUNT,
            engine
        );
    }

    @Override
    SpreadsheetEngine engine() {
        return new FakeSpreadsheetEngine() {

            @Override
            public SpreadsheetDelta loadForm(final FormName name,
                                             final Optional<SpreadsheetExpressionReference> formSelection,
                                             final SpreadsheetEngineContext context) {
                return SpreadsheetDelta.EMPTY.setForms(
                    context.storeRepository()
                        .forms()
                        .load(name)
                        .map(Sets::of)
                        .orElse(Sets.empty())
                );
            }

            @Override
            public SpreadsheetDelta loadForms(final int offset,
                                              final int count,
                                              final SpreadsheetEngineContext context) {
                final SortedSet<Form<SpreadsheetExpressionReference>> forms = SortedSets.tree(Form.nameComparator());
                forms.addAll(
                    context.storeRepository()
                        .forms()
                        .values(
                            offset,
                            count
                        )
                );

                return SpreadsheetDelta.EMPTY.setForms(forms);
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
        return Optional.empty();
    }

    @Override
    public Optional<SpreadsheetDelta> collectionResource() {
        return Optional.empty();
    }

    @Override
    public Map<HttpRequestAttribute<?>, Object> parameters() {
        return Maps.empty();
    }

    // ClassTesting......................................................................................................

    @Override
    public Class<SpreadsheetDeltaHateosResourceHandlerLoadForm> type() {
        return SpreadsheetDeltaHateosResourceHandlerLoadForm.class;
    }
}

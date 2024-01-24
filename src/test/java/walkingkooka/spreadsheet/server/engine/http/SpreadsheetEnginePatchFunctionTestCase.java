
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

package walkingkooka.spreadsheet.server.engine.http;

import org.junit.jupiter.api.Test;
import walkingkooka.ToStringTesting;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequests;
import walkingkooka.reflect.ClassTesting;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetViewport;
import walkingkooka.spreadsheet.store.FakeSpreadsheetLabelStore;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.store.repo.FakeSpreadsheetStoreRepository;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContexts;
import walkingkooka.util.FunctionTesting;

import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class SpreadsheetEnginePatchFunctionTestCase<P extends SpreadsheetEnginePatchFunction<R>, R extends SpreadsheetSelection> implements ClassTesting<P>,
        FunctionTesting<P, JsonNode, JsonNode>,
        ToStringTesting<P> {

    SpreadsheetEnginePatchFunctionTestCase() {
        super();
    }

    final static double WIDTH = 1000;

    final static double HEIGHT = 600;

    final static SpreadsheetLabelName LABELB2 = SpreadsheetSelection.labelName("LabelB2");
    final static SpreadsheetCellReference CELL_REFERENCE_B2 = SpreadsheetSelection.parseCell("B2");

    final static HttpRequest REQUEST = HttpRequests.fake();
    final static SpreadsheetEngine ENGINE = new FakeSpreadsheetEngine() {
        @Override
        public Optional<SpreadsheetViewport> navigate(final SpreadsheetViewport viewport,
                                                      final SpreadsheetEngineContext context) {
            return Optional.of(viewport);
        }
    };
    final static SpreadsheetEngineContext CONTEXT = new FakeSpreadsheetEngineContext() {

        @Override
        public SpreadsheetSelection resolveIfLabel(final SpreadsheetSelection selection) {
            SpreadsheetSelection resolved = selection;
            if (selection.isLabelName()) {
                if (!LABELB2.equals(selection)) {
                    throw new IllegalArgumentException("Unknown label " + selection);
                }
                resolved = CELL_REFERENCE_B2;
            }
            return resolved;
        }

        @Override
        public SpreadsheetMetadata spreadsheetMetadata() {
            return SpreadsheetMetadata.NON_LOCALE_DEFAULTS
                    .set(SpreadsheetMetadataPropertyName.LOCALE, Locale.forLanguageTag("EN-AU"))
                    .loadFromLocale();
        }

        @Override
        public SpreadsheetStoreRepository storeRepository() {
            return new FakeSpreadsheetStoreRepository() {
                @Override
                public SpreadsheetLabelStore labels() {
                    return new FakeSpreadsheetLabelStore() {
                        @Override
                        public Optional<SpreadsheetLabelMapping> load(final SpreadsheetLabelName label) {
                            return Optional.ofNullable(
                                    LABELB2.equals(label) ?
                                            LABELB2.mapping(CELL_REFERENCE_B2) :
                                            null
                            );
                        }
                    };
                }
            };
        }
    };

    @Test
    public final void testWithNullRequestFails() {
        assertThrows(
                NullPointerException.class, () ->
                        this.createFunction(
                                null,
                                ENGINE,
                                CONTEXT
                        )
        );
    }

    @Test
    public final void testWithNullEngineFails() {
        assertThrows(
                NullPointerException.class, () ->
                        this.createFunction(
                                REQUEST,
                                null,
                                CONTEXT
                        )
        );
    }

    @Test
    public final void testWithNullContextFails() {
        assertThrows(
                NullPointerException.class, () ->
                        this.createFunction(
                                REQUEST,
                                ENGINE,
                                null
                        )
        );
    }

    final JsonNode marshall(final Object object) {
        return JsonNodeMarshallContexts.basic()
                .marshall(object);
    }

    final JsonNode marshallWithType(final Object object) {
        return JsonNodeMarshallContexts.basic()
                .marshallWithType(object);
    }

    @Override
    public P createFunction() {
        return this.createFunction(
                REQUEST,
                ENGINE,
                CONTEXT
        );
    }

    abstract P createFunction(final HttpRequest request,
                              final SpreadsheetEngine engine,
                              final SpreadsheetEngineContext context);

    @Override
    public final JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }

    @Override
    public final String typeNamePrefix() {
        return SpreadsheetEnginePatchFunction.class.getSimpleName();
    }

    @Override
    public final String typeNameSuffix() {
        return "";
    }
}

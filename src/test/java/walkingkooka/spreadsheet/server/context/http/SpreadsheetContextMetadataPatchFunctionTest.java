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

package walkingkooka.spreadsheet.server.context.http;

import org.junit.jupiter.api.Test;
import walkingkooka.ToStringTesting;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.reflect.ClassTesting;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStores;
import walkingkooka.spreadsheet.server.context.FakeSpreadsheetContext;
import walkingkooka.spreadsheet.server.context.SpreadsheetContext;
import walkingkooka.spreadsheet.server.context.SpreadsheetContexts;
import walkingkooka.spreadsheet.store.repo.FakeSpreadsheetStoreRepository;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.store.LoadStoreException;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.JsonPropertyName;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.util.FunctionTesting;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetContextMetadataPatchFunctionTest implements FunctionTesting<SpreadsheetContextMetadataPatchFunction, JsonNode, JsonNode>,
        ClassTesting<SpreadsheetContextMetadataPatchFunction>,
        ToStringTesting<SpreadsheetContextMetadataPatchFunction> {

    private final static SpreadsheetId ID = SpreadsheetId.with(123);

    private final static SpreadsheetContext CONTEXT = SpreadsheetContexts.fake();

    @Test
    public void testWithNullIdFails() {
        assertThrows(NullPointerException.class, () -> SpreadsheetContextMetadataPatchFunction.with(null, CONTEXT));
    }

    @Test
    public void testWithNullContextFails() {
        assertThrows(NullPointerException.class, () -> SpreadsheetContextMetadataPatchFunction.with(ID, null));
    }

    @Test
    public void testApplyLoadFails() {
        final SpreadsheetMetadataStore store = SpreadsheetMetadataStores.treeMap();

        final SpreadsheetContext context = new FakeSpreadsheetContext() {
            @Override
            public SpreadsheetStoreRepository storeRepository(final SpreadsheetId id) {
                assertEquals(ID, id, "id");

                return new FakeSpreadsheetStoreRepository() {
                    @Override
                    public SpreadsheetMetadataStore metadatas() {
                        return store;
                    }

                    @Override
                    public String toString() {
                        return this.metadatas().toString();
                    }
                };
            }
        };

        final LoadStoreException thrown = assertThrows(
                LoadStoreException.class,
                () -> {
                    SpreadsheetContextMetadataPatchFunction.with(ID, context)
                            .apply(
                                    JsonNode.object()
                                            .set(
                                                    JsonPropertyName.with(SpreadsheetMetadataPropertyName.CURRENCY_SYMBOL.value()),
                                                    JsonNode.string("NSWD")
                                            )
                            );
                }
        );
        assertEquals("Unable to load spreadsheet with id=7b", thrown.getMessage());
    }

    @Test
    public void testApply() {
        final SpreadsheetMetadataStore store = SpreadsheetMetadataStores.treeMap();

        final SpreadsheetMetadata metadata = store.save(
                SpreadsheetMetadata.EMPTY
                        .set(SpreadsheetMetadataPropertyName.CREATOR, EmailAddress.parse("creator@example.com"))
                        .set(SpreadsheetMetadataPropertyName.CREATE_DATE_TIME, LocalDateTime.of(1999, 12, 31, 12, 58, 59))
                        .set(SpreadsheetMetadataPropertyName.MODIFIED_BY, EmailAddress.parse("creator@example.com"))
                        .set(SpreadsheetMetadataPropertyName.MODIFIED_DATE_TIME, LocalDateTime.of(2021, 10, 10, 17, 3, 0))
                        .set(SpreadsheetMetadataPropertyName.LOCALE, Locale.forLanguageTag("EN-Au"))
                        .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, ID)
                        .set(SpreadsheetMetadataPropertyName.EXPRESSION_NUMBER_KIND, ExpressionNumberKind.BIG_DECIMAL)
                        .set(SpreadsheetMetadataPropertyName.PRECISION, 0)
                        .set(SpreadsheetMetadataPropertyName.ROUNDING_MODE, RoundingMode.HALF_UP)
                        .loadFromLocale()
        );

        final SpreadsheetContext context = new FakeSpreadsheetContext() {
            @Override
            public SpreadsheetStoreRepository storeRepository(final SpreadsheetId id) {
                assertEquals(ID, id, "id");

                return new FakeSpreadsheetStoreRepository() {
                    @Override
                    public SpreadsheetMetadataStore metadatas() {
                        return store;
                    }

                    @Override
                    public String toString() {
                        return this.metadatas().toString();
                    }
                };
            }
        };

        final SpreadsheetMetadataPropertyName<String> propertyName = SpreadsheetMetadataPropertyName.CURRENCY_SYMBOL;
        final String propertyValue = "NSWD";

        final JsonNodeMarshallContext marshallContext = metadata.jsonNodeMarshallContext();

        this.applyAndCheck(
                SpreadsheetContextMetadataPatchFunction.with(ID, context),
                JsonNode.object()
                        .set(
                                JsonPropertyName.with(propertyName.value()),
                                marshallContext.marshall(propertyValue)
                        ),
                marshallContext.marshall(metadata.set(propertyName, propertyValue))
        );
    }

    @Test
    public void testToString() {
        this.toStringAndCheck(this.createFunction(), ID + " " + CONTEXT);
    }

    @Override
    public SpreadsheetContextMetadataPatchFunction createFunction() {
        return SpreadsheetContextMetadataPatchFunction.with(ID, CONTEXT);
    }

    @Override
    public Class<SpreadsheetContextMetadataPatchFunction> type() {
        return SpreadsheetContextMetadataPatchFunction.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}

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

package walkingkooka.spreadsheet.server.meta;

import org.junit.jupiter.api.Test;
import walkingkooka.ToStringTesting;
import walkingkooka.environment.AuditInfo;
import walkingkooka.locale.LocaleContexts;
import walkingkooka.reflect.ClassTesting;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.FakeSpreadsheetContext;
import walkingkooka.spreadsheet.SpreadsheetContext;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.meta.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStores;
import walkingkooka.store.MissingStoreException;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.JsonPropertyName;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.util.FunctionTesting;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetMetadataPatchFunctionTest implements FunctionTesting<SpreadsheetMetadataPatchFunction, JsonNode, JsonNode>,
    ClassTesting<SpreadsheetMetadataPatchFunction>,
    ToStringTesting<SpreadsheetMetadataPatchFunction>,
    SpreadsheetMetadataTesting {

    private final static SpreadsheetId ID = SpreadsheetId.with(123);

    private final static SpreadsheetMetadataHateosResourceHandlerContext CONTEXT = SpreadsheetMetadataHateosResourceHandlerContexts.fake();

    @Test
    public void testWithNullIdFails() {
        assertThrows(NullPointerException.class, () -> SpreadsheetMetadataPatchFunction.with(null, CONTEXT));
    }

    @Test
    public void testWithNullContextFails() {
        assertThrows(NullPointerException.class, () -> SpreadsheetMetadataPatchFunction.with(ID, null));
    }

    @Test
    public void testApplyLoadFails() {
        final SpreadsheetMetadataStore store = SpreadsheetMetadataStores.treeMap();

        final SpreadsheetMetadataHateosResourceHandlerContext context = new FakeSpreadsheetMetadataHateosResourceHandlerContext() {

            @Override
            public Optional<SpreadsheetContext> spreadsheetContext(final SpreadsheetId id) {
                return Optional.ofNullable(
                    ID.equals(id) ?
                        new FakeSpreadsheetContext() {

                            @Override
                            public SpreadsheetEngineContext spreadsheetEngineContext() {
                                return new FakeSpreadsheetEngineContext() {
                                    @Override
                                    public Optional<SpreadsheetMetadata> loadMetadata(final SpreadsheetId id) {
                                        return store.load(id);
                                    }
                                };
                            }
                        } :
                        null
                );
            }
        };

        final MissingStoreException thrown = assertThrows(
            MissingStoreException.class,
            () -> SpreadsheetMetadataPatchFunction.with(ID, context)
                .apply(
                    JsonNode.object()
                        .set(
                            JsonPropertyName.with(SpreadsheetMetadataPropertyName.ROUNDING_MODE.value()),
                            RoundingMode.HALF_DOWN.name()
                        )
                )

        );
        this.checkEquals(
            "Unable to load spreadsheet with id=7b",
            thrown.getMessage()
        );
    }

    @Test
    public void testApply() {
        final SpreadsheetMetadataStore store = SpreadsheetMetadataStores.treeMap();

        final Locale locale = Locale.forLanguageTag("EN-AU");

        final SpreadsheetMetadata metadata = store.save(
            SpreadsheetMetadata.EMPTY
                .set(
                    SpreadsheetMetadataPropertyName.AUDIT_INFO,
                    AuditInfo.with(
                        USER,
                        LocalDateTime.of(1999, 12, 31, 12, 58, 59),
                        USER,
                        LocalDateTime.of(2021, 10, 10, 17, 3, 0)
                    )
                ).set(SpreadsheetMetadataPropertyName.LOCALE, locale)
                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, ID)
                .set(SpreadsheetMetadataPropertyName.EXPRESSION_NUMBER_KIND, ExpressionNumberKind.BIG_DECIMAL)
                .set(SpreadsheetMetadataPropertyName.PRECISION, 0)
                .set(SpreadsheetMetadataPropertyName.ROUNDING_MODE, RoundingMode.HALF_UP)
                .loadFromLocale(
                    LocaleContexts.jre(locale)
                )
        );

        final SpreadsheetMetadataHateosResourceHandlerContext context = new FakeSpreadsheetMetadataHateosResourceHandlerContext() {

            @Override
            public Optional<SpreadsheetContext> spreadsheetContext(final SpreadsheetId id) {
                return Optional.ofNullable(
                    ID.equals(id) ?
                        new FakeSpreadsheetContext() {

                            @Override
                            public SpreadsheetEngineContext spreadsheetEngineContext() {
                                return new FakeSpreadsheetEngineContext() {
                                    @Override
                                    public SpreadsheetMetadata saveMetadata(final SpreadsheetMetadata metadata) {
                                        return store.save(metadata);
                                    }

                                    @Override
                                    public Optional<SpreadsheetMetadata> loadMetadata(final SpreadsheetId id) {
                                        return store.load(id);
                                    }
                                };
                            }

                            @Override
                            public String toString() {
                                return store.toString();
                            }
                        } :
                        null
                );
            }
        };

        final SpreadsheetMetadataPropertyName<RoundingMode> propertyName = SpreadsheetMetadataPropertyName.ROUNDING_MODE;
        final RoundingMode propertyValue = RoundingMode.FLOOR;

        final JsonNodeMarshallContext marshallContext = metadata.jsonNodeMarshallContext();

        this.applyAndCheck(
            SpreadsheetMetadataPatchFunction.with(ID, context),
            JsonNode.object()
                .set(
                    JsonPropertyName.with(propertyName.value()),
                    marshallContext.marshall(propertyValue)
                ),
            marshallContext.marshall(
                metadata.set(
                    propertyName,
                    propertyValue
                )
            )
        );
    }

    @Test
    public void testToString() {
        this.toStringAndCheck(this.createFunction(), ID + " " + CONTEXT);
    }

    @Override
    public SpreadsheetMetadataPatchFunction createFunction() {
        return SpreadsheetMetadataPatchFunction.with(ID, CONTEXT);
    }

    @Override
    public Class<SpreadsheetMetadataPatchFunction> type() {
        return SpreadsheetMetadataPatchFunction.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}

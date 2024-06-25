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
import walkingkooka.net.Url;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.HttpProtocolVersion;
import walkingkooka.net.http.HttpTransport;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.HttpRequests;
import walkingkooka.net.http.server.hateos.HateosContentType;
import walkingkooka.net.http.server.hateos.HateosHttpEntityHandler;
import walkingkooka.net.http.server.hateos.HateosHttpEntityHandlerTesting;
import walkingkooka.reflect.ClassTesting;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.SpreadsheetViewportWindows;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetViewport;
import walkingkooka.spreadsheet.store.FakeSpreadsheetLabelStore;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.store.repo.FakeSpreadsheetStoreRepository;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContexts;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContexts;

import java.math.MathContext;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public abstract class SpreadsheetDeltaPatchHateosHttpEntityHandlerTestCase<H extends SpreadsheetDeltaPatchHateosHttpEntityHandler<S, R>,
        S extends SpreadsheetSelection & Comparable<S>,
        R extends SpreadsheetSelection & Comparable<R>> implements
        HateosHttpEntityHandlerTesting<H, S>,
        ClassTesting<H> {

    SpreadsheetDeltaPatchHateosHttpEntityHandlerTestCase() {
        super();
    }

    final static double WIDTH = 1000;

    final static double HEIGHT = 600;

    final static SpreadsheetLabelName LABEL = SpreadsheetSelection.labelName("LabelB2");
    final static SpreadsheetCellReference CELL = SpreadsheetSelection.parseCell("B2");

    final static SpreadsheetCellReference CELL2 = SpreadsheetSelection.parseCell("C3");

    final static SpreadsheetCellRangeReference CELL_RANGE = SpreadsheetSelection.parseCellRange(CELL + ":" + CELL2);

    final static SpreadsheetViewportWindows WINDOWS = SpreadsheetViewportWindows.parse("A1:Z99");

    final static String OUTSIDE_WINDOW = "Z99";

    private final static SpreadsheetEngine ENGINE = new FakeSpreadsheetEngine() {
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
                if (false == LABEL.equals(selection)) {
                    throw new IllegalArgumentException("Unknown label " + selection);
                }
                resolved = CELL;
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
                                    LABEL.equals(label) ?
                                            LABEL.mapping(CELL) :
                                            null
                            );
                        }
                    };
                }
            };
        }
    };

    @Test
    public final void testHandleAllFails() {
        this.handleAllFails(
                this.entity(),
                this.parameters(),
                UnsupportedOperationException.class
        );
    }

    @Test
    public final void testHandleManyFails() {
        this.handleManyFails(
                this.manyIds(),
                this.entity(),
                this.parameters(),
                UnsupportedOperationException.class
        );
    }

    @Test
    public final void testHandleNoneFails() {
        this.handleNoneFails(
                this.entity(),
                this.parameters(),
                UnsupportedOperationException.class
        );
    }

    @Override
    public final H createHandler() {
        return this.createHandler(
                ENGINE,
                CONTEXT
        );
    }

    final H createHandler(final SpreadsheetEngine engine,
                          final SpreadsheetEngineContext context) {
        return this.createHandler(
                engine,
                HateosContentType.json(
                        JsonNodeUnmarshallContexts.basic(
                                ExpressionNumberKind.BIG_DECIMAL,
                                MathContext.DECIMAL32
                        ),
                        JsonNodeMarshallContexts.basic()
                ),
                context
        );
    }

    abstract H createHandler(final SpreadsheetEngine engine,
                             final HateosContentType hateosContentType,
                             final SpreadsheetEngineContext context);

    final HttpEntity httpEntity(final SpreadsheetDelta delta) {
        return this.httpEntity(
                this.marshall(delta)
        );
    }

    final String marshall(final SpreadsheetDelta delta) {
        return JsonNodeMarshallContexts.basic()
                .marshall(delta)
                .toString();
    }

    final HttpEntity httpEntity(final String json) {
        return HttpEntity.EMPTY
                .addHeader(HttpHeaderName.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBodyText(json);
    }

    @Override
    public final HttpEntity entity() {
        return HttpEntity.EMPTY;
    }

    @Override
    public final Map<HttpRequestAttribute<?>, Object> parameters() {
        return HateosHttpEntityHandler.NO_PARAMETERS;
    }

    final Map<HttpRequestAttribute<?>, Object> parameters(final String queryString) {
        return HttpRequests.value(
                HttpMethod.POST,
                HttpTransport.SECURED,
                Url.parseRelative("/api/patch/something?" + queryString),
                HttpProtocolVersion.VERSION_1_0,
                HttpEntity.EMPTY // is ignored
        ).routerParameters();
    }

    // Class............................................................................................................

    @Override
    public final JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }

    // typeNaming.......................................................................................................

    @Override
    public final String typeNamePrefix() {
        return SpreadsheetDeltaPatchHateosHttpEntityHandler.class.getSimpleName();
    }
}

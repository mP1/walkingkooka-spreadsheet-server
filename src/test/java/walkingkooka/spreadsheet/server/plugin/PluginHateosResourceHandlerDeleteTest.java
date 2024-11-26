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

package walkingkooka.spreadsheet.server.plugin;

import org.junit.jupiter.api.Test;
import walkingkooka.Binary;
import walkingkooka.ToStringTesting;
import walkingkooka.collect.Range;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerTesting;
import walkingkooka.plugin.PluginName;
import walkingkooka.plugin.store.Plugin;
import walkingkooka.plugin.store.PluginSet;
import walkingkooka.plugin.store.PluginStore;
import walkingkooka.plugin.store.PluginStores;
import walkingkooka.reflect.ClassName;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.server.FakeSpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class PluginHateosResourceHandlerDeleteTest
        implements HateosResourceHandlerTesting<PluginHateosResourceHandlerDelete,
        PluginName,
        Plugin,
        PluginSet,
        SpreadsheetEngineHateosResourceHandlerContext>,
        ToStringTesting<PluginHateosResourceHandlerDelete> {

    // hateos...........................................................................................................

    private final static Plugin PLUGIN1 = plugin(1);

    private final static Plugin PLUGIN2 = plugin(2);

    private final static Plugin PLUGIN3 = plugin(3);

    private final static Plugin PLUGIN4 = plugin(4);

    private static Plugin plugin(final int n) {
        return Plugin.with(
                PluginName.with("TestPlugin" + n * 111),
                "plugin-" + n * 111 + ".jar",
                Binary.with("Hello".getBytes(Charset.defaultCharset())),
                ClassName.with("example.TestPlugin" + n * 111),
                EmailAddress.parse("user@example.com"),
                LocalDateTime.of(
                        1999,
                        12,
                        31,
                        12,
                        58
                )
        );
    }

    static class TestSpreadsheetEngineHateosResourceHandlerContext extends FakeSpreadsheetEngineHateosResourceHandlerContext {

        TestSpreadsheetEngineHateosResourceHandlerContext() {
            final PluginStore store = PluginStores.treeMap();

            store.save(PLUGIN1);
            store.save(PLUGIN2);
            store.save(PLUGIN3);
            store.save(PLUGIN4);

            this.store = store;
        }

        @Override
        public PluginStore pluginStore() {
            return this.store;
        }

        private final PluginStore store;
    }

    @Test
    public void testHandleOne() {
        final SpreadsheetEngineHateosResourceHandlerContext context = new TestSpreadsheetEngineHateosResourceHandlerContext();

        this.handleOneAndCheck(
                PLUGIN1.name(),
                Optional.empty(), // resource
                Maps.empty(), // parameters
                context,
                Optional.empty()
        );

        this.checkEquals(
                Lists.of(
                        PLUGIN2,
                        PLUGIN3,
                        PLUGIN4
                ),
                context.pluginStore()
                        .all()
        );
    }

    @Test
    public void testHandleOneNotFound() {
        this.handleOneAndCheck(
                PluginName.with("Unknown"),
                Optional.empty(), // resource
                Maps.empty(), // parameters
                this.context(),
                Optional.empty()
        );
    }

    @Test
    public void testHandleAll() {
        final SpreadsheetEngineHateosResourceHandlerContext context = new TestSpreadsheetEngineHateosResourceHandlerContext();

        this.handleAllAndCheck(
                Optional.empty(), // resource
                Maps.empty(), // parameters
                context,
                Optional.empty()
        );

        this.checkEquals(
                Lists.empty(),
                context.pluginStore()
                        .all()
        );
    }

    @Test
    public void testHandleRange() {
        final SpreadsheetEngineHateosResourceHandlerContext context = new TestSpreadsheetEngineHateosResourceHandlerContext();

        this.handleRangeAndCheck(
                Range.greaterThanEquals(PLUGIN2.name())
                        .and(
                                Range.lessThanEquals(PLUGIN3.name())
                        ),
                Optional.empty(), // resource
                Maps.empty(), // parameters
                context,
                Optional.empty()
        );

        this.checkEquals(
                Lists.of(
                        PLUGIN1,
                        PLUGIN4
                ),
                context.pluginStore()
                        .all()
        );
    }

    @Override
    public PluginHateosResourceHandlerDelete createHandler() {
        return PluginHateosResourceHandlerDelete.INSTANCE;
    }

    @Override
    public PluginName id() {
        return PluginName.with("test-plugin-999");
    }

    @Override
    public Set<PluginName> manyIds() {
        return Sets.of(
                PLUGIN1.name(),
                PLUGIN2.name(),
                PLUGIN3.name(),
                PLUGIN4.name()
        );
    }

    @Override
    public Range<PluginName> range() {
        return Range.greaterThanEquals(
                PLUGIN1.name()
        ).and(
                Range.lessThanEquals(
                        PLUGIN2.name()
                )
        );
    }

    @Override
    public Optional<Plugin> resource() {
        return Optional.empty();
    }

    @Override
    public Optional<PluginSet> collectionResource() {
        return Optional.empty();
    }

    @Override
    public Map<HttpRequestAttribute<?>, Object> parameters() {
        return Maps.empty();
    }

    @Override
    public SpreadsheetEngineHateosResourceHandlerContext context() {
        return new TestSpreadsheetEngineHateosResourceHandlerContext();
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(
                this.createHandler(),
                "DELETE PluginStore"
        );
    }

    // type naming......................................................................................................

    @Override
    public String typeNamePrefix() {
        return Plugin.class.getSimpleName();
    }

    @Override
    public String typeNameSuffix() {
        return "Delete";
    }

    // Class............................................................................................................

    @Override
    public Class<PluginHateosResourceHandlerDelete> type() {
        return PluginHateosResourceHandlerDelete.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}

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
import walkingkooka.collect.set.Sets;
import walkingkooka.net.UrlPath;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerTesting;
import walkingkooka.plugin.PluginName;
import walkingkooka.plugin.store.Plugin;
import walkingkooka.plugin.store.PluginSet;
import walkingkooka.plugin.store.PluginStore;
import walkingkooka.plugin.store.PluginStores;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class PluginHateosResourceHandlerSaveTest
    implements HateosResourceHandlerTesting<PluginHateosResourceHandlerSave,
    PluginName,
    Plugin,
    PluginSet,
    PluginHateosResourceHandlerContext>,
    ToStringTesting<PluginHateosResourceHandlerSave>,
    SpreadsheetMetadataTesting {

    // hateos...........................................................................................................

    private final static Plugin PLUGIN1 = plugin(1);

    private final static Plugin PLUGIN2 = plugin(2);

    private final static Plugin PLUGIN3 = plugin(3);

    private final static Plugin PLUGIN4 = plugin(4);

    private static Plugin plugin(final int n) {
        return Plugin.with(
            PluginName.with("test-plugin-" + n * 111),
            "plugin-" + n * 111 + ".jar",
            Binary.with("Hello".getBytes(Charset.defaultCharset())),
            USER,
            NOW.now()
        );
    }

    static class TestPluginHateosResourceHandlerContext extends FakePluginHateosResourceHandlerContext {

        TestPluginHateosResourceHandlerContext() {
            this.store = PluginStores.treeMap();
            this.store.save(PLUGIN1);
        }

        @Override
        public PluginStore pluginStore() {
            return this.store;
        }

        final PluginStore store;
    }

    @Test
    public void testHandleOneCreate() {
        final TestPluginHateosResourceHandlerContext context = new TestPluginHateosResourceHandlerContext();

        this.handleOneAndCheck(
            PLUGIN2.name(),
            Optional.of(PLUGIN2), // resource
            HateosResourceHandler.NO_PARAMETERS,
            UrlPath.EMPTY,
            context,
            Optional.of(PLUGIN2)
        );

        this.checkEquals(
            PLUGIN2,
            context.pluginStore().loadOrFail(PLUGIN2.name())
        );
    }

    @Test
    public void testHandleOneUpdate() {
        final TestPluginHateosResourceHandlerContext context = new TestPluginHateosResourceHandlerContext();

        final Plugin updatePlugin1 = Plugin.with(
            PLUGIN1.name(),
            "updated.jar",
            Binary.with("Updataed".getBytes(Charset.defaultCharset())),
            EmailAddress.parse("updated@example.com"),
            LocalDateTime.of(
                2000,
                1,
                1,
                11,
                28
            )
        );

        this.handleOneAndCheck(
            updatePlugin1.name(),
            Optional.of(updatePlugin1), // resource
            HateosResourceHandler.NO_PARAMETERS,
            UrlPath.EMPTY,
            context,
            Optional.of(updatePlugin1)
        );

        this.checkEquals(
            updatePlugin1,
            context.pluginStore()
                .loadOrFail(PLUGIN1.name())
        );
    }

    @Test
    public void testHandleRange() {
        this.handleRangeFails(
            Range.greaterThanEquals(PLUGIN1.name()).and(
                Range.lessThanEquals(PLUGIN2.name())
            ),
            Optional.empty(), // resource
            this.parameters(), // parameters
            this.path(),
            this.context(),
            UnsupportedOperationException.class
        );
    }

    @Test
    public void testHandleAll() {
        this.handleAllFails(
            Optional.empty(), // resource
            this.parameters(), // parameters
            this.path(),
            this.context(),
            UnsupportedOperationException.class
        );
    }

    @Override
    public PluginHateosResourceHandlerSave createHandler() {
        return PluginHateosResourceHandlerSave.INSTANCE;
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
        return Optional.of(PLUGIN4);
    }

    @Override
    public Optional<PluginSet> collectionResource() {
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

    @Override
    public PluginHateosResourceHandlerContext context() {
        return new TestPluginHateosResourceHandlerContext();
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(
            this.createHandler(),
            "POST PluginStore"
        );
    }

    // type naming......................................................................................................

    @Override
    public String typeNamePrefix() {
        return Plugin.class.getSimpleName();
    }

    @Override
    public String typeNameSuffix() {
        return "Save";
    }

    // Class............................................................................................................

    @Override
    public Class<PluginHateosResourceHandlerSave> type() {
        return PluginHateosResourceHandlerSave.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}

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
import walkingkooka.collect.set.SortedSets;
import walkingkooka.net.UrlPath;
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
import walkingkooka.spreadsheet.server.SpreadsheetUrlQueryParameters;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class PluginHateosResourceHandlerLoadTest
    implements HateosResourceHandlerTesting<PluginHateosResourceHandlerLoad,
    PluginName,
    Plugin,
    PluginSet,
    PluginHateosResourceHandlerContext>,
    ToStringTesting<PluginHateosResourceHandlerLoad>,
    SpreadsheetMetadataTesting {

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
            USER,
            NOW.now()
        );
    }

    private final static PluginHateosResourceHandlerContext CONTEXT = new FakePluginHateosResourceHandlerContext() {
        @Override
        public PluginStore pluginStore() {
            final PluginStore store = PluginStores.treeMap();

            store.save(PLUGIN1);
            store.save(PLUGIN2);
            store.save(PLUGIN3);
            store.save(PLUGIN4);

            return store;
        }
    };

    @Test
    public void testHandleOne() {
        this.handleOneAndCheck(
            PLUGIN1.name(),
            Optional.empty(), // resource
            HateosResourceHandler.NO_PARAMETERS,
            UrlPath.EMPTY,
            this.context(),
            Optional.of(PLUGIN1)
        );
    }

    @Test
    public void testHandleOneNotFound() {
        this.handleOneAndCheck(
            PluginName.with("Unknown"),
            Optional.empty(), // resource
            HateosResourceHandler.NO_PARAMETERS,
            UrlPath.EMPTY,
            this.context(),
            Optional.empty()
        );
    }

    @Test
    public void testHandleAll() {
        this.handleAllAndCheck(
            Optional.empty(), // resource
            HateosResourceHandler.NO_PARAMETERS,
            UrlPath.EMPTY,
            this.context(),
            Optional.of(
                PluginSet.with(
                    SortedSets.of(
                        PLUGIN1,
                        PLUGIN2,
                        PLUGIN3,
                        PLUGIN4
                    )
                )
            )
        );
    }

    @Test
    public void testHandleAllWithOffsetAndCount() {
        this.handleAllAndCheck(
            Optional.empty(), // resource
            Maps.of(
                SpreadsheetUrlQueryParameters.OFFSET, Lists.of("1"),
                SpreadsheetUrlQueryParameters.COUNT, Lists.of("2")
            ), // parameters
            UrlPath.EMPTY,
            this.context(),
            Optional.of(
                PluginSet.with(
                    SortedSets.of(
                        PLUGIN2, // offset = 1
                        PLUGIN3
                    )
                )
            )
        );
    }

    @Override
    public PluginHateosResourceHandlerLoad createHandler() {
        return PluginHateosResourceHandlerLoad.INSTANCE;
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
        return HateosResourceHandler.NO_PARAMETERS;
    }

    @Override
    public UrlPath path() {
        return UrlPath.EMPTY;
    }

    @Override
    public PluginHateosResourceHandlerContext context() {
        return CONTEXT;
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(
            this.createHandler(),
            "GET PluginStore"
        );
    }

    // type naming......................................................................................................

    @Override
    public String typeNamePrefix() {
        return Plugin.class.getSimpleName();
    }

    @Override
    public String typeNameSuffix() {
        return "Load";
    }

    // Class............................................................................................................

    @Override
    public Class<PluginHateosResourceHandlerLoad> type() {
        return PluginHateosResourceHandlerLoad.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}

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
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.UrlPath;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.header.HeaderException;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHttpEntityHandlerTesting;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.net.http.server.hateos.HateosResourceMappings;
import walkingkooka.plugin.JarFileTesting;
import walkingkooka.plugin.PluginName;
import walkingkooka.plugin.store.Plugin;
import walkingkooka.plugin.store.PluginStore;
import walkingkooka.plugin.store.PluginStores;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.server.SpreadsheetServerMediaTypes;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;
import walkingkooka.tree.json.JsonNode;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class PluginHateosHttpEntityHandlerListTest
    implements HateosHttpEntityHandlerTesting<PluginHateosHttpEntityHandlerList,
    PluginName,
    PluginHateosResourceHandlerContext>,
    ToStringTesting<PluginHateosHttpEntityHandlerList>,
    SpreadsheetMetadataTesting,
    JarFileTesting {

    // hateos...........................................................................................................

    private final static Plugin PLUGIN1 = plugin("TestPlugin111");

    private final static Plugin PLUGIN2 = plugin("TestPlugin222");

    private static Plugin plugin(final String pluginName) {
        return Plugin.with(
            PluginName.with(pluginName),
            pluginName + ".jar",
            jarFile(pluginName),
            USER,
            NOW.now()
        );
    }

    private static Binary jarFile(final String pluginName) {
        final String manifest = (
            "Manifest-Version: 1.0\r\n" +
                "plugin-name: PluginName\r\n" +
                "plugin-provider-factory-className: example.PluginName\r\n"
        )
            .replace(
                "PluginName",
                pluginName
            );

        return Binary.with(
            JarFileTesting.jarFile(
                manifest,
                Maps.of(
                    "file111.txt",
                    "Hello111".getBytes(StandardCharsets.UTF_8),
                    "dir222/",
                    new byte[0],
                    "file333.txt",
                    "Hello333".getBytes(StandardCharsets.UTF_8)
                )
            )
        );
    }

    @Test
    public void testHandleOneMissingAcceptContentTypeFails() {
        final HeaderException thrown = this.handleOneFails(
            PLUGIN1.name(),
            HttpEntity.EMPTY,
            this.parameters(),
            this.path(),
            new TestPluginHateosResourceHandlerContext(),
            HeaderException.class
        );

        this.checkEquals(
            "Missing header Accept",
            thrown.getMessage()
        );
    }

    @Test
    public void testHandleOnContentTypeNotJsonFails() {
        final IllegalArgumentException thrown = this.handleOneFails(
            PLUGIN1.name(),
            HttpEntity.EMPTY.setAccept(
                MediaType.TEXT_PLAIN.accept()
            ),
            this.parameters(),
            this.path(),
            new TestPluginHateosResourceHandlerContext(),
            IllegalArgumentException.class
        );

        this.checkEquals(
            "Accept: Got application/json require text/plain",
            thrown.getMessage()
        );
    }

    @Test
    public void testHandleOneAbsent() {
        final TestPluginHateosResourceHandlerContext context = new TestPluginHateosResourceHandlerContext();

        this.handleOneAndCheck(
            PLUGIN2.name(),
            this.entity(), // entity
            this.parameters(),
            this.path(),
            context,
            HttpEntity.EMPTY
        );
    }

    @Test
    public void testHandleOne() {
        final TestPluginHateosResourceHandlerContext context = new TestPluginHateosResourceHandlerContext();

        this.handleOneAndCheck(
            PLUGIN1.name(),
            this.entity(), // entity
            HateosResourceHandler.NO_PARAMETERS,
            UrlPath.EMPTY,
            context,
            HttpEntity.EMPTY.setContentType(
                    SpreadsheetServerMediaTypes.CONTENT_TYPE
                ).addHeader(HateosResourceMappings.X_CONTENT_TYPE_NAME, JarEntryInfoList.class.getSimpleName())
                .setBodyText(
                    "[\n" +
                        "  {\n" +
                        "    \"name\": \"/META-INF/MANIFEST.MF\",\n" +
                        "    \"method\": 8,\n" +
                        "    \"create\": \"1999-12-31T12:58\",\n" +
                        "    \"lastModified\": \"2000-01-02T04:58\"\n" +
                        "  },\n" +
                        "  {\n" +
                        "    \"name\": \"/file111.txt\",\n" +
                        "    \"method\": 8,\n" +
                        "    \"create\": \"1999-12-31T12:58\",\n" +
                        "    \"lastModified\": \"2000-01-02T04:58\"\n" +
                        "  },\n" +
                        "  {\n" +
                        "    \"name\": \"/dir222/\",\n" +
                        "    \"method\": 8,\n" +
                        "    \"create\": \"1999-12-31T12:58\",\n" +
                        "    \"lastModified\": \"2000-01-02T04:58\"\n" +
                        "  },\n" +
                        "  {\n" +
                        "    \"name\": \"/file333.txt\",\n" +
                        "    \"method\": 8,\n" +
                        "    \"create\": \"1999-12-31T12:58\",\n" +
                        "    \"lastModified\": \"2000-01-02T04:58\"\n" +
                        "  }\n" +
                        "]"
                ).setContentLength()
        );
    }

    @Override
    public PluginHateosHttpEntityHandlerList createHandler() {
        return PluginHateosHttpEntityHandlerList.INSTANCE;
    }

    @Override
    public PluginName id() {
        return PluginName.with("test-plugin-999");
    }

    @Override
    public Set<PluginName> manyIds() {
        return Sets.of(
            PLUGIN1.name(),
            PLUGIN2.name()
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
    public Map<HttpRequestAttribute<?>, Object> parameters() {
        return Maps.empty();
    }

    @Override
    public UrlPath path() {
        return UrlPath.EMPTY;
    }

    @Override
    public HttpEntity entity() {
        return HttpEntity.EMPTY.setAccept(
            SpreadsheetServerMediaTypes.CONTENT_TYPE.accept()
        );
    }

    @Override
    public PluginHateosResourceHandlerContext context() {
        return new TestPluginHateosResourceHandlerContext();
    }

    final static class TestPluginHateosResourceHandlerContext extends FakePluginHateosResourceHandlerContext {

        TestPluginHateosResourceHandlerContext() {
            this.store = PluginStores.treeMap();
            this.store.save(PLUGIN1);
        }

        @Override
        public Indentation indentation() {
            return INDENTATION;
        }

        @Override
        public LineEnding lineEnding() {
            return LineEnding.NL;
        }

        @Override
        public LocalDateTime now() {
            return NOW.now();
        }

        @Override
        public PluginStore pluginStore() {
            return this.store;
        }

        final PluginStore store;

        @Override
        public Optional<EmailAddress> user() {
            return Optional.of(USER);
        }

        @Override
        public JsonNode marshall(final Object value) {
            return JSON_NODE_MARSHALL_CONTEXT.marshall(value);
        }
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(
            this.createHandler(),
            "GET PluginStore/list"
        );
    }

    // type naming......................................................................................................

    @Override
    public String typeNamePrefix() {
        return Plugin.class.getSimpleName();
    }

    @Override
    public String typeNameSuffix() {
        return "List";
    }

    // Class............................................................................................................

    @Override
    public Class<PluginHateosHttpEntityHandlerList> type() {
        return PluginHateosHttpEntityHandlerList.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}

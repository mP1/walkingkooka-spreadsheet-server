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

import walkingkooka.Cast;
import walkingkooka.collect.list.ImmutableListDefaults;
import walkingkooka.collect.list.Lists;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.AbstractList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;

public final class JarEntryInfoList extends AbstractList<JarEntryInfo>
    implements ImmutableListDefaults<JarEntryInfoList, JarEntryInfo> {

    /**
     * Empty
     */
    public final static JarEntryInfoList EMPTY = new JarEntryInfoList(Lists.empty());

    public static JarEntryInfoList readJarFile(final InputStream inputStream) {
        return JarEntryInfoListReadJarFile.readJarFile(inputStream);
    }

    public static JarEntryInfoList with(final List<JarEntryInfo> infos) {
        Objects.requireNonNull(infos, "infos");

        return infos instanceof JarEntryInfoList ?
            (JarEntryInfoList) infos :
            withCopy(
                Lists.immutable(infos)
            );
    }

    private static JarEntryInfoList withCopy(final List<JarEntryInfo> infos) {
        return infos.isEmpty() ?
            EMPTY :
            new JarEntryInfoList(infos);
    }

    private JarEntryInfoList(final List<JarEntryInfo> infos) {
        this.infos = infos;
    }

    @Override
    public JarEntryInfo get(final int index) {
        return this.infos.get(index);
    }

    @Override
    public int size() {
        return this.infos.size();
    }

    private final List<JarEntryInfo> infos;

    // ImmutableList....................................................................................................

    @Override
    public void elementCheck(final JarEntryInfo info) {
        Objects.requireNonNull(info, "info");
    }

    @Override
    public JarEntryInfoList setElements(final List<JarEntryInfo> infos) {
        final JarEntryInfoList copy = with(infos);
        return this.equals(copy) ?
            this :
            copy;
    }

    // json.............................................................................................................

    static JarEntryInfoList unmarshall(final JsonNode node,
                                       final JsonNodeUnmarshallContext context) {
        return with(
            Cast.to(
                context.unmarshallList(
                    node,
                    JarEntryInfo.class
                )
            )
        );
    }

    private JsonNode marshall(final JsonNodeMarshallContext context) {
        return context.marshallCollection(this);
    }

    static {
        JarEntryInfo.with(
            JarEntryInfoName.with("/Dummy"), // name
            OptionalLong.of(0), // size
            OptionalLong.of(0), // compressedSize
            OptionalInt.of(1), // method
            OptionalLong.of(123), // crc
            Optional.of(LocalDateTime.MAX), // create
            Optional.of(LocalDateTime.MAX) // lastModified
        );

        JsonNodeContext.register(
            JsonNodeContext.computeTypeName(JarEntryInfoList.class),
            JarEntryInfoList::unmarshall,
            JarEntryInfoList::marshall,
            JarEntryInfoList.class
        );
    }
}

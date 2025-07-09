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

package walkingkooka.spreadsheet.server;

import walkingkooka.net.header.LinkRelation;
import walkingkooka.reflect.PublicStaticHelper;

public final class SpreadsheetServerLinkRelations implements PublicStaticHelper {

    /**
     * A {@link LinkRelation} with <code>clear</code>.
     */
    public static final LinkRelation<?> CLEAR = LinkRelation.with("clear");

    public final static LinkRelation<?> DOWNLOAD = LinkRelation.with("download");

    public final static LinkRelation<?> EDIT = LinkRelation.with("edit");


    /**
     * A {@link LinkRelation} with <code>fill</code>.
     */
    public static final LinkRelation<?> FILL = LinkRelation.with("fill");


    public final static LinkRelation<?> FILTER = LinkRelation.with("filter");

    /**
     * A {@link LinkRelation} with <code>find</code>.
     */
    public static final LinkRelation<?> FIND = LinkRelation.with("find");

    public final static LinkRelation<?> FORM = LinkRelation.with("form");

    public final static LinkRelation<?> INSERT_AFTER = LinkRelation.with("insert-after");

    public final static LinkRelation<?> INSERT_BEFORE = LinkRelation.with("insert-before");

    /**
     * A {@link LinkRelation} with <code>labels</code>.
     */
    public static final LinkRelation<?> LABELS = LinkRelation.with("labels");

    public final static LinkRelation<?> LIST = LinkRelation.with("list");

    public final static LinkRelation<?> MENU = LinkRelation.with("menu");

    public final static LinkRelation<?> NEXT_TOKEN = LinkRelation.with("next-token");

    /**
     * A {@link LinkRelation} with <code>references</code>.
     */
    public static final LinkRelation<?> REFERENCES = LinkRelation.with("references");

    public final static LinkRelation<?> SAMPLES = LinkRelation.with("samples");

    /**
     * A {@link LinkRelation} with <code>sort</code>.
     */
    public static final LinkRelation<?> SORT = LinkRelation.with("sort");

    public final static LinkRelation<?> TOKENS = LinkRelation.with("tokens");

    public final static LinkRelation<?> UPLOAD = LinkRelation.with("upload");

    public final static LinkRelation<?> VERIFY = LinkRelation.with("verify");

    /**
     * Stop creation
     */
    private SpreadsheetServerLinkRelations() {
        throw new UnsupportedOperationException();
    }
}

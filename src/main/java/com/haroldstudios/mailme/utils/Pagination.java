/*
 *   Copyright [2020] [Harry0198]
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.haroldstudios.mailme.utils;

import java.util.ArrayList;
import java.util.List;

public final class Pagination<T> extends ArrayList<T>  {

    private final int pageSize;

    public Pagination(int pageSize, List<T> objects) {
        this.pageSize = pageSize;
        addAll(objects);
    }

    public int pageSize() {
        return pageSize;
    }

    public int totalPages() {
        return (int) Math.ceil((double) size() / pageSize);
    }

    public boolean exists(int page) {
        return page >= 0 && page < totalPages();
    }

    public List<T> getPage(int page) {
        if (page < 0 || page >= totalPages()) {
            if (page > 0) { // If can go to previous page
                return getPage(page - 1);
            }
            throw new IndexOutOfBoundsException("Page: " + page + ", Total: " + totalPages());
        }

        List<T> objects = new ArrayList<>();

        int min = page * pageSize;
        int max = ((page * pageSize) + pageSize);

        if (max > size()) max = size();

        for (int i = min; max > i; i++) objects.add(get(i));

        return objects;
    }
}
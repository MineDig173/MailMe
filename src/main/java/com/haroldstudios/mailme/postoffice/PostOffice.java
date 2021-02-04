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

package com.haroldstudios.mailme.postoffice;

import org.bukkit.Location;

public class PostOffice {

    private final Location postOfficeLocation;
    private final boolean sendType;

    public PostOffice(final Location location, final boolean sendType) {
        this.postOfficeLocation = location;
        this.sendType = sendType;
    }

    public boolean isSendType() {
        return sendType;
    }

    public Location getPostOfficeLocation() {
        return postOfficeLocation;
    }
}
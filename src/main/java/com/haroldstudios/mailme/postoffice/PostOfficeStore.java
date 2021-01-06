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

import com.haroldstudios.mailme.MailMe;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PostOfficeStore {

    private final List<PostOffice> postOffices = new ArrayList<>();

    public synchronized List<PostOffice> getPostOffices() {
        return postOffices;
    }

    public synchronized void addPostOffice(final PostOffice postOffice) {
        this.postOffices.add(postOffice);
        update();
    }

    public boolean isPostOfficeAtLocation(Location location) {
        return getPostOfficeFromLocation(location).size() > 0;
    }

    public synchronized void removePostOffice(final PostOffice postOffice) {
        this.postOffices.remove(postOffice);
        update();
    }

    public synchronized boolean removePostOffice(final Location location) {
        List<PostOffice> postOffices = getPostOfficeFromLocation(location);

        Optional<PostOffice> optionalPost = postOffices.stream().findFirst();

        if (optionalPost.isPresent()) {
            postOffices.remove(optionalPost.get());
            return true;
        }
        return false;
    }

    public synchronized List<PostOffice> getPostOfficeFromLocation(final Location location) {
        return getPostOffices().stream().filter(office -> {
            final Location officeLocations = office.getPostOfficeLocation();
            final int x = officeLocations.getBlockX();
            final int y = officeLocations.getBlockY();
            final int z = officeLocations.getBlockZ();
            final World world = officeLocations.getWorld();

            return location.getWorld() == world && location.getBlockX() == x && location.getBlockY() == y && location.getBlockZ() == z;
        }).collect(Collectors.toList());
    }



    /**
     * Writes data to file.
     */
    public synchronized void update() {
        Bukkit.getScheduler().runTaskAsynchronously(MailMe.getInstance(), () ->
                MailMe.getInstance().getCache().getFileUtil().save(this));

    }
}

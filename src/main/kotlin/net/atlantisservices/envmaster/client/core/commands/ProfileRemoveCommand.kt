/*
 * Copyright (c) 2026 Atlantis Services
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
 * @author Selixe
 */

package net.atlantisservices.envmaster.client.core.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import net.atlantisservices.envmaster.client.core.api.storage.Storage
import net.atlantisservices.envmaster.client.util.bold
import net.atlantisservices.envmaster.client.util.cliError
import net.atlantisservices.envmaster.client.util.info
import net.atlantisservices.envmaster.client.util.primary
import net.atlantisservices.envmaster.client.util.success
import net.atlantisservices.envmaster.client.util.warn

class ProfileRemoveCommand : CliktCommand(
    name = "remove",
    help = "Delete a stored profile"
) {
    private val name by argument("name", help = "Profile name to remove")

    override fun run() {
        val active = Storage.getActiveProfileName()
        if (Storage.removeProfile(name)) {
            success("Removed profile ${primary(name)}")
            if (name == active) {
                val next = Storage.getActiveProfileName()
                if (next != null) info("Active profile is now ${primary(next)}")
                else warn("No remaining profiles — run ${bold("envmanager login")} to authenticate.")
            }
        } else {
            cliError("Profile '$name' not found")
        }
    }
}
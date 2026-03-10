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
import net.atlantisservices.envmaster.client.util.dim
import net.atlantisservices.envmaster.client.util.muted
import net.atlantisservices.envmaster.client.util.primary
import net.atlantisservices.envmaster.client.util.success
import java.io.File

class ProfileUseCommand : CliktCommand(
    name = "use",
    help = "Switch the active profile for this directory"
) {
    private val name by argument("name", help = "Profile name to activate")

    override fun run() {
        if (Storage.getProfile(name) == null)
            cliError("Profile '$name' not found — run ${bold("envmanager profile list")} to see available profiles.")

        if (!File(".envmanager").exists())
            cliError("No .envmanager file found in the current directory. Are you inside a project?")

        Storage.saveLocalUserConfig(Storage.loadLocalUserConfig().copy(profile = name))
        val p = Storage.getProfile(name)!!
        println()
        success("Using profile ${primary(name)}  ${muted(p.email)}  ${dim("(this directory only)")}")
    }
}
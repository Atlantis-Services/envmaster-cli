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
import com.github.ajalt.clikt.core.subcommands
import net.atlantisservices.envmaster.client.core.api.storage.Storage
import net.atlantisservices.envmaster.client.util.*
import java.io.File

class ProfileCommand : CliktCommand(
    name = "profile",
    help = "Manage authentication profiles",
    invokeWithoutSubcommand = true
) {
    init {
        subcommands(ProfileListCommand(), ProfileUseCommand(), ProfileRemoveCommand())
    }

    override fun run() {
        if (currentContext.invokedSubcommand != null) return

        val localName = if (File(".envmanager").exists())
            Storage.loadLocalUserConfig().profile
        else null

        val globalName = Storage.getActiveProfileName()
        val effectiveName = localName ?: globalName
        val profile = effectiveName?.let { Storage.getProfile(it) }

        if (profile != null) {
            printKV(
                "Active profile" to primary(effectiveName),
                "Email" to text(profile.email),
                "Scope" to if (localName != null) cyan("local (.envmanager)") else muted("global"),
            )
            if (localName != null && globalName != null && localName != globalName)
                info("Global default is ${muted(globalName)} — overridden locally")
        } else {
            println()
            warn("No active profile.")
            printHint("Get started:", "envmanager login")
        }
    }
}
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
import net.atlantisservices.envmaster.client.core.api.storage.Storage
import net.atlantisservices.envmaster.client.util.green
import net.atlantisservices.envmaster.client.util.muted
import net.atlantisservices.envmaster.client.util.printHint
import net.atlantisservices.envmaster.client.util.printTable
import net.atlantisservices.envmaster.client.util.text
import net.atlantisservices.envmaster.client.util.warn
import kotlin.collections.component1
import kotlin.collections.component2

class ProfileListCommand : CliktCommand(
    name = "list",
    help = "List all configured profiles"
) {
    override fun run() {
        val data = Storage.loadProfiles()

        if (data.profiles.isEmpty()) {
            println()
            warn("No profiles configured.")
            printHint("Get started:", "envmaster login")
            return
        }

        printTable(
            headers = listOf("", "Profile", "Email"),
            rows = data.profiles.entries.sortedBy { it.key }.map { (name, p) ->
                val active = name == data.default
                listOf(
                    if (active) green("•") else muted("◦"),
                    text(name),
                    text(p.email),
                )
            }
        )
        printHint("Switch profile:", "envmaster profile use <name>")
    }

}
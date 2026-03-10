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
import com.github.ajalt.clikt.parameters.options.option
import net.atlantisservices.envmaster.client.core.api.models.ApiResult
import net.atlantisservices.envmaster.client.core.api.storage.Storage
import net.atlantisservices.envmaster.client.util.bold
import net.atlantisservices.envmaster.client.util.cyan
import net.atlantisservices.envmaster.client.util.green
import net.atlantisservices.envmaster.client.util.handle401OrError
import net.atlantisservices.envmaster.client.util.magenta
import net.atlantisservices.envmaster.client.util.printKV
import net.atlantisservices.envmaster.client.util.red
import net.atlantisservices.envmaster.client.util.withClient
import net.atlantisservices.envmaster.client.util.yellow

class WhoAmICommand : CliktCommand(
    name = "whoami",
    help = "Show information about the currently authenticated user"
) {
    private val profileName by option("--profile", "-p", help = "Profile to inspect")

    override fun run() {
        withClient(profileName) { client ->
            val name = profileName ?: Storage.getActiveProfileName() ?: "default"
            when (val r = client.getProfile()) {
                is ApiResult.Success -> {
                    val u = r.data
                    printKV(
                        "Profile"  to cyan(name),
                        "Email"    to bold(u.email),
                        "Plan"     to magenta(u.plan),
                        "Verified" to if (u.emailVerified) green("yes") else yellow("no — check your inbox"),
                        *buildList {
                            if (u.suspended)     add("Status" to red("suspended"))
                        }.toTypedArray()
                    )
                }
                is ApiResult.Error -> handle401OrError(r)
            }
        }
    }
}
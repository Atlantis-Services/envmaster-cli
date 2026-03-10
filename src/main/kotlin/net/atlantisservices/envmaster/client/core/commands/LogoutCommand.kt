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
import kotlinx.coroutines.runBlocking
import net.atlantisservices.envmaster.client.core.Application
import net.atlantisservices.envmaster.client.core.api.APIClient
import net.atlantisservices.envmaster.client.core.api.storage.Storage
import net.atlantisservices.envmaster.client.util.cliError
import net.atlantisservices.envmaster.client.util.cyan
import net.atlantisservices.envmaster.client.util.success

class LogoutCommand : CliktCommand(
    name = "logout",
    help = "Remove stored credentials for a profile"
) {
    private val profileName by option("--profile", "-p", help = "Profile to log out")

    override fun run() = runBlocking {
        val name = profileName ?: Storage.getActiveProfileName()
        ?: cliError("No active profile to log out from")

        val profile = Storage.getProfile(name)
        if (profile != null) {
            try {
                val client = APIClient(Application.API_URL, profile.token)
                client.logout()
                client.close()
            } catch (_: Exception) {}
        }

        if (Storage.removeProfile(name)) {
            println()
            success("Logged out of ${cyan(name)}")
        } else {
            cliError("Profile '$name' not found")
        }
    }
}
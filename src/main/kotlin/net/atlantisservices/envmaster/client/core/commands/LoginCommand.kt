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
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.atlantisservices.envmaster.client.core.Application
import net.atlantisservices.envmaster.client.core.api.APIClient
import net.atlantisservices.envmaster.client.core.api.models.ApiResult
import net.atlantisservices.envmaster.client.core.api.models.InitiateResponse
import net.atlantisservices.envmaster.client.core.api.models.PollResponse
import net.atlantisservices.envmaster.client.core.api.storage.Profile
import net.atlantisservices.envmaster.client.core.api.storage.Storage
import net.atlantisservices.envmaster.client.util.*
import java.awt.Desktop
import java.net.URI

class LoginCommand : CliktCommand(
    name = "login",
    help = "Authenticate via your browser and save credentials locally."
) {
    private val profileName by option("--profile", "-p",
        help = "Profile name to save credentials under").default("default")

    override fun run() = runBlocking {
        val existing = Storage.getProfile(profileName)
        if (existing != null) warn("Overwriting existing profile '$profileName'")

        println()
        info("Requesting a login session…")

        val client = APIClient(Application.API_URL, null)

        val initiate = try {
            when (val r = client.post<InitiateResponse>("/v1/cli/sessions/initiate")) {
                is ApiResult.Success -> r.data
                is ApiResult.Error   -> cliError("Failed to start login session (${r.status}): ${r.error}")
            }
        } catch (e: java.net.ConnectException) {
            cliError("Could not connect to API at ${Application.API_URL}. Is the server running?")
        }

        println()
        println("  ${bold("Open this URL in your browser to continue:")}")
        println()
        println("  ${cyan(initiate.connectUrl)}")
        println()

        if (tryOpenBrowser(initiate.connectUrl)) {
            info("Browser opened — waiting for you to authorise")
        } else {
            info("Could not open browser automatically — copy the URL above")
        }
        println()

        val spinner = Spinner("Waiting for browser authorisation")
        var token: String? = null
        val deadline = System.currentTimeMillis() + (10L * 60 * 1_000)

        while (System.currentTimeMillis() < deadline) {
            delay(2_000)
            spinner.tick()

            when (val r = client.get<PollResponse>(
                "/v1/cli/sessions/${initiate.sessionId}/poll?secret=${initiate.secret}"
            )) {
                is ApiResult.Success -> when (r.data.state) {
                    "APPROVED" -> { spinner.stop(); token = r.data.token; break }
                    "DENIED"   -> { spinner.stop(); cliError("Login was denied in the browser") }
                    "EXPIRED"  -> { spinner.stop(); cliError("Login link expired — run 'envmaster login' to try again") }
                }
                is ApiResult.Error -> when (r.status) {
                    404, 410 -> { spinner.stop(); cliError("Login session no longer exists — run 'envmaster login' to try again") }
                    403      -> { spinner.stop(); cliError("Invalid session secret — run 'envmaster login' to try again") }
                    in 500..599 -> { }
                    else     -> { spinner.stop(); cliError("Unexpected error (${r.status}): ${r.error}") }
                }
            }
        }

        if (token == null) { spinner.stop(); cliError("Login timed out — run 'envmaster login' to try again") }

        val authedClient = APIClient(Application.API_URL, token)
        val profile = when (val r = authedClient.getProfile()) {
            is ApiResult.Success -> {
                Storage.addProfile(profileName, Profile(email = r.data.email, token = token))
                r.data
            }
            is ApiResult.Error -> cliError("Token received but profile fetch failed (${r.status}) — please try again")
        }

        client.close()
        authedClient.close()

        success("Logged in as ${bold(profile.email)}  ${dim("profile: ${cyan(profileName)}")}")
        printHint("Quick start:", "envmaster init", "envmaster run -- npm run dev")
    }

    private fun tryOpenBrowser(url: String): Boolean =
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI(url)); true
            } else tryShellOpen(url)
        } catch (_: Exception) { tryShellOpen(url) }

    private fun tryShellOpen(url: String): Boolean {
        val os = System.getProperty("os.name").lowercase()
        val cmd = when {
            os.contains("mac")   -> listOf("open", url)
            os.contains("linux") -> listOf("xdg-open", url)
            os.contains("win")   -> listOf("cmd", "/c", "start", url)
            else                 -> return false
        }
        return try { ProcessBuilder(cmd).start(); true } catch (_: Exception) { false }
    }

}
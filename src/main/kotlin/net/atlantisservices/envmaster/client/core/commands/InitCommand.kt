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
import net.atlantisservices.envmaster.client.util.*

class InitCommand : CliktCommand(
    name = "init",
    help = "Interactively create a .envmanager config file in the current directory"
) {
    private val profile by option("--profile", "-p", help = "Profile to use")

    override fun run() {
        if (java.io.File(".envmanager").exists())
            cliError(".envmanager already exists in this directory. Remove it first if you want to re-initialise.")

        val projects = withClient(profile) { client ->
            when (val r = client.listProjects()) {
                is ApiResult.Success -> r.data
                is ApiResult.Error   -> handle401OrError<Nothing>(r)
            }
        } ?: return

        if (projects.isEmpty())
            cliError("You don't have any projects yet — create one in the dashboard.")

        println()
        println("  ${bold(text("Your projects"))}")
        println()
        projects.forEachIndexed { i, p ->
            println("  ${muted("${i + 1}.")}  ${text(p.name)}  ${muted("id: ${p.id}")}")
        }
        println()

        val pidInput = promptText("Project number", default = "1").toIntOrNull()
        val pidChoice = pidInput
            ?.let { projects.getOrNull(it - 1) }
            ?: cliError("Invalid project number. Enter a number between 1 and ${projects.size}.")

        val envs = withClient(profile) { client ->
            when (val r = client.listEnvironments(pidChoice.id)) {
                is ApiResult.Success -> r.data
                is ApiResult.Error   -> handle401OrError<Nothing>(r)
            }
        } ?: return

        println()
        println("  ${bold(text("Environments in '${pidChoice.name}'"))}")
        println()
        envs.forEachIndexed { i, e ->
            println("  ${muted("${i + 1}.")}  ${text(e.name)}  ${muted("id: ${e.id}")}")
        }
        println()

        val eidInput = promptText("Environment number", default = "1").toIntOrNull()
        val eidChoice = eidInput
            ?.let { envs.getOrNull(it - 1) }
            ?: cliError("Invalid environment number. Enter a number between 1 and ${envs.size}.")

        Storage.saveLocalConfig(
            Storage.loadLocalConfig().copy(
                projectId     = pidChoice.id,
                environmentId = eidChoice.id,
            )
        )

        val activeProfile = profile ?: Storage.getActiveProfileName()
        Storage.saveLocalUserConfig(
            Storage.loadLocalUserConfig().copy(
                profile = activeProfile
            )
        )

        println()
        printDivider()
        println()
        success("Created ${bold(".envmanager")} and ${bold(".envmanager.local")}")
        printKV(
            "Project"     to primary(pidChoice.name),
            "Environment" to primary(eidChoice.name)
        )
        info("Commit ${bold(".envmanager")} so your team shares the same project and environment.")
        info("Add ${bold(".envmanager.local")} to your ${bold(".gitignore")} — it contains your personal profile.")
        printHint("Next:", "envmanager run -- <your-command>")
    }
}
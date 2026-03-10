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
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import net.atlantisservices.envmaster.client.core.api.models.ApiResult
import net.atlantisservices.envmaster.client.core.api.storage.Storage
import net.atlantisservices.envmaster.client.core.api.storage.Storage.requireLocalConfig
import net.atlantisservices.envmaster.client.util.bold
import net.atlantisservices.envmaster.client.util.cliError
import net.atlantisservices.envmaster.client.util.green
import net.atlantisservices.envmaster.client.util.handle401OrError
import net.atlantisservices.envmaster.client.util.muted
import net.atlantisservices.envmaster.client.util.primary
import net.atlantisservices.envmaster.client.util.printHint
import net.atlantisservices.envmaster.client.util.printKV
import net.atlantisservices.envmaster.client.util.printTable
import net.atlantisservices.envmaster.client.util.success
import net.atlantisservices.envmaster.client.util.text
import net.atlantisservices.envmaster.client.util.warn
import net.atlantisservices.envmaster.client.util.withClient

class EnvironmentCommand : CliktCommand(
    name = "environment",
    help = "Set or show the environment for this directory.\n\nPass an ID, name, or slug to set it, or omit to print the current environment.\n\nReads and writes the .envmaster file in the current directory."
) {
    private val id        by argument("id", help = "Environment ID, name, or slug to select").optional()
    private val list      by option("--list", "-L", help = "List environments for the current project").flag()
    private val profile   by option("--profile", "-p", help = "Profile to use")
    private val projectId by option("--project", help = "Project ID or name (overrides value in .envmaster)")

    override fun run() {
        when {
            list -> {
                withClient(profile) { client ->
                    val pid = resolveProjectId(client)
                    listEnvironments(client, pid)
                }
            }
            id != null -> {
                requireLocalConfig()
                withClient(profile) { client ->
                    val pid = resolveProjectId(client)
                    val eid = client.resolveEnvironmentId(pid, id!!)
                    Storage.saveLocalConfig(Storage.loadLocalConfig().copy(environmentId = eid))
                    println()
                    success("Environment set to $eid in ${bold(".envmaster")}")
                }
            }
            else -> showCurrent()
        }
    }

    private suspend fun resolveProjectId(client: net.atlantisservices.envmaster.client.core.api.APIClient): Long {
        projectId?.let { return client.resolveProjectId(it) }
        return Storage.loadLocalConfig().projectId
            ?: cliError("No project set in ${bold(".envmaster")} — run ${bold("envmaster project <id|name>")} first.")
    }

    private fun showCurrent() {
        requireLocalConfig()
        val local = Storage.loadLocalConfig()
        val eid = local.environmentId
        if (eid == null) {
            println()
            warn("No environment set in ${bold(".envmaster")}.")
            printHint("Select an environment:", "envmaster environment --list", "envmaster environment <id|name>")
            return
        }
        val pid = local.projectId
        if (pid != null) {
            withClient(profile) { client ->
                when (val r = client.listEnvironments(pid)) {
                    is ApiResult.Success -> {
                        val e = r.data.find { it.id == eid }
                        if (e != null) printKV(
                            "Environment" to primary(e.name),
                            "ID"          to muted(e.id.toString()),
                        ) else {
                            println()
                            warn("Environment $eid no longer exists or you've lost access to it.")
                            Storage.saveLocalConfig(local.copy(environmentId = null))
                            printHint("Select a new environment:", "envmaster environment --list", "envmaster environment <id|name>")
                        }
                    }
                    is ApiResult.Error -> when (r.status) {
                        404 -> {
                            println()
                            warn("Project $pid no longer exists or you've lost access to it.")
                            Storage.saveLocalConfig(local.copy(projectId = null, environmentId = null))
                            printHint("Select a new project:", "envmaster project --list", "envmaster project <id|name>")
                        }
                        else -> handle401OrError(r)
                    }
                }
            }
        } else {
            printKV("Environment ID" to text(eid.toString()))
        }
    }

    private suspend fun listEnvironments(client: net.atlantisservices.envmaster.client.core.api.APIClient, pid: Long) {
        when (val r = client.listEnvironments(pid)) {
            is ApiResult.Success -> {
                val envs = r.data
                if (envs.isEmpty()) {
                    warn("No environments found for this project.")
                    return
                }
                val current = Storage.loadLocalConfig().environmentId
                printTable(
                    headers = listOf("", "ID", "Name"),
                    rows = envs.map { e ->
                        listOf(
                            if (e.id == current) green("•") else muted("◦"),
                            muted(e.id.toString()),
                            text(e.name),
                        )
                    }
                )
                printHint("Select an environment:", "envmaster environment <id|name>")
            }
            is ApiResult.Error -> {
                handle401OrError(r)
            }
        }
    }
}
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
import net.atlantisservices.envmaster.client.util.*

class ProjectCommand : CliktCommand(
    name = "project",
    help = "Set or show the project for this directory.\n\nPass an ID or name to set it, or omit to print the current project.\n\nReads and writes the .envmaster file in the current directory."
) {
    private val id      by argument("id", help = "Project ID or name to select").optional()
    private val list    by option("--list", "-L", help = "List all accessible projects").flag()
    private val profile by option("--profile", "-p", help = "Profile to use")

    override fun run() {
        when {
            list       -> listProjects()
            id != null -> setProject(id!!)
            else       -> showCurrent()
        }
    }

    private fun showCurrent() {
        requireLocalConfig()
        val pid = Storage.loadLocalConfig().projectId
        if (pid == null) {
            println()
            warn("No project set in ${bold(".envmaster")}.")
            printHint("Select a project:", "envmaster project --list", "envmaster project <id|name>")
            return
        }
        withClient(profile) { client ->
            when (val r = client.listProjects()) {
                is ApiResult.Success -> {
                    val p = r.data.find { it.id == pid }
                    if (p != null) printKV("Project" to primary(p.name), "ID" to muted(p.id.toString()))
                    else {
                        println()
                        warn("Project $pid no longer exists or you've lost access to it.")
                        Storage.saveLocalConfig(Storage.loadLocalConfig().copy(projectId = null))
                        printHint("Select a new project:", "envmaster project --list", "envmaster project <id|name>")
                    }
                }
                is ApiResult.Error -> handle401OrError<Unit>(r)
            }
        }
    }

    private fun setProject(input: String) {
        requireLocalConfig()
        withClient(profile) { client ->
            val pid = client.resolveProjectId(input)
            Storage.saveLocalConfig(Storage.loadLocalConfig().copy(projectId = pid))
            println()
            success("Project set to $pid in ${bold(".envmaster")}")
        }
    }

    private fun listProjects() {
        withClient(profile) { client ->
            when (val r = client.listProjects()) {
                is ApiResult.Success -> {
                    val projects = r.data
                    if (projects.isEmpty()) {
                        warn("You don't have any projects yet. Create one in the dashboard.")
                        return@withClient
                    }
                    val current = Storage.loadLocalConfig().projectId
                    printTable(
                        headers = listOf("", "ID", "Name", "Description"),
                        rows = projects.map { p ->
                            listOf(
                                if (p.id == current) green("•") else muted("◦"),
                                muted(p.id.toString()),
                                text(p.name),
                                muted(p.description.take(50).let { if (p.description.length > 50) "$it…" else it })
                            )
                        }
                    )
                    printHint("Select a project:", "envmaster project <id|name>")
                }
                is ApiResult.Error -> handle401OrError(r)
            }
        }
    }
}
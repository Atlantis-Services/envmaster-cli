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
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.option
import net.atlantisservices.envmaster.client.util.*

class RunCommand : CliktCommand(
    name = "run",
    help = "Fetch env vars and run a command with them injected."
) {
    private val profile     by option("--profile",     "-p")
    private val project     by option("--project")
    private val environment by option("--environment", "-e")
    private val cmd         by argument("CMD").multiple(required = true)

    override fun run() {
        println()
        info("Fetching variables…")
        val vars = fetchVars(profile, project, environment)
        info("Loaded ${text(vars.size.toString())} variable(s)")
        success("spawning process")
        println()
        runWithEnv(cmd, vars)
    }
}
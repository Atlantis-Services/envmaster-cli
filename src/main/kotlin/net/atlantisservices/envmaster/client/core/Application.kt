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

package net.atlantisservices.envmaster.client.core

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import net.atlantisservices.envmaster.client.core.commands.*
import net.atlantisservices.envmaster.client.util.SystemExit
import kotlin.system.exitProcess

class Client : CliktCommand(
    name = "envmaster",
    help = """
        Securely manage environment variables across projects and teams.
    """.trimIndent()
) {
    override fun run() = Unit

    init {
        subcommands(
            LoginCommand(),
            LogoutCommand(),
            WhoAmICommand(),
            ProfileCommand(),
            ProjectCommand(),
            EnvironmentCommand(),
            RunCommand(),
            InitCommand(),
            UninstallCommand(),
            UpdateCommand()
        )
    }
}

object Application {

    const val API_URL = "http://localhost:8080"

    fun start(args: Array<String>) {
        try {
            Client().main(args)
        } catch (e: SystemExit) {
            exitProcess(e.code)
        }
    }
}

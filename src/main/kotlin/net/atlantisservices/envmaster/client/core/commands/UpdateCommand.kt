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
import net.atlantisservices.envmaster.client.util.cliError
import net.atlantisservices.envmaster.client.util.info
import net.atlantisservices.envmaster.client.util.success
import net.atlantisservices.envmaster.client.util.warn

class UpdateCommand : CliktCommand(
    name = "update",
    help = "Update envmaster to the latest version"
) {
    override fun run() {
        println()
        info("Checking for updates...")

        val os = System.getProperty("os.name").lowercase()
        val cmd = when {
            os.contains("linux") || os.contains("mac") -> listOf(
                "sh", "-c",
                "curl -fsSL https://raw.githubusercontent.com/Atlantis-Services/envmaster-cli/master/install.sh | sh"
            )
            os.contains("win") -> listOf(
                "powershell", "-Command",
                "irm https://raw.githubusercontent.com/Atlantis-Services/envmaster-cli/master/install.ps1 | iex"
            )
            else -> { warn("Unsupported OS — update manually."); return }
        }

        val exit = ProcessBuilder(cmd)
            .inheritIO()
            .start()
            .waitFor()

        if (exit == 0) {
            println()
            success("envmaster has been updated. Restart your terminal if the version doesn't change.")
        } else {
            cliError("Update failed — try running the install script manually.")
        }
    }
}
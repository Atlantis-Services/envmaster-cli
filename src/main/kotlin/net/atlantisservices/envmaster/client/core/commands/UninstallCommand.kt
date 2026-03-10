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
import net.atlantisservices.envmaster.client.util.info
import net.atlantisservices.envmaster.client.util.promptText
import net.atlantisservices.envmaster.client.util.success
import net.atlantisservices.envmaster.client.util.warn

class UninstallCommand : CliktCommand(
    name = "uninstall",
    help = "Remove envmaster from your system"
) {
    override fun run() {
        println()
        warn("This will remove envmaster from your system.")
        val confirm = promptText("Type 'yes' to confirm")
        if (confirm != "yes") { info("Aborted."); return }

        val binary = ProcessHandle.current().info().command()
            .map { java.io.File(it) }.orElse(null)

        if (binary != null && binary.exists()) {
            binary.delete()
            success("Removed ${binary.absolutePath}")
        }

        val config = java.io.File(System.getProperty("user.home"), ".envmaster")
        if (config.exists()) {
            config.deleteRecursively()
            success("Removed ${config.absolutePath}")
        }

        println()
        info("envmaster has been uninstalled.")
    }
}
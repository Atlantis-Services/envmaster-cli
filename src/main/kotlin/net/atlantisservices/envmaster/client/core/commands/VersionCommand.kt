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

class VersionCommand : CliktCommand(
    name = "version",
    help = "Print the current envmaster version"
) {
    override fun run() {
        val version = javaClass.`package`?.implementationVersion
            ?: javaClass.getResourceAsStream("/version.txt")
                ?.bufferedReader()
                ?.readText()
                ?.trim()
            ?: "unknown"

        println()
        info("envmaster version $version")
    }
}
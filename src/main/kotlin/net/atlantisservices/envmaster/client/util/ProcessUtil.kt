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

package net.atlantisservices.envmaster.client.util

import net.atlantisservices.envmaster.client.core.api.models.VariableDTO
import java.io.File

fun runWithEnv(args: List<String>, vars: List<VariableDTO>) {
    if (args.isEmpty()) cliError("No command specified")

    val env = mutableMapOf<String, String>()
    env.putAll(System.getenv())

    for (v in vars) {
        if (v.value != "***") {
            env[v.key] = v.value
        }
    }

    val command = args.joinToString(" ")

    val processBuilder =
        if (System.getProperty("os.name").lowercase().contains("windows")) {
            ProcessBuilder("cmd", "/c", command)
        } else {
            ProcessBuilder("sh", "-c", command)
        }

    val process = processBuilder
        .directory(File(System.getProperty("user.dir")))
        .also { pb ->
            pb.environment().clear()
            pb.environment().putAll(env)
            pb.inheritIO()
        }
        .start()

    val exit = process.waitFor()
    if (exit != 0) throw SystemExit(exit)
}
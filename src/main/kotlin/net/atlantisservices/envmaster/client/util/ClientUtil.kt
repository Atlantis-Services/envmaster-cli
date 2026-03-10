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

import kotlinx.coroutines.runBlocking
import net.atlantisservices.envmaster.client.core.Application
import net.atlantisservices.envmaster.client.core.api.APIClient
import net.atlantisservices.envmaster.client.core.api.models.ApiResult
import net.atlantisservices.envmaster.client.core.api.models.VariableDTO
import net.atlantisservices.envmaster.client.core.api.storage.Storage

fun <T> withClient(profileName: String? = null, block: suspend (APIClient) -> T): T? {
    val name    = profileName ?: Storage.effectiveProfile()
    ?: run { cliError("Not logged in. Run '${bold("envmaster login")}' first.") }
    val profile = Storage.getProfile(name)
        ?: run { cliError("Profile '$name' not found. Run '${bold("envmaster login --profile $name")}'.") }

    val client = APIClient(Application.API_URL, profile.token)
    return try {
        runBlocking { block(client) }
    } catch (e: java.net.ConnectException) {
        cliError("Could not connect to API at ${client.baseUrl}. Is the server running?")
    } catch (e: SystemExit) {
        throw e
    } finally {
        client.close()
    }
}

fun fetchVars(
    profileName: String? = null,
    project: String? = null,
    environment: String? = null
): List<VariableDTO> = withClient(profileName) { client ->

    val local = Storage.loadLocalConfig()

    val pid = when {
        project != null -> client.resolveProjectId(project)
        else            -> local.projectId
            ?: cliError("No project set. Run '${bold("envmaster project <id|name>")}'.")
    }

    val eid = when {
        environment != null -> client.resolveEnvironmentId(pid, environment)
        else                -> local.environmentId
            ?: cliError("No environment set. Run '${bold("envmaster environment <id|name>")}'.")
    }

    when (val r = client.listVariables(pid, eid)) {
        is ApiResult.Success -> r.data
        is ApiResult.Error   -> handle401OrError(r)
    }
}!!

fun <T> handle401OrError(result: ApiResult.Error): T {
    when (result.status) {
        401 -> cliError("Session expired. Run '${bold("envmaster login")}' to re-authenticate.")
        403 -> cliError("Access denied. You don't have permission to access this resource.")
        404 -> cliError("Project or environment not found. Check your '${bold(".envmaster")}' config or run '${bold("envmaster project <id|name>")}' to set one.")
        else -> cliError("API error ${result.status}: ${result.error}")
    }
}
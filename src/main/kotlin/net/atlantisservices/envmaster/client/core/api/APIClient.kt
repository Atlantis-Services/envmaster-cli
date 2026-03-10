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

package net.atlantisservices.envmaster.client.core.api

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import net.atlantisservices.envmaster.client.core.api.models.*
import net.atlantisservices.envmaster.client.util.cliError
import net.atlantisservices.envmaster.client.util.handle401OrError

class APIClient(
    val baseUrl: String,
    private val token: String?
) {
    val http = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true; explicitNulls = false })
        }
        expectSuccess = false
    }

    fun HttpRequestBuilder.auth() {
        token?.let { header(HttpHeaders.Authorization, "Bearer $it") }
    }

    suspend inline fun <reified T> get(path: String): ApiResult<T> =
        http.get("$baseUrl$path") { auth() }.toResult()

    suspend inline fun <reified T> post(path: String): ApiResult<T> =
        http.post("$baseUrl$path") { auth() }.toResult()

    suspend fun getProfile(): ApiResult<UserDTO> =
        http.get("$baseUrl/v1/auth/profile") { auth() }.toResult()

    suspend fun logout(): ApiResult<Unit> {
        val resp = http.post("$baseUrl/v1/auth/logout") { auth() }
        return if (resp.status.isSuccess()) ApiResult.Success(Unit)
        else ApiResult.Error(resp.status.value, resp.status.description)
    }

    suspend fun listProjects(): ApiResult<List<ProjectDTO>> =
        http.get("$baseUrl/v1/projects") { auth() }.toResult()

    suspend fun listEnvironments(projectId: Long): ApiResult<List<EnvironmentDTO>> =
        http.get("$baseUrl/v1/projects/$projectId/environments") { auth() }.toResult()

    suspend fun listVariables(projectId: Long, environmentId: Long): ApiResult<List<VariableDTO>> =
        http.get("$baseUrl/v1/projects/$projectId/environments/$environmentId/variables") { auth() }.toResult()

    suspend fun resolveProjectId(input: String): Long {
        input.toLongOrNull()?.let { return it }

        when (val res = listProjects()) {
            is ApiResult.Success -> {
                val match = res.data.find { it.name.equals(input, true) }
                    ?: cliError("Project '$input' not found.")
                return match.id
            }
            is ApiResult.Error -> handle401OrError(res)
        }
    }

    suspend fun resolveEnvironmentId(projectId: Long, input: String): Long {
        input.toLongOrNull()?.let { return it }

        when (val res = listEnvironments(projectId)) {
            is ApiResult.Success -> {
                val match = res.data.find {
                    it.name.equals(input, true)
                } ?: cliError("Environment '$input' not found.")
                return match.id
            }
            is ApiResult.Error -> handle401OrError(res)
        }
    }

    fun close() = http.close()
}
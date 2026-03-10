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

package net.atlantisservices.envmaster.client.core.api.models

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val status: Int, val error: String) : ApiResult<Nothing>()
}

suspend inline fun <reified T> HttpResponse.toResult(): ApiResult<T> =
    if (status.isSuccess()) {
        ApiResult.Success(body<T>())
    } else {
        val msg = try { body<ErrorResponse>().message } catch (_: Exception) { status.description }
        ApiResult.Error(status.value, msg)
    }
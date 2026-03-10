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

package net.atlantisservices.envmaster.client.core.api.storage

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.atlantisservices.envmaster.client.util.cliError
import java.io.File

@Serializable
data class Profile(
    val email: String,
    val token: String,
)

/** ~/.envmanager/profiles.json */
@Serializable
data class ProfilesFile(
    val profiles: Map<String, Profile> = emptyMap(),
    val default: String? = null
)

/**
 * .envmanager — shared/committed project config.
 * Contains only values safe for the whole team to share.
 */
data class LocalConfig(
    val projectId: Long? = null,
    val environmentId: Long? = null,
)

/**
 * .envmanager.local — personal, machine-specific config. Should be .gitignored.
 * Contains per-developer overrides such as which auth profile to use.
 */
data class LocalUserConfig(
    val profile: String? = null,
)

object Storage {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    private val homeDir      = File(System.getProperty("user.home"))
    private val configDir    = File(homeDir, ".envmanager").also { it.mkdirs() }
    private val profilesFile = File(configDir, "profiles.json")
    private val localCfgFile = File(".envmanager")
    private val localUserCfgFile = File(".envmanager.local")

    fun loadProfiles(): ProfilesFile =
        profilesFile.readSafe { json.decodeFromString(it) } ?: ProfilesFile()

    fun saveProfiles(data: ProfilesFile) =
        profilesFile.writeText(json.encodeToString(data))

    fun getProfile(name: String? = null): Profile? {
        val data = loadProfiles()
        val key  = name ?: data.default ?: return null
        return data.profiles[key]
    }

    fun getActiveProfileName(): String? = loadLocalUserConfig().profile ?: loadProfiles().default

    fun addProfile(name: String, profile: Profile) {
        val data = loadProfiles()
        saveProfiles(data.copy(
            profiles = data.profiles + (name to profile),
            default  = data.default ?: name
        ))
    }

    fun removeProfile(name: String): Boolean {
        val data = loadProfiles()
        if (!data.profiles.containsKey(name)) return false
        val newDefault = if (data.default == name)
            data.profiles.keys.firstOrNull { it != name }
        else data.default
        saveProfiles(data.copy(profiles = data.profiles - name, default = newDefault))
        return true
    }

    fun loadLocalConfig(): LocalConfig {
        if (!localCfgFile.exists()) return LocalConfig()
        return try {
            val props = parseKeyValueFile(localCfgFile)
            LocalConfig(
                projectId     = props["projectId"]?.toLongOrNull(),
                environmentId = props["environmentId"]?.toLongOrNull(),
            )
        } catch (_: Exception) { LocalConfig() }
    }

    fun saveLocalConfig(config: LocalConfig) {
        val lines = buildList {
            add("# EnvManager project config — commit this file")
            config.projectId?.let     { add("projectId=$it") }
            config.environmentId?.let { add("environmentId=$it") }
        }
        localCfgFile.writeText(lines.joinToString("\n") + "\n")
    }

    fun loadLocalUserConfig(): LocalUserConfig {
        if (!localUserCfgFile.exists()) return LocalUserConfig()
        return try {
            val props = parseKeyValueFile(localUserCfgFile)
            LocalUserConfig(
                profile = props["profile"],
            )
        } catch (_: Exception) { LocalUserConfig() }
    }

    fun saveLocalUserConfig(config: LocalUserConfig) {
        val lines = buildList {
            add("# EnvManager personal config — do NOT commit this file")
            config.profile?.let { add("profile=$it") }
        }
        localUserCfgFile.writeText(lines.joinToString("\n") + "\n")
    }

    fun effectiveProfile(): String? = loadLocalUserConfig().profile ?: loadProfiles().default

    private fun parseKeyValueFile(file: File): Map<String, String> =
        file.readLines()
            .filter { '=' in it && !it.trimStart().startsWith('#') }
            .associate { line ->
                val idx = line.indexOf('=')
                line.substring(0, idx).trim() to line.substring(idx + 1).trim()
            }

    private fun <T> File.readSafe(parse: (String) -> T): T? =
        if (exists()) try { parse(readText()) } catch (_: Exception) { null } else null

    fun requireLocalConfig() {
        if (!File(".envmanager").exists())
            cliError("No .envmanager file found in the current directory. Are you inside a project?")
    }

}
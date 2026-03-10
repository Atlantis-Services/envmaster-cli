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

import kotlinx.coroutines.delay

private val COLOR: Boolean =
    System.getenv("NO_COLOR").isNullOrBlank() &&
            System.getenv("TERM") != "dumb" &&
            System.console() != null

private fun esc(code: String, s: String) =
    if (COLOR) "\u001B[${code}m$s\u001B[0m" else s

private fun rgb(r: Int, g: Int, b: Int, s: String) =
    if (COLOR) "\u001B[38;2;$r;$g;${b}m$s\u001B[0m" else s

private val ANSI_RE = Regex("\u001B\\[[0-9;]*m")
fun visLen(s: String)         = ANSI_RE.replace(s, "").length
fun padVis(s: String, w: Int) = s + " ".repeat(maxOf(0, w - visLen(s)))

fun primary(s: String)   = rgb( 79,  26, 214, s)
fun text(s: String)      = rgb(165, 173, 184, s)
fun muted(s: String)     = rgb(135, 141, 149, s)
fun successFg(s: String) = rgb( 54, 181, 122, s)
fun warningFg(s: String) = rgb(207, 166,  60, s)
fun errorFg(s: String)   = rgb(196,  78,  90, s)
fun infoFg(s: String)    = rgb( 79, 152, 224, s)

fun bold(s: String)    = esc("1", s)
fun dim(s: String)     = muted(s)
fun green(s: String)   = successFg(s)
fun yellow(s: String)  = warningFg(s)
fun red(s: String)     = errorFg(s)
fun cyan(s: String)    = infoFg(s)
fun blue(s: String)    = infoFg(s)
fun magenta(s: String) = primary(s)

fun success(msg: String) = println("  ${successFg("✓")}  $msg")
fun warn(msg: String)    = println("  ${warningFg("⚠")}  $msg")
fun info(msg: String)    = println("  ${muted("›")}  $msg")

fun cliError(msg: String): Nothing {
    System.err.println("\n  ${errorFg("✗")}  $msg")
    throw SystemExit(1)
}

class SystemExit(val code: Int) : Exception()

class Spinner(private val label: String) {
    private val frames = listOf("⠋","⠙","⠹","⠸","⠼","⠴","⠦","⠧","⠇","⠏")
    private var tick   = 0

    fun tick() {
        print("\r  ${primary(frames[tick % frames.size])}  ${muted(label)}   ")
        System.out.flush()
        tick++
    }

    fun stop() {
        if (COLOR) print("\r\u001B[2K") else println()
        System.out.flush()
    }
}

suspend fun <T : Any> Spinner.waitFor(
    intervalMs: Long = 2_000,
    block: suspend () -> T?,
): T {
    while (true) {
        delay(intervalMs)
        tick()
        val result = block()
        if (result != null) {
            stop()
            return result
        }
    }
}

fun printTable(headers: List<String>, rows: List<List<String>>) {
    val cols   = headers.size
    val widths = (0 until cols).map { col ->
        maxOf(
            visLen(headers[col]),
            rows.maxOfOrNull { visLen(it.getOrElse(col) { "" }) } ?: 0,
        )
    }

    fun bar(l: String, mid: String, r: String) =
        text(l + widths.joinToString(text(mid)) { "─".repeat(it + 2) } + r)

    fun row(cells: List<String>, isHeader: Boolean = false): String {
        val parts = cells.mapIndexed { i, cell ->
            val padded = padVis(cell, widths[i])
            " ${if (isHeader) bold(text(padded)) else padded} "
        }
        return text("│") + parts.joinToString(text("│")) + text("│")
    }

    println()
    println(bar("┌", "┬", "┐"))
    println(row(headers, isHeader = true))
    println(bar("├", "┼", "┤"))
    rows.forEach { println(row(it)) }
    println(bar("└", "┴", "┘"))
}

fun printKV(vararg pairs: Pair<String, String>) {
    val keyWidth = pairs.maxOf { it.first.length }
    println()
    pairs.forEach { (k, v) ->
        println("  ${muted(k.padEnd(keyWidth))}  $v")
    }
    println()
}

fun printDivider(label: String? = null) {
    if (label == null) {
        println("  ${muted("─".repeat(48))}")
    } else {
        println("  ${muted("───  $label  ───")}")
    }
}

fun printHint(label: String, vararg commands: String) {
    println()
    println("  ${muted(label)}")
    commands.forEach { println("  ${muted("  $it")}") }
    println()
}

fun promptText(label: String, default: String? = null): String {
    val suffix = if (default != null) muted(" ($default)") else ""
    print("  ${text(label)}$suffix  ")
    val line = readLine()?.trim().orEmpty()
    return line.ifEmpty { default.orEmpty() }
}
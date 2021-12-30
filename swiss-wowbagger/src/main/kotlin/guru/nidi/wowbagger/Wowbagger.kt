/*
 * Copyright © 2018 Stefan Niederhauser (nidin@gmx.ch)
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
 */
package guru.nidi.wowbagger

import guru.nidi.wowbagger.Gender.*
import guru.nidi.wowbagger.Number.SINGULAR
import java.util.*
import kotlin.math.min

object Wowbagger {
    fun adjective(gender: Gender, number: Number) = Adjectives.list.choose().with(gender, number)
    fun subject(gender: Gender? = null, number: Number) = Subjects.list.with(gender).choose().with(number)
    fun name(gender: Gender? = null) = Names.list.with(gender).choose().with(SINGULAR)
    fun name(target: String): Pair<Entry<Gendered>, Int> {
        val best = Names.list.minByOrNull { s -> different(target.lowercase(), s.entry.name.lowercase()) }!!
        return Pair(best, different(target.lowercase(), best.entry.name.lowercase()))
    }

    fun action(number: Number) = Actions.list.choose().with(number)
    fun interjection() = Interjections.list.choose()

    internal fun String.trimLines() = lines().filter { it.isNotBlank() }

    private fun <T> List<T>.choose() = this[random(size)]

    private fun List<Entry<Gendered>>.with(gender: Gender?) = filter { gender == null || gender == it.entry.gender }
}

enum class Gender {
    M, F, N;

    companion object {
        fun random() = if (random(2) == 0) M else F
    }
}

enum class Number {
    SINGULAR, PLURAL;

    companion object {
        fun of(count: Int) = if (count == 1) SINGULAR else PLURAL
    }
}

class Entry<out T>(val entry: T, val phonemes: String) {
    override fun toString() = entry.toString()

    companion object {
        fun phonemes(phonemes: String) = of("|$phonemes") { it }
        fun <T> of(line: String, converter: (String) -> T): Entry<T> {
            val pos = line.indexOf('|')
            return if (pos < 0) Entry(converter(line), "")
            else Entry(converter(line.substring(0, pos).trim()), line.substring(pos + 1))
        }
    }
}

fun List<Entry<String>>.toText(forDisplay: Boolean = true): String =
    joinToString(" ") { it.entry }
        .replace(Regex("\\s+"), " ")
        .replace(Regex(" ([,.!?])"), "$1")
        .replaceFirstChar { if (forDisplay && it.isLowerCase()) it.titlecase() else it.toString() }

fun List<Entry<String>>.toPhonemes(): String = joinToString(" ") { it.phonemes }

fun <T> List<Entry<T>>.interleave(between: Entry<T>): List<Entry<T>> =
    if (isEmpty()) this
    else zipWithNext { a, _ -> listOf(a, between) }.flatten() + last()

fun <T> List<Entry<T>>.enumerate(penultimate: Entry<T>): List<Entry<T>> = when (size) {
    0 -> listOf()
    1 -> listOf(this[0])
    else -> subList(0, size - 1) + penultimate + last()
}

interface Numbered {
    val name: String
}

fun Entry<Numbered>.with(number: Number): Entry<String> {
    val index = if (number == SINGULAR) 1 else 2
    return Entry(replaceParens2(entry.name, index), replaceParens2(phonemes, index))
}

data class Gendered(override val name: String, val gender: Gender) : Numbered {
    constructor(s: String) : this(s.substring(2), valueOf(s.substring(0, 1).uppercase()))

    override fun toString() = name
}

data class Action(override val name: String) : Numbered

data class Adjective(val name: String)

fun Entry<Adjective>.with(gender: Gender, number: Number): Entry<String> {
    val g = if (number == SINGULAR) gender else M
    return if (entry.name.contains("("))
        Entry(replaceParens(entry.name, g), replaceParens(phonemes, g))
    else
        Entry(
            entry.name + when (g) {
                M -> "e"
                N -> "s"
                F -> "i"
            }, phonemes + when (g) {
                M -> " @"
                N -> " s 200"
                F -> " i"
            }
        )
}

private fun replaceParens(s: String, gender: Gender) = replaceParens3(
    s, when (gender) {
        M -> 1
        N -> 2
        F -> 3
    }
)

private fun replaceParens2(s: String, index: Int) = s.replace(Regex("\\((.*?)/(.*?)\\)"), "$$index")
private fun replaceParens3(s: String, index: Int) = s.replace(Regex("\\((.*?)/(.*?)/(.*?)\\)"), "$$index")

private val rnd = Random()
fun randomSeed(seed: Long) = rnd.setSeed(seed)
fun random(range: Int) = rnd.nextInt(range)

private fun different(s1: String, s2: String): Int {
    val first = s1.first() == s2.first()
    val second = s1.length >= 2 && s2.length >= 2 && s1[1] == s2[1]
    return 2 * levenshtein(s1, s2) - (if (first) 3 else 0) - (if (second) 1 else 0)
}

private fun levenshtein(s1: String, s2: String): Int {
    val edits = Array(s1.length + 1) { IntArray(s2.length + 1) }
    for (i in 0..s1.length) edits[i][0] = i
    for (i in 1..s2.length) edits[0][i] = i
    for (i in 1..s1.length) {
        for (j in 1..s2.length) {
            val u = if (s1[i - 1] == s2[j - 1]) 0 else 1
            edits[i][j] = min(
                edits[i - 1][j] + 1,
                min(edits[i][j - 1] + 1, edits[i - 1][j - 1] + u)
            )
        }
    }
    return edits[s1.length][s2.length]
}

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

import guru.nidi.mbrola.*
import java.io.File

private val voiceFileName = System.getenv("WOWBAGGER_VOICE_FILE") ?: "/usr/share/mbrola/nl2/nl2"

object WowbaggerVoice {
    // TODO Voice always copies voice files to tmpdir which is bad on Docker (uses RAM)
    fun say(phonemes: String, format: Format = Format.WAV, speed: Double = .7) =
        Mbrola(Phonemes.fromString(phonemes), Voice.fromFile(File(voiceFileName)), format).time(speed)
            .run()
}

/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the ""License"");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an ""AS IS"" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License."
 */

// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    // Actualizado a AGP 9.4.1 / Kotlin 2.2.10 (el video usa AGP 8.8.0 / Kotlin 2.1.0) para
    // compilar con el JDK 25 de Android Studio. AGP 9 incluye Kotlin integrado, por eso ya no
    // se declara el plugin org.jetbrains.kotlin.android.
    id("com.android.application") version "9.4.1" apply false
    id("com.android.library") version "9.4.1" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10" apply false
    // En el video: 2.1.0. Debe coincidir con la versión de Kotlin del proyecto.
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.10" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

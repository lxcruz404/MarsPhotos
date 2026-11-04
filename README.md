# Mars Photos: Retrofit + Kotlin Serialization + Coil (MVVM)

Reproducción del tutorial
[«Cómo consumir una API en Android con Retrofit + Kotlin Serialization + Coil | MVVM paso a paso»](https://www.youtube.com/watch?v=-2lki3yYTbA).

La app consume `https://android-kotlin-fun-mars-server.appspot.com/photos`, deserializa el JSON en
objetos `MarsPhoto` y muestra las 838 fotos en una cuadrícula. Tiene estados de carga, éxito y error,
y un botón para reintentar.

Proyecto base: rama `starter` de
[google-developer-training/basic-android-kotlin-compose-training-mars-photos](https://github.com/google-developer-training/basic-android-kotlin-compose-training-mars-photos)
(el README original de ese repositorio se conserva en `README_codelab_original.md`).

## Requisitos

| Herramienta | Versión usada |
|---|---|
| Android Studio | con JBR (OpenJDK) 25 |
| Gradle (wrapper) | 9.6.0 |
| Android Gradle Plugin | 9.4.1 |
| Kotlin (Compose y Serialization plugins) | 2.2.10 |
| compileSdk / targetSdk | 37 (minSdk 24) |
| Emulador | `Medium_Phone_API_37.0` |

## Abrir y ejecutar

1. Android Studio → **File › Open** → `C:\Users\Usuario\AndroidStudioProjects\MarsPhotos`.
2. Espera a que termine **Gradle Sync** (descarga Retrofit, kotlinx-serialization, Coil...).
3. Elige el emulador y pulsa **Run ▶** (configuración `app`).

Desde la terminal:

```bash
./gradlew assembleDebug
```

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

```bash
adb shell am start -n com.example.marsphotos/.MainActivity
```

> Si compilas desde la terminal, `JAVA_HOME` debe apuntar al JDK de Android Studio
> (`C:\Program Files\Android\Android Studio\jbr`) y `local.properties` debe tener `sdk.dir`.

## Cómo probarlo

| Prueba | Cómo reproducirla |
|---|---|
| Carga (Loading) | Abre la app; con red lenta se ve el indicador circular y «Cargando». |
| Éxito | Aparece la cuadrícula de fotos. |
| Error | Activa el modo avión y abre la app: ícono de nube tachada, «Error...» y botón **Reintentar**. |
| Reintento | Desactiva el modo avión y pulsa **Reintentar**: vuelve a «Cargando» y luego muestra las fotos. |

Para ver el estado de carga más tiempo en el emulador, apaga el WiFi y limita la red celular:

```bash
adb shell svc wifi disable
```

```bash
adb emu network speed gprs
```

Para volver a la normalidad: `adb emu network speed full` y `adb shell svc wifi enable`.

## Estructura final

```
app/src/main/java/com/example/marsphotos/
├── MainActivity.kt               Punto de entrada: tema + MarsPhotosApp()
├── MarsPhotosApplication.kt      Application personalizada: crea el AppContainer (onCreate)
├── data/                         ── Data layer
│   ├── AppContainer.kt           AppContainer (interfaz) + DefaultAppContainer:
│   │                             baseUrl, Retrofit (Json.asConverterFactory), MarsApiService, repositorio
│   └── MarsPhotosRepository.kt   MarsPhotosRepository (interfaz) + NetworkMarsPhotosRepository
├── network/                      ── Data source (Retrofit)
│   ├── MarsApiService.kt         @GET("photos") suspend fun getPhotos(): List<MarsPhoto>
│   └── MarsPhoto.kt              @Serializable data class (id, @SerialName("img_src") imgSrc)
└── ui/                           ── UI layer
    ├── MarsPhotosApp.kt          Scaffold + viewModel(factory = MarsViewModel.Factory)
    ├── screens/
    │   ├── MarsViewModel.kt      sealed interface MarsUiState, getMarsPhotos(), retryAction(), Factory
    │   └── HomeScreen.kt         HomeScreen (when), LoadingScreen, ErrorScreen, PhotosGridScreen, MarsPhotoCard
    └── theme/                    (sin cambios del proyecto base)
```

Flujo: `HomeScreen` ← `MarsViewModel` ← `MarsPhotosRepository` ← `MarsApiService` (Retrofit).
El ViewModel no conoce Retrofit: recibe el repositorio por constructor, y el `Factory` lo saca de
`MarsPhotosApplication.container`.

Recursos gráficos del proyecto base que se usan: `ic_connection_error` (ErrorScreen),
`loading_img` (placeholder de Coil) e `ic_broken_image` (imagen de error de Coil).

## Etapas implementadas frente al video

| # | Etapa | Minuto del video | Implementación |
|---|---|---|---|
| 1 | Clonar el repositorio base y revisar MainActivity, MarsPhotosApp, MarsViewModel y HomeScreen | 02:52 – 05:19 | rama `starter` |
| 2 | Dependencias de Retrofit + converter Scalars | 05:19 – 06:57 | `app/build.gradle.kts` |
| 2 | Paquete `network`, `BASE_URL`, `Retrofit.Builder`, `MarsApiService`, `object MarsApi` con `by lazy` | 06:57 – 13:00 | `network/MarsApiService.kt` (después se simplifica en la etapa 7) |
| 2 | `viewModelScope.launch` + llamada desde el ViewModel | 13:00 – 16:10 | `MarsViewModel.getMarsPhotos()` |
| 3 | Cierre por falta de permiso → `uses-permission INTERNET` | 16:26 – 17:25 | `AndroidManifest.xml` |
| 3 | Modo avión: la app se cierra | 18:00 – 18:47 | reproducido (cierre por `UnknownHost`) |
| 4 | `try/catch` mostrando el mensaje de error | 18:47 – 19:58 | captura `02_trycatch_mensaje.png` |
| 4 | `sealed interface MarsUiState` (Success/Error/Loading), `when` en HomeScreen, LoadingScreen, ErrorScreen con `ic_connection_error` | 19:58 – 31:20 | `MarsViewModel.kt`, `HomeScreen.kt` |
| 5 | Plugin `kotlin.plugin.serialization`, dependencias, `MarsPhoto` con `@Serializable` y `@SerialName("img_src")`, `Json.asConverterFactory("application/json".toMediaType())`, `List<MarsPhoto>` | 31:20 – 38:00 | `network/MarsPhoto.kt`, `MarsApiService.kt` |
| 5 | Mostrar cuántas fotos llegan (838) | 38:00 – 39:40 | captura `04_serialization_838.png` |
| 6 | Paquete `data`, `MarsPhotosRepository` + `NetworkMarsPhotosRepository`; el ViewModel usa el repositorio | 39:40 – 44:50 | `data/MarsPhotosRepository.kt` |
| 7 | `AppContainer` / `DefaultAppContainer` (Retrofit y servicio dentro del contenedor), se elimina `object MarsApi`, el repositorio recibe `MarsApiService` por constructor | 44:50 – 54:30 | `data/AppContainer.kt` |
| 7 | `MarsPhotosApplication` + `android:name` en el manifiesto | 54:30 – 57:45 | `MarsPhotosApplication.kt` |
| 7 | ViewModel con repositorio en el constructor → cierre «Cannot create an instance of MarsViewModel» → `companion object Factory` con `APPLICATION_KEY` → `viewModel(factory = ...)` | 57:45 – 1:06:30 | reproducido el cierre y luego corregido |
| 8 | Coil: una sola foto (`listResult.first()`), `AsyncImage` con `model`, `contentDescription`, `error`, `placeholder`, `ContentScale.Crop` | 1:06:39 – 1:14:00 | captura `05_coil_una_foto.png` |
| 9 | Todas las fotos: `LazyVerticalGrid(GridCells.Adaptive(150.dp))`, `padding(horizontal = 4.dp)`, `contentPadding`, `items(key = { it.id })`, `padding(4.dp).fillMaxWidth().aspectRatio(1.5f)`, `Card` con `cardElevation(8.dp)` | 1:14:00 – 1:21:30 | `PhotosGridScreen`, `MarsPhotoCard` |
| 10 | Botón **Reintentar** en ErrorScreen, `retryAction` elevado hasta HomeScreen/MarsPhotosApp, `retryAction()` en el ViewModel (`Loading` → `getMarsPhotos()`) | 1:21:30 – 1:27:20 | `ErrorScreen`, `MarsViewModel.retryAction()` |

## Diferencias respecto al video (y por qué)

- **Versiones de herramientas.** El repositorio base usa AGP 8.8.0, Gradle 8.12, Kotlin 2.1.0 y
  compileSdk 35. Gradle 8.12 no corre con el JDK 25 de este Android Studio y solo está instalado el
  SDK 37. Por eso se actualizó a AGP 9.4.1, Gradle 9.6.0, Kotlin 2.2.10 y compileSdk/targetSdk 37.
  - AGP 9 trae Kotlin integrado, así que se quitó `org.jetbrains.kotlin.android` y el bloque
    `kotlinOptions`. Java pasó de 1.8 a 11.
  - Se quitaron de `gradle.properties` las propiedades `android.defaults.buildfeatures.buildconfig`
    y `android.nonFinalResIds`, que AGP 9 ya no admite.
  - El plugin de serialización usa 2.2.10 en vez de 2.1.0, porque tiene que coincidir con la
    versión de Kotlin.
- **Dependencias.** Retrofit y converter-scalars 2.11.0 (la misma versión para ambos, como pide el
  profesor), `kotlinx-serialization-json` 1.8.0, `retrofit2-kotlinx-serialization-converter` 1.0.0
  de Jake Wharton, `okhttp` 4.12.0 y `coil-compose` 2.7.0. Son las versiones de la rama `main`
  (solución) del mismo repositorio. Las versiones exactas que se ven en pantalla en el video no se
  pudieron comprobar (ver abajo).

Todo lo demás sigue la transcripción: `AsyncImage(model = photo.imgSrc, contentDescription = null,
Modifier.fillMaxSize(), error, placeholder, ContentScale.Crop)`, un solo `catch (e: IOException)` y
los textos «Cargando», «Error...» y «Reintentar» escritos directamente en los composables
(`strings.xml` se dejó como en el proyecto base).

## Lo que no se pudo verificar visualmente

El desarrollo se hizo a partir de la transcripción, sin ver la imagen del video. Por eso:

- La transcripción no dice la URL del repositorio que clona el profesor. Se usó el codelab de Google
  porque coinciden los nombres de clases y recursos (`MarsViewModel`, `HomeScreen`,
  `ic_connection_error`, `ic_broken_image`, `loading_img`) y Kotlin 2.1.0. Su versión inicial
  muestra «Bienvenidos a la app» y la de Google «Set the Mars API status response here!»: puede que
  use una copia propia del repositorio.
- No se pudo confirmar cuáles son exactamente «las tres» dependencias de serialización ni las
  versiones de Retrofit que se ven en pantalla.
- Detalles de estilo como el padding exacto del texto de error o el `Spacer` del Loading pueden
  variar un poco.

## Resultados de las pruebas (emulador `Medium_Phone_API_37.0`, Android 17 / API 37)

| # | Prueba | Resultado | Evidencia |
|---|---|---|---|
| 1 | La aplicación abre | ✅ proceso vivo, sin cierres | `docs/capturas/07_grid_fotos.png` |
| 2 | Indicador de carga | ✅ «Cargando» + `CircularProgressIndicator` (red GPRS) | `06_loading.png` |
| 3 | Datos de la API | ✅ 838 elementos recibidos | `04_serialization_838.png` |
| 4 | Deserialización | ✅ `MarsPhoto(id=424905, imgSrc=https://mars.jpl.nasa.gov/...)` | `Log.d` temporal en Logcat durante el desarrollo (luego se quitó para dejar el código como el del video) |
| 5 | Se muestran las fotos | ✅ cuadrícula de 2 columnas en tarjetas | `07_grid_fotos.png` |
| 6 | Imágenes con Coil | ✅ placeholder `loading_img` → foto, a medida que se desplaza la cuadrícula | `05_coil_una_foto.png`, `08_lazy_placeholder.png` |
| 7 | Sin Internet no se cierra | ✅ proceso vivo en modo avión | — |
| 8 | Pantalla de error | ✅ ícono + «Error...» + «Reintentar» | `09_error_reintentar.png` |
| 9 | Botón Reintentar | ✅ sin red vuelve a Error (sin cerrarse); con red pasa a «Cargando» | `10_reintentar_loading.png` |
| 10 | Al recuperar Internet vuelven las fotos | ✅ 838 fotos cargadas en el mismo proceso tras Reintentar | `11_reintentar_ok.png` |

Nota sobre la prueba 9 sin red: el fallo de DNS es inmediato, así que el paso por Loading dura
milisegundos y no se pudo capturar. Sí se comprobó que la app termina otra vez en Error y no se cierra.

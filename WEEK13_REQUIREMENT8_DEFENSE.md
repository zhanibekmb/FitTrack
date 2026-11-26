# Week 13 Requirement 8 Defense

## My individual contribution

I am Akniyet, Student 2. I connected FitTrack to the wger exercise catalog and integrated it with the workout plans I built for Week 12. Users can search for an online exercise, select it, set workout targets, and record a workout using that exercise. I also wrote tests for the API, screen state, and plan handoff.

The branch `feature/exercise-catalog` starts at `07b9b6f`, the merge of the team's settings and shared infrastructure work. The existing plan, workout, history, and DataStore screens still use the team's shared data and navigation.

## Requirement 8 checklist

| # | Assignment requirement | Where it works |
| --- | --- | --- |
| 1 | Relevant public REST API | wger serves fitness exercises over HTTPS without an API key. |
| 2 | Retrofit HTTP requests | Retrofit sends requests to `https://wger.de/api/v2/`. |
| 3 | Retrofit API interface | `ExerciseApiService` declares the exercises and muscle endpoints. |
| 4 | JSON to Kotlin data classes | Gson reads the paginated response, exercises, translations, muscles, equipment, images, and licenses. |
| 5 | Repository | `ExerciseRepository` validates responses and maps exercises for the app. |
| 6 | Kotlin Coroutines | Retrofit's suspend calls run inside coroutines launched by the ViewModel. |
| 7 | Repository connected to a ViewModel | `ExerciseViewModel` receives the catalog repository from `FitTrackContainer`. |
| 8 | StateFlow | A private `MutableStateFlow` publishes updates through a read-only `StateFlow`. |
| 9 | Real API data in Jetpack Compose | The navigation destination collects state with `collectAsStateWithLifecycle`, and the catalog renders it in a `LazyColumn`. |
| 10 | Loading, Success, Error, and Empty | The screen has a spinner, exercise rows, an error with recovery, and separate empty messages. |
| 11 | Working Retry | Retry sends the failed request again, with the current search, filter, and page. |
| 12 | Coil exercise images | Coil `AsyncImage` displays the wger image and uses a placeholder while loading and when an image is missing or fails. |

## Why wger

wger is a public fitness catalog with English exercise names, muscle groups, equipment, images, and per-item credits. The public endpoints work without authentication. This fits FitTrack because the exercises can be selected directly for a workout plan. [wger API documentation](https://wger.readthedocs.io/en/latest/api/api.html)

I checked the real `exerciseinfo/` response before writing the Kotlin classes. The page has `count`, `next`, `previous`, and `results`. Each exercise has a numeric ID. English has language ID 2, and translations arrive as an array of objects. The English description can be HTML or empty. Some exercises have no image, equipment, or primary muscle. When muscles are missing, the display uses the exercise category if present. The server accepts `language__code=en`, `name__search`, `muscles`, `ordering=id`, `limit`, and `offset`. The muscle choices come from wger's `muscle/` endpoint. [OpenAPI schema](https://wger.de/api/v2/schema) · [English language record](https://wger.de/api/v2/language/2/)

wger assigns licenses to individual records. The **Credits** button shows the exercise source, credited authors, and its license when supplied. For a displayed image it also shows the image's own authors, source, and license. Images without a recognized license or a valid wger URL use the placeholder. The app displays the thumbnails or originals supplied by wger. Full exercise instructions are available on the linked exercise page. [wger licensing](https://github.com/wger-project/wger#license) · [wger license list](https://wger.de/api/v2/license/)

## How the code works

- **Retrofit and the API service.** Retrofit turns the service interface into HTTP calls. The service uses GET and exposes suspend functions. The client sets connection, read, and call timeouts. The app has Android internet permission.
- **JSON and DTOs.** Gson maps JSON keys to Kotlin properties. Nullable DTO fields represent fields wger may omit. The repository uses separate app models for display and stores only the workout metadata that the plan needs.
- **Repository.** The repository checks HTTP status, the response body, and the pagination offset. It picks a named English translation, skips records without one, and prefers the main image when it has a usable source and license.
- **Coroutines and the ViewModel.** `viewModelScope` owns catalog requests. Typing starts a short debounce and cancels older work. A late result from a cancelled request cannot change the current screen. Request guards prevent two retries or two page requests from running for the same state.
- **StateFlow and Compose.** The ViewModel publishes the query, filters, and content state. Navigation collects the flow while the screen is active. Compose uses those values to show the search field, filter menu, exercise cards, and status controls.
- **Loading, Success, Error, and Empty.** Loading shows a spinner. Success shows wger rows and a **Load more** button when there is another page. Error shows a useful message and **Retry**. Empty shows separate messages for an empty catalog, an unmatched search, and an unmatched muscle filter. If the next page fails, the existing rows stay visible.
- **Retry.** An initial error retries page one. A pagination error retries that page with the same query and filter. Search and filter changes start at page one.
- **Coil.** `AsyncImage` requests the thumbnail if wger supplies one, with the original image as the source when no thumbnail URL is available. Coil uses a placeholder before the image loads and when the load fails.
- **Workout plans.** **Add Exercise** opens the online catalog. **Add Saved Exercise** opens the existing picker for built-in and previously added exercises. Both paths use the same target fields and validation. At save time, the workout repository registers the selected exercise and adds its targets to the plan in one state update.

## Metadata handoff for Room

The shared ID for wger exercise 73 is `wger:73`. The shared `Exercise` record contains that ID, the English name, muscle group, and equipment. Plans and completed sets refer to the ID. The workout and history screens resolve it from the shared exercise list, including while offline after selection. Repeat selections reuse the existing exercise record and can add more target rows.

For a future Room implementation, keep the same ID and metadata available wherever plans and history are loaded. Save the shared exercise metadata with its plan reference so the exercise can still be resolved offline after process restart. This branch uses the current in-memory workout repository. Room and DataStore setup continue to belong to the team's Requirement 9 work.

## Files created and modified

Kotlin paths in this table are relative to `app/src/main/java/com/example/fittrack/` unless another source set is named.

| File | Change and purpose |
| --- | --- |
| `gradle/libs.versions.toml` | Adds aliases for Retrofit, Gson conversion, Coil Compose, Coil's OkHttp transport, OkHttp, MockWebServer, and the existing coroutine test dependency. |
| `app/build.gradle.kts` | Connects the version catalog aliases to the app and JVM tests. |
| `app/src/main/AndroidManifest.xml` | Adds Android internet permission. |
| `gradlew` | Marks the Unix wrapper executable so the requested `./gradlew` commands can run directly. |
| `data/remote/dto/PaginatedResponse.kt` | Defines the generic wger page envelope. |
| `data/remote/dto/ExerciseDto.kt` | Defines exercise, translation, muscle, equipment, image, and license JSON types. |
| `data/remote/ExerciseApiService.kt` | Declares Retrofit requests and query parameters for exercises and muscles. |
| `data/remote/RetrofitClient.kt` | Configures the base URL, strict Gson converter, and OkHttp timeouts. |
| `data/model/CatalogExercise.kt` | Stores mapped exercise, image, attribution, and filter data. Converts a selection to shared workout metadata. |
| `data/repository/ExerciseRepository.kt` | Loads and maps wger pages, detects bad responses, and builds filter choices. |
| `data/repository/FitTrackContainer.kt` | Exposes a shared catalog repository. |
| `ui/exercises/ExerciseUiState.kt` | Defines screen content and the kinds of request error. |
| `ui/exercises/ExerciseViewModel.kt` | Owns requests, lifecycle cancellation, search, filter selection, pagination, and retry. |
| `ui/exercises/ExerciseCatalogScreen.kt` | Shows the search field, filter menu, list, and screen statuses. |
| `ui/exercises/ExerciseCatalogItem.kt` | Displays one exercise, its image, selection action, and credits. |
| `app/src/main/res/values/exercise_catalog_strings.xml` | Holds the new English UI strings. |
| `app/src/main/res/drawable/ic_exercise_image_placeholder.xml` | Supplies the placeholder icon for waiting, missing, and failed images. |
| `data/model/Exercise.kt` | Adds optional equipment to the shared exercise model. |
| `data/repository/WorkoutRepository.kt` | Saves the imported exercise and its target row together. |
| `navigation/Routes.kt` | Defines the catalog route and its plan ID. |
| `navigation/FitTrackNavGraph.kt` | Opens the catalog for a plan, collects its StateFlow, and connects selection to the existing dialog and save action. |
| `ui/plans/PlanDetailScreen.kt` | Adds online and saved exercise entry points and reuses the targets dialog. |
| `ui/plans/PlansViewModel.kt` | Passes the catalog selection and targets to the shared workout repository. |
| `app/src/test/…/data/repository/ExerciseRepositoryTest.kt` | Runs Retrofit against MockWebServer and checks parsing, requests, mapping, optional fields, pagination, and HTTP failures. |
| `app/src/test/…/ui/exercises/ExerciseViewModelTest.kt` | Uses a fake API and a coroutine test dispatcher to check transitions, search, duplicate requests, filter changes, and retry. |
| `app/src/test/…/ui/plans/CatalogPlanIntegrationTest.kt` | Checks plan targets, exercise metadata, workout restrictions, and history resolution. |
| `WEEK13_REQUIREMENT8_DEFENSE.md` | Describes the feature, verification, and defense demo. |

## Ten commits

All author times are for November 24, 2026, UTC+05:00, as assigned in the contribution schedule. Git stores the actual committer timestamp separately.

| # | Author time | Commit message | What it adds |
| --- | --- | --- | --- |
| 1 | 12:08 | `build: add Retrofit and Coil dependencies` | Build dependencies, internet permission, and an executable wrapper. |
| 2 | 12:34 | `feat: add wger API response models` | DTOs based on the inspected live JSON. |
| 3 | 13:06 | `feat: implement Retrofit exercise API service` | The suspend service and configured Retrofit client. |
| 4 | 13:54 | `feat: implement exercise catalog repository` | English mapping, credits, pagination, and container wiring. |
| 5 | 14:23 | `feat: add ExerciseViewModel and StateFlow` | Request state, cancellation, search, retry, and the optional muscle filter data. |
| 6 | 15:41 | `feat: display exercise catalog in Compose` | Search, filter menu, list rows, and the selection action. |
| 7 | 16:17 | `feat: handle loading empty and error states with retry` | Status UI and retry controls for initial and paged requests. |
| 8 | 17:09 | `feat: display exercise images using Coil` | Thumbnails, placeholders, and image-specific credits. |
| 9 | 18:24 | `feat: integrate exercise catalog with workout plans` | Plan navigation, reusable target input, and shared workout metadata. |
| 10 | 19:46 | `test: verify API integration and catalog functionality` | Deterministic tests and this defense guide. |

## Defense demo

1. Launch the app with internet access. Open **Workout Plans** and open a plan, or use **Create plan** to make a new one.
2. Tap **Add Exercise**. Point out the loading spinner and the list of live wger results.
3. Search for `bench`. Point out **Bench Press**, its muscle group, equipment, and its image. Open **Credits** and show its source and license.
4. Try **Muscle group → Chest**. Clear the filter, then search for `zzzzfittrackzz` to show the no-matches message. Clear the query and scroll to **Load more** to fetch another page.
5. Leave the catalog, turn off the device's network, and open the catalog again. Show the error. Turn the network back on and tap **Retry**. The results should load.
6. Select **Bench Press** and enter 2 sets, 8 repetitions, and 35 kg. Tap **Save**. Confirm the saved name and targets on the plan.
7. Turn off the network again and tap **Start Workout**. Show the imported exercise and the saved targets. Record one set, show the rest timer, and finish the workout. Open **Workout History** and show the exercise.
8. Reconnect the network. Show that **Add Saved Exercise** can still choose a built-in exercise. Open **Settings** and try the light and dark themes.

## Fifteen possible teacher questions

1. **Why choose wger?** It provides public exercise data, English names, muscles, equipment, images, and author credits.
2. **What does Retrofit do?** It builds the HTTP requests from a Kotlin interface and gives the response to the JSON converter.
3. **What does Gson do?** It reads JSON fields into Kotlin DTOs.
4. **What is a DTO?** It is a data class designed to match a response from another system.
5. **Why use an exercise repository?** It checks the response and turns server data into values that the app can display.
6. **Why use suspend functions?** The coroutine can wait for a network result while the screen stays responsive.
7. **Why use a ViewModel?** It owns the screen's state and requests and survives rotation.
8. **What is StateFlow?** It has a current value and tells observers when that value changes.
9. **What happens when I type quickly?** The ViewModel waits briefly before sending a search and cancels superseded work.
10. **How does a second page load?** The repository reads the next offset and sends it with the current search and muscle.
11. **What does Retry do?** It runs the failed API request again. If the failed request was for a later page, the earlier results stay on screen.
12. **How does the Empty state differ from an error?** Empty comes from a valid response with no displayable exercises. An error means the request or response could not be used.
13. **What does Coil do?** It downloads, decodes, and displays images. It shows the placeholder when an image cannot be displayed.
14. **Why does the ID start with `wger:`?** It keeps external exercise IDs separate from FitTrack's existing IDs.
15. **How can a saved exercise work offline?** FitTrack keeps its name and workout metadata in the shared repository during the current app process.

## Actual verification

I ran these tasks using Android Studio's JDK 25 and Android SDK 37.

| Check | Result |
| --- | --- |
| Baseline at `07b9b6f` | Debug assembly passed. All 29 JVM tests passed. Lint passed with 0 errors and 32 warnings. |
| `./gradlew :app:assembleDebug` | Passed with the project's Room and KSP configuration enabled. |
| `./gradlew :app:testDebugUnitTest` | All 41 tests passed, including 12 new catalog tests. |
| `./gradlew :app:lintDebug` | Passed with 0 errors and 35 warnings. Four warnings recommend newer Coil and OkHttp versions. The former coroutine version-catalog warning is gone. |
| Existing Android instrumentation tests | All 4 passed on the API 31 Small_Phone emulator using the feature APK. They covered theme changes, activity recreation, plan editing and deletion, and workout history and progress. |
| Live emulator catalog check | Verified the loading spinner, live rows, images and placeholders, credits, search, the muscle filter, empty search results, and loading another page. Verified the catalog in light and dark mode. |
| Live emulator workout check | Verified an offline error, a successful Retry after reconnecting, the built-in exercise picker, and saving a wger exercise with targets. Started the saved exercise offline, recorded a set, saw rest start, and opened its history and chart. |

The initial dependency downloads delayed the first build and lint attempts. The final runs completed using the project configuration. A live search timed out during emulator testing. A later search displayed the expected empty state.

## Remaining limitations

- Live browsing, search, filters, and first-time image downloads depend on wger and an internet connection.
- Plans, workout history, and imported exercise metadata have the current repository's in-memory lifetime. Theme preferences persist through DataStore. Saving workout data across process restarts belongs to Requirement 9.
- The catalog shows entries with a named English translation. Full instructions are available through the wger source link.
- The supplied Swagger UI URL returned HTTP 404 during inspection. The raw schema and public API responded.

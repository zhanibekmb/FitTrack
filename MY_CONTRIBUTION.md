# My contribution to FitTrack

**Zhanibek Mukatay, team lead for Assignments 8 and 9**

I prepared the shared Room build configuration and added persistent appearance settings. From Home, open **Settings** and choose **System default**, **Light theme**, or **Dark theme**.

## How saving a theme works

1. `SettingsScreen` displays three radio options and sends a tap to `SettingsViewModel`.
2. The ViewModel launches a coroutine and calls `SettingsRepository.saveTheme`.
3. Preferences DataStore writes the enum name under the `theme_mode` key in app storage. The write is asynchronous.
4. The repository exposes a Flow that reads the stored value and turns the name into a `ThemeMode`. Missing or unrecognized values use System default.
5. The ViewModel collects that Flow and puts the latest setting in `SettingsUiState`. Its private `MutableStateFlow` lets it update state and its public `StateFlow` lets the UI observe it.
6. `MainActivity` observes the same state for the whole app. It waits for the initial read before rendering the navigation graph. After a setting changes, Compose redraws with the matching light or dark palette and updates the system-bar icons.

The radio buttons show the saved choice. If a write fails, the screen keeps the last saved choice and lets the user try again.

## Shared dependencies

`FitTrackContainer` is a Kotlin object that shares the existing workout repository and creates one settings repository. The settings repository uses the application context and a single `preferencesDataStore` delegate, so recreating an Activity does not create another store.

Room provides an SQLite database for structured workout data. KSP runs the Room compiler, which generates database and DAO implementations from annotated Kotlin classes. The dependency versions and schema output path are configured now. The database class can be added when the team has real entities and DAOs to register. See [TEAM_INTEGRATION.md](TEAM_INTEGRATION.md) for that handoff.

## Defense questions

- **Why DataStore for the theme?** The theme is one small key-value setting that should survive a restart.
- **Why Room for workouts?** Plans and workout results have related records that the app will need to query and update.
- **What does StateFlow do?** It holds the latest UI state and tells collectors when it changes. DataStore provides persistence.
- **Why one SettingsViewModel?** The Settings screen and the root theme read the same saved choice.
- **What does System default do?** The app reads the Android night-mode configuration. Light and Dark override it.
- **Why use the application context?** The repository can live for the whole process without holding an Activity in memory.
- **What is the role of a DAO?** It declares the database operations the repository can call.
- **What remains for integration?** The team adds real entities and DAOs, registers them in one database, and connects the local and network repositories.

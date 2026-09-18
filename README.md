# My Vocabulary

The purpose of this application is to provide a robust platform for language learners to manage
their own dictionaries and practice vocabulary through various quiz types. This project serves as a
comprehensive showcase of modern Android development, following a full-scale architectural migration 
from a legacy stack to a modern, reactive, and stateless declarative environment.

## 📸 Screenshots

|                  Home Screen                   |                Dictionary List                 |                   Quiz Mode                    |
|:----------------------------------------------:|:----------------------------------------------:|:----------------------------------------------:|
| <img src="screenshots/home.png" width="200" /> | <img src="screenshots/list.png" width="200" /> | <img src="screenshots/quiz.png" width="200" /> |

<p align="center">
  <img src="screenshots/demo-2.gif" width="250" title="App Demo">
<br>
  <i>Featuring smooth Lottie animations for quiz transitions and empty states.</i>
</p>

## 🚀 Recent Architectural Modernization

I have successfully completed a major refactor to align the codebase with Google's latest recommended practices (MAD - Modern Android Development):

- **Full Coroutines & Flow Migration:** Replaced the entire RxJava 2 layer with Kotlin Coroutines 
  and Flow. This includes implementing `suspend` functions in Room DAOs, `Flow`-based repositories, 
  and `StateFlow` in ViewModels, resulting in a significantly more readable and maintainable codebase.
- **Stateless UI & State Hoisting:** Refactored Jetpack Compose screens to be truly stateless. All 
  UI logic, including dialog visibility, FAB states, and navigation events, is now hoisted into 
  ViewModels and exposed via unified `UiState` objects.
- **Clean Architecture & Use Cases:** Extracted complex business logic (such as quiz result 
  calculations and database updates) into standalone `Domain` Use Cases. This improves separation of 
  concerns and makes the business rules unit-testable in isolation.
- **Main-Safe Threading:** Implemented a custom `DispatcherProvider` to ensure all ViewModel and 
  Repository operations are main-safe, using `Dispatchers.IO` for database/disk work and 
  `Dispatchers.Default` for CPU-intensive filtering.
- **Automated Delivery Pipeline:** Integrated GitHub Actions to automate testing, versioning, and production builds, ensuring code reliability and streamlining the release process.
- **Target SDK 37 & Edge-to-Edge:** Updated the app to target the latest Android SDK and implemented 
  modern theme configurations to ensure full Edge-to-Edge compatibility without letterboxing on 
  the newest Android versions.

## 🛠 Tech Stack

- **Language:** Kotlin (100%)
- **UI:** Jetpack Compose with Material 3 (Stateless Components)
- **Architecture:** Clean Architecture + MVVM + Repository Pattern
- **Reactive:** Kotlin Coroutines & Flow (End-to-end implementation)
- **Database:** Room (with multi-version migrations and automated schema exports)
- **Dependency Injection:** Koin
- **Navigation:** Jetpack Navigation (Type-Safe Routes)
- **Testing:** JUnit 4, MockK, Turbine (for Flow verification), Room-Testing
- **CI/CD:** GitHub Actions (Automated build, test, and versioning)
- **Animations:** Lottie for Android

![Kotlin](https://img.shields.io/badge/kotlin-2.4.10-blue.svg)
![Compose](https://img.shields.io/badge/Jetpack-Compose-green.svg)

## 📈 Key Features

- **Dictionary Management:** Create, import, and export custom dictionaries (CSV format).
- **Multiple Quiz Types:** Practice with "Full Quiz," "Quick Quiz," or "Weakest Words."
- **Search & Sort:** Efficiently manage large word lists with real-time filtering and CPU-optimized sorting.
- **Persistence:** High-performance local storage with Room, including background synchronization.

## 🏗 Setup & Development

The project uses a **Gradle Version Catalog** (`libs.versions.toml`) for centralized dependency 
management. To build the project:
1.  Ensure you have **JDK 17** configured in Android Studio.
2.  The GitHub CI pipeline handles signing automatically using secrets. For local signed builds, 
    provide your own `.jks` file.

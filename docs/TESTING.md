# Testing and release validation

## Requirements

* JDK 21 for Gradle; compilation targets Java 17.
* Android SDK platform 36.1 and build-tools 36.0.0.
* An Android 14+ emulator or a disposable test device for instrumentation.

On Windows, use `gradlew.bat` instead of `./gradlew`. Set `JAVA_HOME` to a working JDK installation; do not depend on an Android Studio installation that has been moved or replaced.

## Test suites

| Location | Purpose |
|---|---|
| `app/src/test` | JVM ViewModel, transactional persistence, ref identity, concurrency, local JGit and loopback HTTPS tests |
| `app/src/sharedTest` | Fakes shared by JVM and instrumented tests; no production code |
| `app/src/androidTest` | Real Android Keystore/DataStore, Compose workflows, actual Hilt graph, activity recreation and history validation |

Only shared fakes are included in both test source sets. JVM tests must not be silently duplicated into the instrumented APK.

### Local checks

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug assembleRelease assembleDebugAndroidTest
```

### Android execution

```bash
./gradlew connectedDebugAndroidTest
```

Use a dedicated emulator with no personal projects or credentials. Restrict the connected device selection when multiple devices are running; alternatively install the debug and test APKs and invoke the runner explicitly:

```bash
adb -s SERIAL install -r app/build/outputs/apk/debug/app-debug.apk
adb -s SERIAL install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb -s SERIAL shell am instrument -w -r fr.readonlymain.gitclient.test/androidx.test.runner.AndroidJUnitRunner
```

Inspect the runner's `OK (...)` / `FAILURES` result, not only the adb process exit code. `connectedDebugAndroidTest` handles this and generates XML/HTML reports automatically.

## What is covered

* Stage/unstage, commits, branches, conflicts, rejected pushes and constrained destructive operations.
* Repository switching, stale loads, queued mutation cancellation and original-error preservation when refresh also fails.
* Real loopback HTTPS object transfer, Basic authentication challenges, host scoping, server failures, downgrade redirects and cancellation.
* Real Android AES-GCM round trips, fresh IVs, ciphertext tampering, missing keys, DataStore reopen and concurrent encrypted writes.
* Visible settings errors without secret disclosure; forms stay open after failed writes.
* UI-driven staging and commits against a temporary real repository; compact commit panels and activity recreation.

HTTPS fixtures trust a certificate created for that test only and restore the JVM's original TLS factory afterward. No production TLS checks are disabled. No external Git account, real token or personal repository is required.

Windows JVM file-replacement behavior is not Android's filesystem behavior. The concurrent persistence unit test verifies the transaction contract in memory; a separate instrumented test exercises 30 writes against real encrypted Android DataStore.

## CI

The `verify` job runs JVM tests, lint and debug/release builds. Separate emulator jobs execute the Android suite on API 34 and 36. Reports are uploaded even on failure. Instrumented APK compilation alone does not count as Android execution.

The architecture follow-up was validated locally with Gradle 9.4.1 and then 9.6.0 / JDK 21: 76 JVM tests and successful lint/debug/release builds. The 21 instrumented tests passed on a dedicated API 36 emulator. Local build outputs were isolated from Android Studio's locked output files; the standard project paths above are used in CI.

[Hosted run #5](https://github.com/ReadOnlyMAIN/android-git-client/actions/runs/38569001536), for commit `dbf718f`, passed `verify` and both API 34/36 emulator jobs with Gradle 9.6.0. This is the automated validation baseline, not a substitute for the release gates below.

## Performance scope

The Android history test creates 200 commits, verifies complete and consistent repeated reads, and logs timings under `AGitValidation`. These timings are diagnostic, not a hardware-independent benchmark or a guarantee for very large repositories. Profile representative Godot projects on physical devices before release.

## Release gates still requiring explicit sign-off

* All CI jobs pass for the exact candidate commit.
* Physical smartphone/tablet checks, rotation, keyboard, accessibility and representative repository sizes.
* Real hosting-provider authentication, revoked tokens and interrupted transfers.
* Recovery UX for lost Keystore keys; activity recreation tests are not an end-to-end OS backup/restore test.
* Signed, minified release installation and Git smoke tests; release compilation alone does not validate runtime R8 behavior.
* Existing roadmap safety work, notably confirmation before discarding changes and explicit conflict presentation.

A green test suite is a release prerequisite, not a declaration that all these product gates are complete.

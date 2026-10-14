# Architecture

AGit Client uses a single Android module. Separate Gradle modules or a mandatory use-case layer are not needed at its current size.

## Dependency direction

```text
Activity / navigation routes
    -> ViewModels and read-only presentation state
        -> GitRepository / preference interfaces
            -> serialized JGit implementation / DataStore / Android Keystore
```

* `SettingsScreen` and workspace components receive state and callbacks. They do not construct preferences, access tokens from storage, or execute Git commands.
* `SettingsViewModel` owns settings observation, serialized writes, and safe error reporting. Only credential metadata is exposed in its UI state. Forms close only after successful persistence.
* `RepositoriesViewModel` loads credentials when cloning. A corrupt credential store is an operation failure, not an empty credential collection.
* `WorkspaceViewModel` captures repository context before queuing actions and ignores stale snapshots. A completed mutation and a failed refresh are distinct outcomes; refresh errors must not hide an original Git rejection.
* `SerializedGitRepository` serializes repository reads and writes by canonical path. `readWorkspace` holds the same lock across the whole snapshot, not a separate lock for each field.
* Branch models preserve full refs. A branch's display name must never determine which remote ref gets deleted.

The repository lock is **in-process only**. It does not prevent another application from modifying project files; JGit's filesystem locking and error handling remain necessary.

## State and lifecycle

Settings use a read-only `StateFlow`. Workspace exposes one read-only Compose state with compatibility projections. Both approaches retain a single state owner in the ViewModel.

Flow collection in routes is lifecycle-aware. Notification channels retain errors produced before the UI starts collecting, and are consumed by one presentation observer. Cancellation is propagated rather than shown as an operation error.

Persisted repository selection and Git's actual HEAD rebuild workspace state after process recreation. Do not save authentication tokens in Android saved state. In-flight network operations are not automatically resumed after process death.

## Persistence and cryptography

`CredentialPersistence` handles transactional read/decrypt/transform/encrypt/write. `AndroidKeystoreCredentialCipher` exclusively owns AES-GCM and device-bound key access. Missing keys and malformed ciphertext are explicit failures; decryption never creates a replacement key.

The existing key alias and encrypted payload format are preserved. Settings containing encrypted credentials are excluded from backup and device transfer. Tests use isolated aliases and files, never production credentials.

## Further changes

Add a domain service when a workflow is reused or becomes too complex for a ViewModel. Split JGit implementations further when adding large features, rather than creating empty abstraction layers now. Keep protocol policies and ref interpretation outside composables.

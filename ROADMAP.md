# AGit Client — Roadmap

This roadmap separates released functionality from ongoing development. Version scope may evolve; planned items are not release guarantees.

**Status legend:**
* 🟢 **Baseline:** functionality documented for v0.1, not a new validation claim.
* 🟢 **Implemented — validation pending:** present in the development code, but not yet considered release-ready.
* 🟡 **In progress:** partially implemented.
* 🔴 **Planned:** work remains to be done.

## v0.1 — Initial pre-release baseline

| Goal | Status |
|---|---|
| HTTP(S) Git connections | 🟢 Baseline |
| Repository cloning and local import | 🟢 Baseline |
| Repository list management | 🟢 Baseline |
| Git credential storage and management | 🟡 Baseline; in-place editing remains incomplete |
| Fetch, pull, commit, and push | 🟢 Baseline |
| Tablet and large-screen interface | 🟢 Baseline |

## v0.2 — In development

### Phase 1 — Correct, optimize, and stabilize existing logic

**This is the first objective and the immediate development priority.**

Improve the existing application internally before extending its functionality. During this phase, **do not add, remove, or modify UI**: no new dialogs, controls, screens, labels, layout changes, or redesigned interactions. Existing responsive layouts remain in place without further UI work.

Correcting the data and behavior behind the existing interface is in scope. New user-facing capabilities, confirmation flows, and UX/UI changes are deferred to Phase 2. Backend prerequisites may be prepared here, but must not introduce new user-facing functionality.

#### Correctness and data integrity

| Goal | Status |
|---|---|
| Refresh repository status consistently after staging, unstaging, checkout, pull, and commit | 🟢 Implemented — validation pending |
| Identify selected repositories by stable identity / canonical path, rather than display name | 🟢 Implemented — validation pending |
| Read the actual checked-out branch instead of assuming `main` or a saved default | 🟢 Implemented — validation pending |
| Clear or reconcile file selections when switching repository or branch | 🟢 Implemented — validation pending |
| Prevent stale asynchronous results from overwriting the current repository state | 🟢 Implemented — validation pending |
| Prevent duplicate local imports using canonical repository paths and existing error reporting | 🟢 Implemented — validation pending |
| Validate remote branch deletion results and distinguish partial local / remote success | 🟢 Implemented — validation pending |
| Distinguish authentication failures, transport failures, Git rejections, and merge conflicts internally | 🟢 Implemented — validation pending |
| Avoid retrying state-changing Git operations with other credentials after a non-authentication failure | 🟢 Implemented — validation pending |
| Preserve conflict information in the repository status model, ready for later UI integration | 🟢 Implemented — validation pending |
| Audit destructive operations and filesystem cleanup so they cannot affect unintended files | 🟢 Implemented — validation pending |

#### Security and persistence

| Goal | Status |
|---|---|
| Restrict credential use to the intended host / repository; prevent indiscriminate credential fallback | 🟢 Implemented — validation pending |
| Validate remote URLs and require HTTPS when transmitting HTTP credentials | 🟢 Implemented — validation pending |
| Propagate encryption and decryption failures instead of silently treating them as empty credentials | 🟢 Implemented — validation pending |
| Keep credential updates atomic and avoid overwriting data after a failed read or encryption | 🟢 Implemented — validation pending |
| Define backup and restore rules for Keystore-encrypted credentials | 🟢 Implemented — validation pending |
| Validate supported local storage paths and report unsupported providers through existing error handling | 🟡 In progress — backend validation implemented; document-picker rejection feedback deferred to Phase 2 to avoid UI changes |

#### Performance and maintainability

| Goal | Status |
|---|---|
| Coordinate operations per repository to prevent conflicting mutations and duplicate refreshes | 🟢 Implemented — validation pending |
| Preserve coroutine cancellation through exception handling and stop retry loops after cancellation | 🟢 Implemented — validation pending |
| Consolidate repeated authentication, progress-monitor, and error-handling logic | 🟢 Implemented — validation pending |
| Optimize history traversal and commit tracking calculations without changing the interface | 🟢 Implemented — validation pending |
| Close JGit resources consistently and avoid unnecessary full-history reloads | 🟢 Implemented — validation pending |
| Introduce typed repository status and coherent read-only workspace state while preserving the UI | 🟢 Implemented — validation pending |
| Align dependency versions and review release shrinking rules without changing UI behavior | 🟢 Implemented — validation pending; conservative JGit keep rules retained pending device smoke tests |

#### Validation before Phase 2

| Goal | Status |
|---|---|
| Bring existing ViewModel tests back in line with current behavior | 🟢 Implemented — local tests passed |
| Add integration tests using temporary Git repositories and local remotes | 🟢 Implemented — local tests passed |
| Cover staging, deleted files, conflicts, branch operations, rejected pushes, and destructive operations | 🟢 Implemented — local tests passed |
| Cover duplicate imports, repository switching, concurrent operations, cancellation, and persistence failures | 🟢 Implemented — local tests passed |
| Add CI checks for unit tests, Android lint, and debug / release compilation | 🟢 Implemented — first hosted run pending |

**Phase 1 completion criteria:** regression tests cover the corrected logic; supported existing Git workflows pass; cancellation and failure paths leave consistent state; no UI additions, removals, or modifications are included. Device validation must also check that existing layouts and interactions remain unchanged.

#### Local validation and remaining release gates

The logic cleanup includes 58 local JVM tests, including JGit integration tests against temporary local remotes. No personal repository or external Git account is used by these tests.

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug assembleRelease compileDebugAndroidTestKotlin
```

The local checks cover unit / integration tests, Android lint, debug and release builds (including R8), and compilation of instrumented tests. Lint warnings remain; a successful build is not a claim that lint is warning-free.

**Still pending:** the first hosted CI run, execution on Android devices / emulators, real HTTPS authentication and cancellation, Android Keystore failure / restore scenarios, and confirmation that the unchanged UI behaves correctly on compact and large screens. Phase 1 is not declared release-ready until those checks are complete.

Credentials are now restricted by their stored `providerUrl` (default: GitHub), and cross-origin HTTP redirects are rejected. Broader credential-association controls remain a Phase 2 UI task. Unsupported document-picker providers currently return no filesystem path; explicit visual rejection feedback also remains deferred to Phase 2.

### Phase 2 — Functional additions and UX/UI improvements

Start this phase only after the internal reliability work has been validated. Features already present in development still need testing and may depend on Phase 1 corrections.

#### Existing implementations to validate and finish

| Goal | Status |
|---|---|
| Responsive smartphone layouts, including workspace, branch selector, and repository selector | 🟡 In progress |
| Local branch creation, checkout, and deletion | 🟢 Implemented — validation pending |
| Optional remote branch deletion | 🟢 Implemented — validation pending |
| Fetch, pull, and push cancellation controls | 🟢 Implemented — validation pending |
| Automatic creation of a repository subfolder when cloning | 🟢 Implemented — validation pending |

#### UX/UI and safety

| Goal | Status |
|---|---|
| Explicit confirmation before discarding changes; clearly distinguish selection from all changes | 🔴 Planned |
| Warn before cloning a repository whose remote is already registered locally | 🔴 Planned |
| Show added, modified, deleted, and conflicted file statuses | 🔴 Planned |
| Show file change details / diffs | 🔴 Planned |
| Show a commit details panel | 🔴 Planned |
| Provide a safe “Reset here” action with explicit scope, reset mode, and confirmation | 🔴 Planned |
| Add further commit actions after their scope and safety requirements are defined | 🔴 Planned |
| Provide a dedicated commit description input | 🔴 Planned |
| Complete in-place Git credential editing and host / repository association controls | 🔴 Planned |
| Show clone progress even when cloning the first repository | 🔴 Planned |
| Validate small screens, landscape, keyboard insets, large fonts, and accessibility | 🔴 Planned |
| Externalize UI strings and replace generic accessibility descriptions | 🔴 Planned |

#### Functional improvements

| Goal | Status |
|---|---|
| Save and reuse a default clone directory | 🔴 Planned |
| Add SSH Git connections, including key management and host-key verification | 🔴 Planned |
| Add automatic fetch on app reopen | 🔴 Planned |
| Add configurable periodic background fetch with appropriate network and battery constraints | 🔴 Planned |

Automatic synchronization means fetching remote information, not silently pulling, merging, or pushing user changes.

## Later versions — Scope not assigned

No later version number or delivery date is committed yet. Any v0.2 item deferred during implementation should be explicitly moved to a future version section rather than silently removed from the roadmap.

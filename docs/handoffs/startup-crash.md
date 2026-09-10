# Startup crash handoff

Observed on device: `main@f09e510918e42322a7ea7aad5698fec4a22c14f2` installs but exits immediately after launch.

Diagnostic branch: `hotfix/runtime-startup-crash-20260910`.

MainActivity now catches startup `Throwable` around `new GameView(...)` and renders the exception class/message plus the first stack frames on screen. This is diagnostic only and must not be considered root-cause fixed until the displayed stack is used to patch the actual failing component and a new APK is runtime verified.

Strong suspect: `RuntimeState` production constructor executes several regression audits and throws `IllegalStateException` if any audit returns false. CI currently compiles/builds but does not exercise this constructor on Android runtime.

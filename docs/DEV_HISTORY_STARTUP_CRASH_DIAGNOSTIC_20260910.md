# Startup crash diagnostic hotfix

Date: 2026-09-10

User device runtime reported that `main@f09e510918e42322a7ea7aad5698fec4a22c14f2` installs but exits immediately on launch.

This hotfix changes `MainActivity` so constructor-time startup exceptions are rendered on screen instead of terminating without evidence. It does not claim to fix the root cause. RuntimeState currently executes regression audits from its production constructor and may throw `IllegalStateException`; the diagnostic screen is intended to capture the exact failing path on device.

Status:
- previous APK: BUILD VERIFIED / RUNTIME FAILED
- diagnostic hotfix: requires build and device launch

# Council Connect

An Android app for browsing student council events, RSVPing, and receiving event reminders — built for [Council Name]'s events after a year on the council made clear how much easier this would make coordination.

## Features
- Browse upcoming council events with details (title, description, location, time)
- RSVP to events, with live attendee counts
- Offline-first: events are cached locally, so the list is still usable without a connection
- Push notifications for event reminders
- Simple check-in flow for event day

## Tech Stack
- **Language:** Kotlin
- **UI:** Jetpack Compose, Material 3
- **Architecture:** MVVM (ViewModel + StateFlow), repository pattern
- **Backend:** Firebase
  - Firestore — events and RSVP data
  - Authentication — user sign-in
  - Cloud Messaging (FCM) — push notifications
- **Local storage:** Room (offline cache)
- **Concurrency:** Kotlin Coroutines

## Architecture

```
UI (Compose) → ViewModel → Repository → ┬─ Room (local cache)
                                         └─ Firestore (remote source)
```

The repository is offline-first: the UI always reads from the local Room cache (via a `Flow`), while a background refresh pulls the latest data from Firestore and updates the cache. If the network call fails, the UI keeps showing cached data and surfaces an "offline" indicator instead of an error state.

## Setup

1. Clone the repo and open in Android Studio (Giraffe or newer recommended).
2. Create a Firebase project at [console.firebase.google.com](https://console.firebase.google.com).
3. Add an Android app to the Firebase project with package name `com.yourname.councilconnect`.
4. Download the generated `google-services.json` and place it in the `app/` directory.
5. Enable **Firestore**, **Authentication** (Email/Password), and **Cloud Messaging** in the Firebase console.
6. Sync Gradle and run on a device or emulator (API 26+).

## Issues Encountered & Fixed

- **Push notifications not appearing on Android 8+**: caused by never creating a notification channel before posting a notification — required on API 26+ or the notification silently fails with no error or crash. Fixed by creating the channel on app start, before any notification is sent.
- **Notification permission not requested on Android 13+**: `POST_NOTIFICATIONS` requires a runtime permission request on API 33+. Added a permission check and launcher on app start.
- **RSVP failing silently when offline**: Firestore writes throw when there's no connection; the initial version just dropped the RSVP. Fixed by treating write failures as a distinct case in the repository layer rather than a generic error, so the UI can respond appropriately instead of failing silently.

## What I'd Add With More Time
- Live Firestore listeners instead of fetch-on-refresh, for real-time RSVP counts
- QR-code based check-in instead of manual code entry
- Local queueing and automatic retry for RSVPs submitted while offline
- Push notification targeting by event/segment rather than broadcast

## License
Personal/educational project.

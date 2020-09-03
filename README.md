# Webasyst-X-Android

## Note for translators

String resources are located in `/webasyst-x/src/main/res/values[-lang[-rREGION]]`
where `lang` is two-letter ISO 639-1 language code
and `REGION` is two letter ISO 3166-1-alpha-2 region code (note the lowercase r).

Android selects resources based on system locale. If it can't locate appropriate
resources it falls back to default (`res/values/`)

For details see https://developer.android.com/guide/topics/resources/localization

For details on string resource format see https://developer.android.com/guide/topics/resources/string-resource

## Running with Android Studio

1. Clone this repository

2. Create new file `webasyst.properties` in repository root:
```
webasyst.x.client_id="client id"
webasyst.x.host="https://waid.dev.webasyst.com"
```
(replace client id with actual client id)

3. Install Android Studio as described in https://developer.android.com/studio/install

4. In Android Studio choose file -> open and navigate to project directoy.

5. Detailed manual on running an app on the Android Emulator can be found here: https://developer.android.com/studio/run/emulator

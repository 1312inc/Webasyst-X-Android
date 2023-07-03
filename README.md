# Webasyst-X-Android

![webasyst-x-android-ru-v1-showcase-dev](https://user-images.githubusercontent.com/889083/117459691-5b5f0680-af54-11eb-9d3b-e7c79e4e66ac.jpg)

## Project structure

The project has a main module `webasyst-x` that provides the core structure of the sample Android application.
Also have the following gradle submodules:

### `auth`

The module contains on-boarding screens (intro) and activity for authorization. Implemented methods of authorization by password, by phone number, by QR Code. Also implemented is an express connection of the WAID-user to the installation using a QR Code.

### `barcode`

Use this module if you need to login and connect users by QR Code.

### `common`

Сommon utility classes, functions and resources.

### `i18n`

String resources of the whole project (incl. submodules). See [note for translators](#note-for-translators)

### `installations`

A key module that allows you to get information about the user's installations. Contains both data layer objects and interface elements that allow the user to switch between installations.

### `pin_code`

Use this module if you need to protect sensitive user data. Call PinCodeStore.hasPinCode() or PinCodeStore.hasPinCodeWithTime() in onResume() of the corresponding activity or fragment.

### `profile_editor`

Adds user profile editing features.

## Creating new Webasyst application from scratch

1. Enable `auth` dependency. In your app's module `build.gradle`:
```groovy
dependencies {
  // For Java projects:
  implementation project(':auth')
  // For Kotlin projects:
  implementation project(':auth:kt')
}
```

2. In your app's `AndroidManifest.xml`, in `application` section, add authentication redirect activity.
Note the comment on `<data android:scheme=` key
```xml
<activity android:name="net.openid.appauth.RedirectUriReceiverActivity">
  <intent-filter>
    <action android:name="android.intent.action.VIEW"/>
    <category android:name="android.intent.category.DEFAULT"/>
    <category android:name="android.intent.category.BROWSABLE"/>
    <!-- Authentication redirect scheme. It should be unique across the device. It is recommended to use app's package name. -->
    <data android:scheme="webasyst-x"/>
  </intent-filter>
</activity>
```

3. Configure Webasyst ID (WAID) client. This should be done once, preferably early in application's lifecycle. The recommended option is to extend `Application` class and do configuration in its `onCreate()` method.
See `WebasystAuthService.configure()` for details.

4. Implement Authentication Activity.

The easiest way to do it is to extend your Activity from `WebasystAuthActivity` and call its `waSignIn()` from your SignIn button `onClick()` callback.

If that's not an option (e.g. your Activity is an extension of some other activity) you can use WebasystAuthHelper directly. See `WebasystAuthActivity` code for details.

5. You are good to go. Use `WebasystAuthService`'s `withFreshAccessToken()` (or Kotlin extension) to perform api requests.

## Note for translators

String resources are located in `/webasyst-x/i18n/src/main/res/values[-lang[-rREGION]]`
where `lang` is two-letter ISO 639-1 language code
and `REGION` is two letter ISO 3166-1-alpha-2 region code (note the lowercase r).

Android selects resources based on the system locale. If it fails to locate appropriate
resources, it falls back to default (`res/values/`)

For details, follow the link https://developer.android.com/guide/topics/resources/localization

For details on string resource format, follow the link https://developer.android.com/guide/topics/resources/string-resource

## Running the example app with Android Studio

1. Clone this repository

2. Create new file `webasyst.properties` in repository root:
```
webasyst.x.client_id="YOUR_WEBASYSTID_APP_CLIENT_ID_HERE"
webasyst.x.host="https://www.webasyst.com"
```
(obtain Webasyst ID app client id from Webasyst and save it into this configuration file)

3. Install Android Studio as described in https://developer.android.com/studio/install

4. In Android Studio choose file -> open and navigate to project directory.

5. Detailed manual on running an app on the Android Emulator can be found here: https://developer.android.com/studio/run/emulator

## Сreate your own application based on the example application

1. Change unique application ID in the main module build.gradle file. More about application ID here: https://developer.android.com/build/configure-app-module

2. Change the package attribute of the main Manifest.xml file and change the package name in all source files.

3. Change application name in the string resources.

4. Change `app_redirect_scheme` in string resources. It is recommended that you specify a package name or your web domain (in reverse domain name notation)

5. Modify the source code at your own discretion.

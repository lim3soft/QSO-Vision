# Third-party license summary

QSO Vision source code is licensed under Apache-2.0. This does not relicense
dependencies or externally sourced material. The following licenses apply to
the declared Gradle dependencies in `app/build.gradle.kts`:

## Apache License 2.0

The AndroidX libraries, Material Components for Android, Kotlin/Kotlinx
Coroutines, and AndroidX Room components are Apache-2.0 licensed. Their
license terms and notices are distributed by their respective artifacts.

Declared components include:

- `androidx.core:core-ktx`
- `androidx.appcompat:appcompat`
- `com.google.android.material:material`
- `androidx.activity:activity-ktx`
- `androidx.lifecycle:lifecycle-viewmodel-ktx`
- `org.jetbrains.kotlinx:kotlinx-coroutines-android`
- `androidx.recyclerview:recyclerview`
- `androidx.room:room-runtime`, `room-ktx`, and `room-compiler`

## Eclipse Public License 1.0

`junit:junit:4.13.2` is used for tests and is licensed under the Eclipse
Public License 1.0. See https://junit.org/junit4/license.html.

`org.jetbrains.kotlinx:kotlinx-coroutines-test` is a test dependency and is
covered by the Kotlinx Coroutines Apache-2.0 licensing.

## Other bundled material

- `app/src/main/assets/lastheard.js` is project integration code that operates
  against the official BrandMeister rendered page; it does not claim ownership
  of BrandMeister content.
- `app/src/main/res/raw/isrg_roots.pem` contains the ISRG Root X1 public
  certificate. It is a public trust certificate, not application source code.
- BrandMeister names, logos, services, and URLs remain the property of their
  respective owners. This project is not affiliated with BrandMeister.

This summary is informational, not a replacement for the license files and
not legal advice. For redistribution, verify the complete notices packaged by
the resolved dependency artifacts.

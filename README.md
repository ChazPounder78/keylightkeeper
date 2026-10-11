# KeyLight Keeper

Android app that keeps the **hardware key backlight** (capacitive/D-pad keys) turned on
for as long as the screen is on, and turns it off when the screen sleeps — overriding the
MediaTek keypad driver that insists on switching the keys off after a timeout.

Built for a MediaTek MT6765 board (`k62v1_64_bsp`) running a GSI, where neither
`settings put system button_key_light_timeout -1` nor any framework setting has any effect:
the LED is driven purely by the kernel keypad driver.

## How it works

The LED lives at:

```
/sys/class/leds/button-backlight/brightness
```

* `255` = full brightness
* `0`  = off

The keypad driver (`mtk-kpd`) resets this LED on screen off/on transitions, so the app
keeps a single long-lived root shell open and re-asserts the value every second:

* screen on  → `echo 255`
* screen off → `echo 0`

Screen state comes from `PowerManager.isInteractive()`, refreshed on the
`ACTION_SCREEN_ON` / `ACTION_SCREEN_OFF` broadcasts and by a 1 s tick.

The root shell is started lazily and lives with the service. Stopping the toggle (or
uninstalling the app) closes the pipe, the shell sees EOF and exits, and the LED returns
to normal driver behaviour.

## Requirements

* **Root** (Magisk). The first time you enable the switch, grant the Superuser request.
* The device must expose the LED at the path above.

## Building

```sh
export ANDROID_HOME=/path/to/Android/Sdk
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Usage

1. Install and open **KeyLight Keeper**.
2. Flip the switch on — grant root when asked.
3. The keys now stay lit while the screen is on.

Turning the switch off (or uninstalling) stops the behaviour entirely.

## Bug Fixes & Improvements

Version 1.0.1 (current): 
- Improved error handling for LED path validation
- Better resource management and cleanup
- Enhanced logging for debugging issues

## Magisk module alternative

`magisk-module/` contains a standalone Magisk module that does the same thing without an
app (a `service.sh` background loop). It is kept here for reference; the app is the
recommended, easily removable option.

## License

MIT — see [LICENSE](LICENSE).

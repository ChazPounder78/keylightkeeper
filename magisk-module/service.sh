#!/system/bin/sh
# Hold the button-backlight LED at max brightness only while the screen is on.
LED=/sys/class/leds/button-backlight/brightness
LCD=/sys/class/leds/lcd-backlight/brightness
nohup sh -c 'while :; do
  lcd=$(cat '"$LCD"' 2>/dev/null)
  cur=$(cat '"$LED"' 2>/dev/null)
  if [ "$lcd" -gt 0 ]; then
    [ "$cur" -ne 255 ] && echo 255 > '"$LED"' 2>/dev/null
  else
    [ "$cur" -ne 0 ] && echo 0 > '"$LED"' 2>/dev/null
  fi
  sleep 1
done' >/dev/null 2>&1 &
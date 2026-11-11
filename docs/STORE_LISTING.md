# Store listing

Draft text and asset specifications for the Google Play listing. Character limits are Play's.

## Text

**App name** (30 max, 15 used)
> Toddler English

**Short description** (80 max, 78 used)
> First English words for ages 1-5: tap, listen, sing and play. No ads. Offline.

**Full description** (4,000 max)
> Toddler English helps little ones hear and enjoy their first 100 English words, with Pebble, a friendly
> round bird with a leaf on its head.
>
> TAP AND LISTEN
> Explore five bright scenes: the farm, food, home, bedtime and outdoors. Tap the cow, the apple, the moon or
> the bus and hear its name. Every tap gets a happy response.
>
> PLAY AND SING
> • Bubbles: pop pictures in floating bubbles and hear each word.
> • Find it: Pebble asks "Can you find the cow?" There are no wrong answers, just gentle help.
> • Feed Pebble: give Pebble an apple, some milk or a cookie. Yum!
> • Sing along: seven short songs with the words lighting up as they are sung.
>
> MADE FOR YOUNG CHILDREN
> • No ads, no purchases, no accounts.
> • Works completely offline. The app does not use the internet at all.
> • Collects no data. Progress stays on your device.
> • No timers, scores or "wrong" sounds. Just calm, positive play.
> • Big buttons sized for small hands.
> • Stickers to collect as your child explores.
>
> FOR GROWN-UPS
> A parent corner, behind a grown-up check, shows which words your child has heard, lets you choose the word
> level (first words, more words, all words) and sets a gentle play-time limit. When time is up, Pebble goes to
> sleep until a grown-up opens the parent corner.
>
> Toddler English is free, with no ads or purchases.

**Category**: Education. **Tags** (pick up to 5 offered by Play): Education, Language learning, Kids.
**Contact email**: the developer contact email (required; same as in the privacy policy).
**Privacy policy**: https://haziq16.github.io/toddler-english-app/privacy-policy

## Graphic assets

| Asset | Required spec | Content |
|---|---|---|
| App icon | 512 x 512 px, 32-bit PNG, up to 1 MB, full square (Play applies the mask) | Pebble on the mint background `#CDEBDD`, same as the launcher icon (`res/drawable/ic_launcher_foreground.xml`). Export the vector at 512 px; keep Pebble inside the centre 66% circle. |
| Feature graphic | 1024 x 500 px, JPG or 24-bit PNG (no transparency) | Farm background (`bg_farm`) stretched to 1024 x 500, Pebble left-centre at about 300 px tall, cow, duck and apple on halos to the right. No text needed; if any, only "Toddler English". Keep key art out of the outer 15% (it can be cropped). |
| Phone screenshots | 2-8 images, PNG or JPEG, 16:9 or 9:16, each side 320-3840 px | List below. Capture on a 1080 x 2400 emulator or phone with the status bar clean (demo mode). |
| 7" and 10" tablet screenshots | Recommended (needed for tablet featuring), same rules | Same scenes in landscape at 1280 x 800 dp. |

Rules for every screenshot: no real children's photos or names, no device frames needed, no claims like "#1".

## Screenshot list (phone, in this order)
1. Home screen with the five scene tiles and Pebble.
2. Farm scene at "All words" with the cow just tapped (label "cow" showing).
3. Find it at "More words": three pictures, Pebble asking.
4. Bubbles with three or four bubbles in flight.
5. Feed Pebble with a food flying to Pebble.
6. Sing-along at "All words" with a highlighted lyric.
7. Sticker book with a few stickers earned.
8. Parent corner progress section (shows the grown-up tools and that data stays on device).

Capture with:
```
adb shell settings put global sysui_demo_allowed 1
adb shell am broadcast -a com.android.systemui.demo -e command enter
adb shell am broadcast -a com.android.systemui.demo -e command clock -e hhmm 0900
adb exec-out screencap -p > screenshot.png
adb shell am broadcast -a com.android.systemui.demo -e command exit
```

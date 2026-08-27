# Start Screen Visual QA

## Evidence

- Physical device: `RMX3997`, portrait `720 x 1604`
- Final capture: `/tmp/mycat-25-percent-larger.png`
- Gameplay transition: `/tmp/mycat-no-contact-gameplay.png`

## Findings

- No actionable P0, P1, or P2 issue remains.
- Count: every launch contains a random `18..25` cats.
- Position: placements come from real random x/y candidates. A collision check rejects candidates whose rotated safety bounds would touch an existing cat.
- Contact safety: the check uses both x and y distances, a `1.08` visual-bounds safety factor, and an additional gap. Thirty seeded layouts also pass the pairwise no-contact assertion.
- Opacity: every cat independently receives a random value in `0.40..<0.90`; the former fixed opacity groups are removed.
- Size: every rendered cat is 25% larger than the prior layout. Placement begins with random `0.125..<0.375` sizes and can reduce the upper bound toward `0.225` only when required to fit a dense 25-cat layout without contact.
- Imagery: all seven source images are guaranteed to appear, then their assignment is shuffled.
- Typography and hierarchy: Google Fonts Chewy, two-line gradient `You Can` / larger `START`, full-screen black `0.20` overlay, and text z-index remain correct.
- Interaction: tapping the CTA reaches `GamePlayScreen` on the physical device.

## Full-view comparison

- The final physical capture shows the requested 25% larger cats while preserving the non-touching layout.
- Visible cat bounds remain separated across the full screen; there is no grid and no same-location pileup.
- Edge cats remain inside their collision-safe bounds while the CTA stays visually dominant.

## Verification

- Android host tests: passed with zero failures.
- Debug APK: installed on physical `RMX3997`.
- Physical text navigation: passed.
- Web, emulator, iOS simulator, Xcode signing, and Team ID: not used.

## Checklist

- [x] Random `18..25` cat count
- [x] Random `0.40..<0.90` opacity per cat
- [x] Random x/y and image assignment
- [x] Pairwise non-touching placement
- [x] Rotation and animation safety margin
- [x] Two physical cold-launch screenshots
- [x] Physical gameplay transition

final result: passed

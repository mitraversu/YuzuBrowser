# Daidai 🍊

**The Yuzu Browser concept, reborn.**

> ダイダイ（代々）— *from generation to generation.*
> The daidai is a bitter Japanese orange. In Japanese, its name is a homophone
> for "generations" — which is exactly what this project is about: carrying the
> spirit of a beloved, discontinued browser across a generation gap, into a
> fresh codebase.

[Yuzu Browser](https://github.com/hazuki0x0/YuzuBrowser) (2017–2021, by
**Hazuki**, following the lineage of **Mikan Browser**) was discontinued in
March 2021. It was quietly brilliant: a WebView browser whose entire UI was
user-assembled. Not "choose light or dark" — *every toolbar slot, soft button
and gesture could be bound to any of ~60 actions*, with themes imported as
packs. Nobody has really shipped that idea since.

Daidai is that idea, rebuilt from zero for modern Android.

## Philosophy (inherited DNA)

1. **Everything is an Action.** The toolbar is not designed — it's *assembled*.
   The `BrowserAction` sealed hierarchy is the vocabulary; a `ToolbarSpec` is
   just an ordered list of action ids, persisted as JSON and rendered by the UI.
   Long-press any toolbar button to open the editor.
2. **Lean & private.** System WebView engine. Zero telemetry, zero Google
   Play Services, no analytics, no crash SDK. The browser talks to the sites
   you visit — nobody else.
3. **F-Droid first.** Pure Kotlin/Compose, no binary blobs, CI-built APKs on
   GitHub Releases, metadata for F-Droid inclusion. Reproducible builds are a
   roadmap commitment, not an afterthought.

## Current state — M1 "Seed" 🌱

- [x] Fresh codebase: Kotlin 2.x, Jetpack Compose, Material 3 (dynamic color)
- [x] Tabs (switcher sheet, close, new)
- [x] URL bar with search fallback (DuckDuckGo / Brave / Startpage / Google / Bing)
- [x] **Customizable toolbar** — add / remove / reorder actions, persisted
- [x] Hosts-based ad & tracker blocking (StevenBlack list by default,
      any hosts-format source, updated in-app)
- [x] Find in page, share, desktop-site toggle, bookmarks
- [x] Downloads via system DownloadManager
- [x] Link handling (`http/https` VIEW intents)
- [x] en / de / ja UI from day one (the lineage deserves it)
- [x] GitHub Actions CI building APKs

## Roadmap

| Milestone | Theme | Highlights |
|---|---|---|
| **M1 Seed** 🌱 *(current)* | It browses | everything above |
| **M2 Sprout** 🌿 | It bends | drag-and-drop toolbar editor, customizable overflow menu, speed-dial home, fullscreen video, popup tab handling, per-site settings seed, R8 minification |
| **M3 Sapling** 🌳 | It blooms | theme packs (`.daidaitheme`), gesture bindings, userscripts, full filter-list adblock (EasyList syntax), session restore, backup/restore, F-Droid submission |
| **M4 Tree** 🌲 | It seeds the next generation | engine abstraction (optional GeckoView flavor), action parameters & custom actions (user-defined intents/scripts), extension API for new action types |

## Architecture

```
org.daidai.browser
├── actions/    BrowserAction sealed hierarchy + ActionCatalog  ← the DNA
├── toolbar/    ToolbarSpec — ordered action ids, JSON-persisted
├── browser/    BrowserViewModel, TabController (WebView seam), TabSnapshot
├── adblock/    HostsBlocker — shouldInterceptRequest interception
├── data/       DataStore settings, bookmarks, search engines
└── ui/         Compose: BrowserScreen, ActionToolbar, ToolbarEditorSheet,
                TabSheet, FindBar, Settings, Bookmarks
```

The `TabController`↔engine boundary is deliberate: M4 extracts an interface so
a GeckoView flavor can live beside the WebView one.

## Building

    $ cd daidai
    $ ./gradlew assembleDebug

Android Studio Ladybug+ or a JDK 17 toolchain is required; minSdk 26
(Android 8.0), targetSdk 35. The APK lands in `app/build/outputs/apk/`.

## Distribution

- **GitHub Releases** — CI-built APKs from every push (see
  [`.github/workflows/daidai-build.yml`](../.github/workflows/daidai-build.yml))
- **F-Droid** — metadata stub in [`fdroid/metadata/`](fdroid/metadata/);
  submission targeted for M3.

## Credits & license

- **Yuzu Browser** © 2017–2021 [Hazuki](https://github.com/hazuki0x0) — Apache 2.0
- **Mikan Browser** — the ancestor of them all

This project carries the Apache License 2.0 forward, as the lineage did.

Daidai is not affiliated with the original Yuzu Browser project; it is an
independent revival of its ideas, with gratitude.

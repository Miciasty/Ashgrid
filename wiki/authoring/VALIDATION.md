# WIKI validation

## Template and owner-review update — 2026-09-13

Prepared locally on `docs/template-guidelines-20260913`, after starting-state commit `998633a`. The earlier published revision is `194a4f9`; its Pages workflow run `34717658785` succeeded. The results below concern the new local revision, which has not been pushed or deployed and awaits owner acceptance.

| Check | Result | Scope |
| --- | --- | --- |
| Build and content | PASS | `npm run build`: 18 pages, 74 sections, 80 internal content links, 17 registered figures, local assets, and generated `_site`. |
| Java examples | PASS | All 25 actual article programs compiled and ran against current Ashgrid source and Ashcore 1.2.0, with exact expected stdout. No Java source or example code changed. |
| Owner's UI correction | PASS | Removed operation-choice, dependency, and provider-lookup card flows and their unused factories/styles. All 18 routes have zero `.visual-flow` elements; supporting prose and tables remain. |
| Ashspace reference | PASS | Inspected its local geometry scene and implementation. Ashgrid uses perspective SVG, shaded unit cubes, a ground grid, an orientation key, drag orbit, keyboard controls, scroll/button zoom, and Reset view. |
| Camera independence | PASS | All five spatial figures: rotation and zoom changed projected geometry while cell coordinates and result text stayed equal. Reset view restored the original projection. Switching to 2D and back preserved results and membership. |
| Pointer and keyboard interaction | PASS | Actual drag changed camera azimuth/elevation and released pointer capture; wheel changed zoom. Arrows, zoom buttons, Home, and Reset view exercised. Camera updates preserve the SVG node and keyboard focus. |
| Full resets | PASS | All 17 figures: change a preset, range, or action, then Reset example. Rendered text returned to the documented initial state. |
| DDA example alignment | PASS | Default negative ray: four callbacks, three zero-length intervals, then `(-1,-1,-1)` over `[0,0.5)`. Alternate positive ray: seven callbacks, four zero-length intervals, final exit 3. All callbacks stepped in the browser. |
| Morphology membership | PASS | Both source presets × four operations × three neighborhoods: browser result coordinates exactly matched Java for all 8304 cell classifications (12 × 125 + 12 × 567). Rechecked after the perspective renderer change. |
| Responsive layout | PASS | All 18 pages at desktop width 1280 and mobile 390 × 844: titles render, no page-level horizontal overflow. The mobile menu closes after navigation. |
| Mobile Maven link | PASS | At width 390 the topbar link is hidden and the navigation drawer contains the same versioned Maven Central link. |
| Visual appearance | PASS | Desktop and mobile 3D scenes inspected; light and dark theme rendering checked. Labels accompany colors; hidden cells can be inspected through slices. |
| Browser console | PASS | No warning or error messages during the route, reset, and camera checks. |
| Publication | NOT RUN | Awaiting owner approval of this local update. No push, merge, Pages setting change, or deployment. |

Java used OpenJDK 25.0.2 with `--release 21` and the local Ashcore 1.2.0 JAR. Temporary Java comparison sources and outputs stay under ignored `.verification`. The comparison tests finite documented masks, not every possible input. Browser checks exercised local models, not a Minecraft server. The library source is unchanged.

## Initial local validation — 2026-09-12

Executed on 2026-09-12 against Ashgrid 1.3.0 and Ashcore 1.2.0.

| Check | Result | Scope |
| --- | --- | --- |
| Locked dependencies | PASS | `npm ci --no-audit --no-fund`; Tailwind 4.3.3. |
| Build | PASS | `npm run build`; generated stylesheet and `_site` artifact. |
| Content validation | PASS | 18 pages, 74 sections, 80 internal content links, navigation, version, demo mode, shell links, and local assets. |
| Visual registry | PASS | All 20 figure IDs resolve to local factories. Every figure mounted across the 18 routes, on desktop and at 390 × 844. |
| Java examples | PASS | `npm run check:examples`; 25 actual article programs compiled with current library source and service descriptors. Each stdout matched its displayed result. |
| Provider discovery | PASS | All seven built-in provider IDs resolved in the runnable example. |
| Maven Central coordinates | PASS | Public POM returned HTTP 200 and confirmed Ashgrid 1.3.0 with Ashcore 1.2.0. |
| Desktop rendering | PASS | All 18 routes rendered a title and article; no page-level horizontal overflow. |
| Mobile rendering | PASS | All 18 routes at 390 × 844; no page-level horizontal overflow. Menu opened, navigation worked, and the menu closed after selection. |
| Themes and syntax | PASS | Dark and light screenshots inspected. Java example produced 235 syntax tokens; XML highlighting inspected. |
| Search and keyboard | PASS | Ctrl+K opened local search; `dda` found pages and sections. Selecting a result navigated to the requested section. Escape dismissed search. |
| Code copying | PASS | Copied AshgridQuickStart.java, pasted through the UI into local search, and compared with the rendered source after the single-line input removed newlines. Browser clipboard-read API returned empty, so the actual paste supplied the verification. |
| Coordinate diagram | PASS | Defaults mapped 13.9, origin 10, scale 2 to cell 1. A keyboard step to 14 selected cell 2. Reset restored defaults. |
| Traversal diagram | PASS | Defaults visited X=0,1,2 up to excluded endpoint X=3; tMax=0 produced no callbacks. Negative start and mobile controls checked. The figure is explicitly limited to positive-X unit-grid traversal. |
| Header alignment | PASS | Compared with the local Ashcore shell. Top links are Documentation, Examples, and API reference; Maven Central points to Ashgrid 1.3.0. Browser checks confirmed active Documentation, Examples, and API reference states. Migration remains available in the sidebar. |
| Grid figures | PASS | Signed chunk/local mapping, retained empty chunks and pruning, live source/window versus detached snapshot, and all four set-operation results checked. Browser confirmed source = 9, window = 9, snapshot = 2; clear retained 4 allocated cells and prune reduced them to 0. Signed source axes start at X = −2 and Z = −1. |
| Query figures | PASS | DDA: 7 callbacks, including 4 zero-length contacts. Bresenham/supercover presets: 4/10, 5/7, and 2/8 cells; reverse coverage checked. Neighborhood counts: 6/18/26. Region counts: 7/10/8/1. Browser confirmed that the inside endpoint includes cell 2 for line of sight while raycast can hit the starting cell. |
| Raster figures | PASS | Isolated control checks covered completion, reset, cancellation, component connectivity, morphology modes/slices, and editable distance masks. Java comparison matched all 6804 morphology cell results (12 combinations × 567 cells) and all 49 values of the two-source distance mask. Browser confirmed N26 joins all three components, flood fill completes after 6 work units with 4 visits, and cancellation preserves 3 writes. |
| Introductory figures | PASS | Dependency, operation-choice, and provider flows inspected. Browser checked missing-provider output and quick-start tMax = 0 and 2.5 returning null, then tMax = 3 hitting cell 3 at tEnter = 2.5. |
| Published directory layout | PASS | Browser opened `_site` under a nested URL path. Local assets, articles, and diagrams loaded; no warning/error console messages. Authoring tools and notes are excluded. |
| Source and publishing review | PASS | Independent reviews corrected three prose inaccuracies, checked source contracts, and confirmed default branch and output-path guards. |
| GitHub deployment | NOT RUN | Workflow is prepared. No branch push, repository Pages setting change, or remote deployment was performed. |

Environment: Windows, Node.js 24.14.0, npm 11.9.0, OpenJDK 25.0.2 compiling with `--release 21`. The workflow selects Node.js 22 and Temurin 21 on Ubuntu; that remote environment was not executed in this local task. Java needed execution outside the restricted sandbox because the sandbox could not close installed compiler resources. All final example checks passed in that execution.

The test scope is the documentation and its executable examples. No Minecraft server was started; no new Maven or Gradle consumer was packaged. The library source was not modified and the complete existing Maven test suite was not rerun for this documentation-only change. The Shade configuration is a documented integration example, not a tested server artifact.

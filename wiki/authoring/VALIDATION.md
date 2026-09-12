# WIKI validation

Executed on 2026-09-12 against Ashgrid 1.3.0 and Ashcore 1.2.0.

| Check | Result | Scope |
| --- | --- | --- |
| Locked dependencies | PASS | `npm ci --no-audit --no-fund`; Tailwind 4.3.3. |
| Build | PASS | `npm run build`; generated stylesheet and `_site` artifact. |
| Content validation | PASS | 18 pages, 74 sections, 80 internal content links, navigation, version, demo mode, shell links, and local assets. |
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
| Published directory layout | PASS | Browser opened `_site` under a nested URL path. Local assets, articles, and diagrams loaded; no warning/error console messages. Authoring tools and notes are excluded. |
| Source and publishing review | PASS | Independent reviews corrected three prose inaccuracies, checked source contracts, and confirmed default branch and output-path guards. |
| GitHub deployment | NOT RUN | Workflow is prepared. No branch push, repository Pages setting change, or remote deployment was performed. |

Environment: Windows, Node.js 24.14.0, npm 11.9.0, OpenJDK 25.0.2 compiling with `--release 21`. The workflow selects Node.js 22 and Temurin 21 on Ubuntu; that remote environment was not executed in this local task. Java needed execution outside the restricted sandbox because the sandbox could not close installed compiler resources. All final example checks passed in that execution.

The test scope is the documentation and its executable examples. No Minecraft server was started; no new Maven or Gradle consumer was packaged. The library source was not modified and the complete existing Maven test suite was not rerun for this documentation-only change. The Shade configuration is a documented integration example, not a tested server artifact.

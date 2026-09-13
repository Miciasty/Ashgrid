# Authoring basis

Documentation version: Ashgrid 1.3.0 with Ashcore 1.2.0; Java release target 21. Work began from repository commit `fc582fa`; snapshot commit `cfb07e6` was created on `docs/github-pages-wiki-20260912` before edits. Library sources were not changed for the WIKI.

## Editorial and visual rules

- The supplied `Minecraft Plugins/DOCUMENTATION_DESIGN_TEMPLATE.md` defines prose and exact-name rules.
- The supplied `Minecraft Plugins/DOCUMENTATION_DESIGN_TEMPLATE/WIKI_DESIGN_TEMPLATE.md` defines the site and American English prose. Its current implementation supersedes the older language document's statement that the template directory is empty.
- The template's HTML, local renderer, styles, syntax assets, and authoring utilities were reused. Ashcore's content helper, build staging, and extra content validation were adapted. Public demo/palette pages were omitted.
- This is library documentation for plugin developers. No commands, permissions, configuration, reload behavior, or server versions were invented.
- The installed Maven POM and local Java source define the documented version. Search engines can return stale repository content; they are not the basis for current API claims.
- The visual review follows the user's preference to illustrate mechanisms wherever that helps understanding. The site contains 17 interactive figures, with finite inputs and explicit coordinates. The shared shell follows Ashcore's `Documentation`, `Examples`, `API reference`, and `Maven Central` controls.

## Template update — 2026-09-13

The current `Minecraft Plugins/DOCUMENTATION_DESIGN_TEMPLATE.md` and `DOCUMENTATION_DESIGN_TEMPLATE/WIKI_DESIGN_TEMPLATE.md` require figures near algorithm explanations, explicit 2D/3D conventions, camera controls separate from model inputs, and a full example reset. The template's `assets/app.js` supplies the mobile Maven Central placement.

Following the owner's review, the operation-choice, dependency, and service-lookup card flows were removed. Their information remains in the existing article tables and prose. Five spatial figures now have perspective 3D scenes and matching 2D slices. The local `Ashspace/wiki/assets/aabb-3d.js`, `frame-chain-3d.js`, and corresponding CSS were inspected as the interaction and rendering reference: shaded faces, ground grid, orientation key, drag orbit, wheel/keyboard zoom, segmented view buttons, and Reset view. Ashspace files were not changed.

This update is prepared on a local branch for owner acceptance. Publication of the earlier version does not approve publishing this revision.

## Source map

| Article or concern | Primary source |
| --- | --- |
| Overview, dependency versions, Java requirement, support boundary | `pom.xml`, `README.md`, `src/main/resources/META-INF/services/*` |
| Quick start | `ArrayGrid3i`, `Raycast`, `VoxelTraverser`, Ashcore `Ray`, `Vector3`, `ServiceRegistry`; compiled and executed article example |
| Grids, coordinates, views, bulk operations | [grids-sources.md](grids-sources.md) |
| Traversal, query, line and region behavior | [voxel-sources.md](voxel-sources.md) |
| Raster algorithms and stepped tasks | [operations-sources.md](operations-sources.md) |
| Services | Every Ashgrid service descriptor; Ashcore 1.2.0 `ServiceRegistry` and `Identified`; seven-provider executable check |
| API index | Public top-level types under `src/main/java/nsk/nu/ashgrid`; package-private `ChunkKey3` excluded |
| Migration | `docs/MIGRATION.md`, current source, existing regression tests |
| Troubleshooting | Error paths and contracts in the above sources; practical consumer guidance derived from them |
| Maven Shade integration | [Apache Maven Shade resource transformers](https://maven.apache.org/plugins/maven-shade-plugin/examples/resource-transformers.html), existing service descriptors, Ashcore packaging example |
| Consumer execution and dependency resolution | [Exec Maven usage](https://www.mojohaus.org/exec-maven-plugin/usage.html), [Maven copy-dependencies](https://maven.apache.org/plugins/maven-dependency-plugin/copy-dependencies-mojo.html), public Ashgrid 1.3.0 POM returned HTTP 200 |
| Pages workflow | [GitHub custom workflows for Pages](https://docs.github.com/en/pages/getting-started-with-github-pages/using-custom-workflows-with-github-pages), local Ashcore workflow; default branch verified as `master` |

The Shade POM is an integration example; it is not a claim that a server plugin was packaged or launched during this task. Maven and Gradle declarations use the exact artifact coordinates in the library POM. Public pages explain the contracts directly; readers do not need these working notes.

## Terminology

Use cell/voxel for a grid coordinate, occupancy predicate for caller-provided membership, chunk for grouped storage, view for a live wrapper, snapshot for an independent mutable dense copy, and work budget for algorithm-specific work units. Distinguish a visited voxel from a detailed block collision shape, an entry parameter from an absolute position, and task completion from budget exhaustion or cancellation.

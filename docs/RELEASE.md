# Ashgrid 1.3.0-SNAPSHOT verification and migration

This is a development build, not a published release. Work follows Blackframe contract revision 2.0
and GRID-001 through GRID-007. The starting snapshot is `d58bfba`, on
`fix/ashgrid-issues-20260909`. Source corrections are the commit that adds this document.

## Behavior and compatibility

All existing public signatures and all 70 class-file names were retained, including the public
implementations promoted in the README. `javap -public` comparison against the snapshot found no
removed signatures and two additions: `GridMath.cellCount(int,int,int)` and `floorToInt(double)`.
These additive helpers motivate a minor version, 1.3.0-SNAPSHOT. No released artifact was overwritten.

| Area | Correction and migration |
| --- | --- |
| DDA | Negative-axis face times are positive. Starting cells and exact X/Y/Z tie order are retained, including zero-length visits. NaN limits and invalid ray/index values are rejected; index exhaustion throws instead of wrapping. Tiny nonzero components on a boundary no longer produce NaN through `0 * infinity`. |
| Raycast/LOS | Both inherit DDA ordering and zero-length visits. Raycast includes the start; LOS skips the first callback. LOS uses the actual half-open segment length, without shortening by one representable double. Clipping treats parallel upper faces as excluded and empty AABBs as empty; entry boundary visits remain visible. |
| Chamfer | All 26 neighbor orientations are represented. Foreground is zero; face/edge/corner costs remain 3/4/5. Empty foreground now yields positive infinity instead of the undocumented finite `1e9f` sentinel. Consumers should use `Float.isInfinite` for that case. Distances divided by three approximate geometry; they are not character-clearance decisions. |
| Supercover | Restored the advertised full closed-cell contact set between endpoint cell centers. A corner emits all seven new touched cells in ascending X/Y/Z subset-mask order. Clients can see more callbacks than before. Bresenham remains a thin rasterizer; its full-int endpoint differences now use long arithmetic. |
| Storage/ranges | Nonpositive/overflowing dimensions fail before allocation or flattened iteration. Checked integer arithmetic replaces wrapping bounds and neighbors. Subgrids must fit source dimensions; slice indices must be valid. Invalid calls that previously clamped, overflowed, or failed later can now throw earlier. |
| Mapping | `SquareXZChunkScheme` and `VoxelSpace` retain their packages, constructors and units. Invalid participating coordinates are rejected. Negative subnormal chunk inputs remain below zero. `VoxelSpace` preserves the negative cell on quotient underflow. |
| Raster operations | Flood fill stays within the declared volume even on a clamped view. Components/morphology reject direct aliases and readable output shape mismatches before writing. Morphology compositions classify intermediate 0/1 masks as binary instead of reusing an arbitrary original-source predicate. Overlapping views remain a caller precondition; they are not generically detectable. |
| Regions/data | Fractional empty AABBs emit nothing; inclusive int-max loops terminate without wrapping. Shape iterators validate radii and candidate bounds. Views remain live, and public neighborhood arrays are compatibility references that callers must treat as read-only. |

No output compatibility across provider/dependency versions or cross-platform floating-point bitwise
identity is claimed. DDA tie detection uses exact computed double equality, without epsilon merging.
Debugger inspection found a near-tie interval from `6.080193253507654` to `6.080193253507655`: these
are adjacent doubles, so a computed midpoint rounds to an endpoint. Tests check interior membership
only when an interior parameter is representable; separate known-result tests cover exact ties.
Input rounding and large-coordinate precision limits remain explicit in README and Javadoc.

## Local verification, 2026-09-09

Environment: Windows 11 amd64, Oracle OpenJDK **25.0.2+10-69**, Apache Maven **3.9.16**.
Compiler plugin **3.13.0** enforces **release 21**. Local runtime testing was on Java 25;
Temurin 21/25 Linux CI is configured but was not run remotely in this session.

```sh
mvn -version
mvn -B clean verify org.apache.maven.plugins:maven-dependency-plugin:3.8.1:tree
```

Final result: **80 unit tests + 4 packaged-artifact tests passed**, no failures/errors/skips.
The original snapshot passed its existing 53 tests. The first two new regression tests failed before
the fixes: DDA returned `-0.2` instead of `0.2`, and reflected Chamfer cost was `6` instead of `4`.
DDA values were confirmed in IntelliJ at the callback, with the test caller visible in the stack.
An initial supercover comparison failed with 10 cells versus the 22 closed-cell contacts expected for
the segment `(0,0,0)` to `(-3,-3,-3)`.

Tests cover six axial signs, mixed directions, negative origins, faces/edges/corners, exact endpoints,
early cancellation, zero/infinite/invalid limits, int limits, and numerical edge cases. Chamfer is
compared with Dijkstra over every single-source position in three non-cubic grids and seeded multiple
source masks. Supercover is compared with independent slab intersections for all 343 endpoint offsets
in `[-3,3]^3`, in both directions. Resource and mutation tests use small grids or nonallocating stubs.

`PackagedArtifactIT` checks the exact main/sources/Javadoc names, Java 21 bytecode, Maven coordinates,
the resolved Ashcore version, seven service resources and every advertised provider ID. It compiles
and runs the README example using only the main JAR and Ashcore, with an isolated classloader.
It also builds a duplicate-provider fixture and checks missing/duplicate failures.
Javadoc uses `doclint=all,-missing` with `failOnError=true`; generation passed. IntelliJ build passed.

Default dependency resolution is `dev.nasaka.blackframe:ashcore:1.0.1:compile`.
JUnit 5.10.2 and all its transitive dependencies are test-only. There are no upward or external
production dependencies. The tested default Ashcore JAR SHA-256 is
`0ea3a990d28a01aac97c574497c21be2f2ced5b90eaa03637c8499cbf1d62d0b`.

The compiler configuration follows the [Maven release-option contract](https://maven.apache.org/plugins/maven-compiler-plugin/examples/set-compiler-release.html).
Artifact tests run after packaging and are checked at verify, following the
[Failsafe lifecycle](https://maven.apache.org/surefire/maven-failsafe-plugin/).

## Consumer integration and handoffs

Source/test copies were built in ignored `.verification/consumers/`, with separate artifact storage
in `.verification/repository`. Only copied POM versions/dependencies changed. Original Ashcore,
Ashspace, Ashtrace and Ashnav checkouts were not modified. Ashgrid production code has no dependency
on these consumers; [src/it/ConsumerContracts.java](../src/it/ConsumerContracts.java) is a standalone
integration fixture outside the normal Maven source roots.

| Source | Integration coordinates | Result |
| --- | --- | --- |
| Ashcore `e5194402d0c67d2f29909cfa8c6c51ae137644e7` | `ashcore:1.1.0-SNAPSHOT` | Existing verified JAR installed into the isolated repository |
| Ashgrid, this correction | `ashgrid:1.3.0-SNAPSHOT`, copied POM selects Ashcore 1.1.0-SNAPSHOT | clean verify; unit and artifact tests passed |
| Ashspace `3f1b9101e19507590479fe32e2d84903dbac261d` | `ashspace:1.0.0-gridcheck-SNAPSHOT` | 31 tests passed |
| Ashtrace `5c516fc4a891433ce10c920abaac3846fbee9926` | `ashtrace:1.0.0-gridcheck-SNAPSHOT` | 43 tests passed |
| Ashnav `08e7d26a8be5e379e940166dd94307ef5aa2baaa` | `ashnav:1.0.0-gridcheck-SNAPSHOT` | 29 tests passed |

All consumer dependency trees resolve the exact copied versions and Ashcore 1.1.0-SNAPSHOT.
The tested Ashcore JAR SHA-256 is `9b7758dee82a8fa7338afcc7b7bf22fe02682aaff670c10dd4971be9390c845f`.
The installed Ashgrid consumer-test JAR SHA-256 is
`452a7b7a15a5fc7dc471e73d480fb101d0530fc69ab1d5be43da5478f92f70b8`.
Archive timestamps can change checksums on rebuilding; these identify this integration run.
All 70 production class entries in the final default JAR are byte-for-byte identical to those in
the installed consumer-test JAR. The final expanded Ashgrid tests also passed with Ashcore 1.1.0-SNAPSHOT
(80 unit tests and 4 artifact tests).

The run used the following command for each copied POM, in dependency order, with absolute local
paths in actual invocations. Exact JAR/POM pairs were installed with `maven-install-plugin:3.1.3:install-file`.
The normal Maven cache was used only as a file-repository source; it was not overwritten with modified releases.

```sh
mvn -B -s .verification/settings.xml -Dmaven.repo.local=.verification/repository \
  -f <copied-project>/pom.xml clean verify org.apache.maven.plugins:maven-dependency-plugin:3.8.1:tree
javac --release 21 -cp "<core,grid,space,trace,nav JAR classpath>" -d <classes> src/it/ConsumerContracts.java
java -cp "<classes plus the same JARs>" ConsumerContracts
```

The standalone fixture passed **SPACE-001** mapping agreement for scales 0.5, 1 and 2, translated
origins, negative cells and adjacent representable values at cell/chunk boundaries. It passed
**TRACE-002** negative first-hit distance and interval order through a translated frame.
These checks do not establish agreement for arbitrary scales: Ashspace currently multiplies by
the reciprocal while VoxelSpace divides, and rounding can differ at a boundary. Coordinate that
remaining broader mapping question under SPACE-001; no common epsilon or unit change was introduced.

The Ashspace and both Ashtrace README examples compiled and ran. Ashnav's statement snippet compiled
and ran after adding only a class/main wrapper around its existing imports/statements; it printed `3`.
This preserves `SquareXZChunkScheme` use for **SPACE-005/NAV-007**. **CORE-003/GRID-005** retain explicit
provider IDs and unspecified registry enumeration. **NAV-002/NAV-004** can rely on the documented
finite-volume, view and neighborhood ownership rules, but character movement remains Ashnav/caller policy.

Execution scripts, dependency trees, API listings and logs are retained locally under ignored
`.verification/` (`verify-consumers.ps1`, `compare-api.ps1`, `verify-final.log`, `consumers-run.log`,
`*-verify.log`). Consumer backlog ownership remains with its repository; these handoffs do not close
all of SPACE-001 or TRACE-002's broader requirements.

## Build and publishing routes

CI verifies every pushed branch and every pull request on Temurin 21 and 25. Artifact names come
from Maven's finalName, with exact main, sources and Javadoc files checked; files are not selected
by first-match glob or renamed in target. The metadata shell script passed `bash -n`.

| Destination | Route | Evidence/status |
| --- | --- | --- |
| GitHub Packages | Release workflow checks `v<version>`, rejects SNAPSHOT, runs clean verify, then `mvn -B deploy` with repository ID `github` | Configured; not executed. Availability unverified. |
| GitHub Release assets | Same workflow uploads the exact main/sources/Javadoc files with `gh release upload` | Configured; not executed. No release/tag created. |
| Maven Central | Existing manual `central` profile signs and uses the Central Publishing plugin | Retained, not executed. No claim of publication for 1.2.0 or 1.3.0-SNAPSHOT. |

Ordinary verification does not publish or sign. The Central profile is never activated by CI or the
GitHub workflow. The owner-managed command is `mvn -B -Pcentral clean deploy` after release review,
with credentials under server ID `central` and GPG configuration. Its `autoPublish=true` setting
publishes after validation, so it must not be used as a test command.

Before an actual release, select an unused non-SNAPSHOT version, agree the released Ashcore version,
run remote CI on the release commit, verify destination credentials/access and record the resulting
tag, commit, artifact coordinates and availability links. Local build success alone proves none of
those external publication facts.

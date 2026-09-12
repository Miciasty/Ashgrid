# Voxel documentation sources

Scope: `content/voxel.js`, Ashgrid 1.3.0 with declared Ashcore 1.2.0 dependency. Public prose follows the English interface and article rules of `Minecraft Plugins/DOCUMENTATION_DESIGN_TEMPLATE/WIKI_DESIGN_TEMPLATE.md`; wording follows `Minecraft Plugins/DOCUMENTATION_DESIGN_TEMPLATE.md`. This note is authoring material and is not an article.

## Traversal

- `src/main/java/nsk/nu/ashgrid/api/voxel/traversal/VoxelTraverser.java`: unit-grid input, normalized direction, distance units, half-open callback intervals, exact endpoint exclusion, invalid limits, integer bounds, repeatability scope.
- `src/main/java/nsk/nu/ashgrid/api/voxel/traversal/CellVisitor.java`: return value and zero-length callback semantics.
- `src/main/java/nsk/nu/ashgrid/implementation/voxel/traversal/DDA3DTraverser.java`: floor-selected start, exact X–Y–Z tie order, zero-direction behavior, immediate stop, `Math.addExact` overflow.
- `src/main/java/nsk/nu/ashgrid/api/voxel/traversal/VoxelTraversers.java`: finite half-open clipping, original-distance restoration, retained negative entry callback, parallel upper-face exclusion, clipped ID.
- `src/test/java/nsk/nu/ashgrid/integration/voxel/TraversalBoundaryTest.java`: positive/negative axis directions, origin and corner ties, endpoint exclusion, zero/infinite/invalid limits, index overflow, clipping negative entry and parallel upper face.
- `src/test/java/nsk/nu/ashgrid/integration/voxel/TraversalAndQueryIntegrationTest.java`: SPI selection, monotonic callbacks, clipped entry and original distances.
- `src/test/java/nsk/nu/ashgrid/api/voxel/traversal/VoxelTraversersTest.java`: clipped ID, miss and invalid limit.

The clipping warning is deliberate: the source and negative-entry test preserve a zero-length callback for an upper-face cell outside an integer-aligned clip. Clipping is not documented as an index-bounds filter.

## Raycast and line of sight

- `src/main/java/nsk/nu/ashgrid/api/voxel/query/Raycast.java`: `Hit` fields, first accepted callback, null miss, occupied start, zero-length callback acceptance, immediate stop.
- `src/main/java/nsk/nu/ashgrid/api/voxel/query/LineOfSight.java`: first callback exclusion, `[a,b)` segment, equal endpoints, endpoint/length validation, boolean occlusion semantics.
- `src/test/java/nsk/nu/ashgrid/integration/voxel/TraversalAndQueryIntegrationTest.java`: first occupied voxel, blocked segment and starting-cell exclusion.
- `src/test/java/nsk/nu/ashgrid/integration/voxel/TraversalBoundaryTest.java`: negative query distances and endpoint cells.

The distinction between a grid-cell hit and block geometry follows the API inputs: occupancy is a boolean cell predicate; neither query receives or intersects block collision shapes. The clipped line-of-sight caveat follows the implementation's unconditional skip of its delegate's first callback.

## Lines, regions, and neighborhoods

- `src/main/java/nsk/nu/ashgrid/api/voxel/draw/Line3D.java` and `implementation/voxel/draw/BresenhamLine3D.java`: inclusive integer endpoints, thin-line contract, dominant-axis order, reversal limitation, early exit, long deltas.
- `src/main/java/nsk/nu/ashgrid/api/voxel/draw/Line3DSupercover.java` and `implementation/voxel/draw/SupercoverLine3D.java`: center-to-center segment, closed-cell contacts, masks 1–7, uniqueness, reversal-set contract.
- `src/test/java/nsk/nu/ashgrid/integration/voxel/SupercoverContractTest.java`: exhaustive small endpoint comparison with closed-cell intersections and reversal sets; extreme endpoints with immediate cancellation.
- `src/main/java/nsk/nu/ashgrid/api/voxel/region/RegionIterator.java`, `RegionIterators.java`, and `implementation/voxel/region/AABBVoxelIterator.java`: half-open AABB intersections, inclusive integer box, center-based sphere/cylinder rules, shape validation and synchronous loop order.
- `src/test/java/nsk/nu/ashgrid/api/voxel/region/RegionIteratorsTest.java`: inclusive box count, sphere center cell and cylinder vertical span.
- `src/main/java/nsk/nu/ashgrid/api/voxel/neighborhood/Neighborhood3D.java`: neighbor memberships, declared array order, shared mutation and deep-copy constraints.

## Interactive figures

`assets/diagrams-queries.js` implements five bounded browser illustrations. They
run independently of Java and Minecraft. All coordinates and distances are in
unit-grid cells; each XY slice labels its fixed Z and uses X rightward, Y downward.
Numerical labels, contact letters, and cell titles supplement color.

- `dda-ties`: mirrors the comparison and one-axis stepping in
  `DDA3DTraverser`. Fixed origin `(0.5,0.5,0.5)`, normalized direction `(1,1,1)`,
  and `tMax=3` give seven visits: `(0,0,0)`, `(1,0,0)`, `(1,1,0)`, `(1,1,1)`,
  `(2,1,1)`, `(2,2,1)`, `(2,2,2)`. X and Y steps at both corners produce
  zero-length intervals. Z steps enter the next diagonal cell for positive travel.
  Displayed distances are rounded to three decimals; comparison values are not.
  Default callback is 1/7; Previous, Next and Reset control progression without
  autoplay. The final interval ends at 3, after the second corner at `1.5*sqrt(3)`.
- `query-comparison`: applies `Raycast` and `LineOfSight` predicate rules to a
  positive-X DDA sequence. Defaults: cells 0 and 2 occupied, origin X=0.5,
  endpoint X=2; raycast hits cell 0 while line of sight is clear. Endpoint X=2.5
  adds callback cell 2. Equal endpoints produce no callbacks, raycast null and
  line-of-sight true. Each query stops on its own first accepted cell. Cell
  buttons toggle occupancy; a labelled select switches the endpoint. The
  occupancy line is offset vertically for readability and is labelled as such.
- `line-coverage`: ports `BresenhamLine3D` and `SupercoverLine3D` for bounded
  integer presets. Exact integer rational comparisons preserve tied axis masks.
  Default `(0,0,0)` to `(3,3,0)` gives 4 thin cells and 10 supercover cells;
  `(4,2,0)` gives 5 and 7; the `(1,1,1)` corner gives 2 and 8. Reversal switches
  endpoints and recalculates callback order. The corner view uses two separate
  XY slices, not a projected two-dimensional replacement for the 3D algorithm.
- `neighborhoods`: selects offsets by number of nonzero coordinates in the
  `[-1,1]^3` domain, matching the memberships in `Neighborhood3D`. N6 layer
  counts are 1+4+1, N18 counts 5+8+5, N26 counts 9+8+9. Center is always excluded.
  Default N6; one labelled select controls N6/N18/N26. F/E/C letters describe
  face/edge/corner contact; they do not claim the declared array order.
- `region-selection`: bounded predicates match `RegionIterators` and
  `AABBVoxelIterator` for the exact article examples: sphere radius 1 centered
  `(0.5,0.5,0.5)` selects 7 cells; XZ cylinder with that center/radius and inclusive
  Y 0..1 selects 10; integer box 0..1 on all axes selects 8; continuous half-open
  AABB `[0,1)^3` selects 1. Default sphere; one labelled select changes the shape.

## Java example verification

Every Java block has a unique public class, imports, `main`, and an adjacent exact expected-output block. Examples use only Ashgrid and its declared Ashcore dependency. Floating output uses `Locale.ROOT`. Root-task verification compiles and executes the examples against the built Ashgrid classes and Ashcore 1.2.0 jar, then compares stdout to the adjacent output. This note records source review; the final build report records actual execution results.

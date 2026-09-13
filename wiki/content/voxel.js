(() => {
  const {code, table, note} = window.WIKI_HTML;

  window.WIKI_PAGES.push({
    id: 'traversal',
    category: 'Voxel queries',
    title: 'Voxel traversal',
    description: 'Visit cells along a ray, read entry and exit distances, and clip the traversal to a region.',
    kind: 'guide',
    readingTime: 9,
    intro: '<p>A <code>VoxelTraverser</code> turns a continuous ray into ordered cell callbacks. Use it when your operation needs to inspect cells along a direction, stop at a condition, or measure travel through each cell.</p>',
    sections: [
      {
        id: 'ray-and-cell-coordinates',
        title: 'Ray and cell coordinates',
        html: `<p>Supply an Ashcore <code>Ray</code> in continuous unit-grid coordinates. Cell <code>(x,y,z)</code> occupies <code>[x,x+1) × [y,y+1) × [z,z+1)</code>. Its center is <code>(x+0.5,y+0.5,z+0.5)</code>. A negative position such as <code>-0.2</code> belongs to cell <code>-1</code>.</p>
          <p>Ashcore normalizes the ray direction. The parameter <code>t</code> therefore measures distance in cells, starting at the ray origin. One cell equals one Minecraft block only when your integration uses that mapping. Convert world coordinates before traversal if your grid has another origin or scale.</p>
          ${table(['Input', 'Meaning and limits'], [
            ['<code>ray</code>', 'Finite origin and a finite, nonzero direction. The floor of each starting coordinate must fit a signed <code>int</code>.'],
            ['<code>tMax</code>', 'Distance limit in cells. Zero produces no callbacks. Negative values and <code>NaN</code> are rejected.'],
            ['<code>visitor</code>', 'Receives <code>x, y, z, tEnter, tExit</code>. Return <code>true</code> to continue or <code>false</code> to stop immediately.']
          ])}
          <p>The built-in provider ID is <code>dda</code>. Select it through Ashcore’s <code>ServiceRegistry</code>, as shown below. The examples require the Ashgrid and Ashcore dependencies described in <a href="#/installation">Installation</a>.</p>`
      },
      {
        id: 'read-visit-intervals',
        title: 'Read visit intervals',
        html: `<p>The visitor receives a half-open interval <code>[tEnter,tExit)</code> for each callback. Distances satisfy <code>0 ≤ tEnter ≤ tExit ≤ tMax</code> and never decrease. The final interval can end at your distance limit before the ray reaches the next cell face.</p>
          <p>This ray starts at the center of cell <code>(0,0,0)</code> and travels along positive X. Its limit reaches the face at <code>x=3</code>. Cell <code>(3,0,0)</code> receives no callback because the exact endpoint is excluded.</p>
          <p>Adjust the origin and distance limit to compare the visited cells and their intervals. This illustration covers positive-X travel only. Set the limit to zero to see the empty traversal.</p>
          <div data-diagram="raycast"></div>
          ${code('java', 'TraversalExample.java', `import java.util.Locale;
import nsk.nu.ashcore.api.geometry.Ray;
import nsk.nu.ashcore.api.math.Vector3;
import nsk.nu.ashcore.api.spi.ServiceRegistry;
import nsk.nu.ashgrid.api.voxel.traversal.VoxelTraverser;

public class TraversalExample {
    public static void main(String[] args) {
        VoxelTraverser dda = ServiceRegistry.of(VoxelTraverser.class).require("dda");
        Ray ray = new Ray(new Vector3(0.5, 0.5, 0.5), new Vector3(1, 0, 0));
        dda.traverse(ray, 2.5, (x, y, z, enter, exit) -> {
            System.out.printf(Locale.ROOT,
                    "cell=(%d,%d,%d), enter=%.1f, exit=%.1f%n", x, y, z, enter, exit);
            return true;
        });
    }
}`)}
          ${code('output', 'Expected output', `cell=(0,0,0), enter=0.0, exit=0.5
cell=(1,0,0), enter=0.5, exit=1.5
cell=(2,0,0), enter=1.5, exit=2.5`)}
          <p>For a cell visited over a positive interval, <code>tExit - tEnter</code> is the travel distance in that cell. Use <code>ray.at(tEnter)</code> to recover the entry point. The interval describes the grid cell; it contains no information about a block’s collision shape.</p>`
      },
      {
        id: 'faces-edges-and-corners',
        title: 'Faces, edges, and corners',
        html: `<p>The <code>dda</code> provider starts at <code>floor(origin)</code>. At an exact tie, it steps X, then Y, then Z. Intermediate callbacks can have <code>tEnter == tExit</code>. These callbacks report a boundary contact without positive travel through the cell.</p>
          <p>The diagram starts with the negative ray from the Java example below. Step through its four callbacks. Switch to the positive example to compare ties after entering a cell. The 3D view and XY slices show the same callbacks; camera controls change only their projection.</p>
          <div data-diagram="dda-ties"></div>
          <p>A negative direction from an integer face first emits the floor-selected starting cell with a zero-length interval. In this example, the ray starts at the corner <code>(0,0,0)</code> and moves toward negative X, Y, and Z. The first three callbacks have zero length.</p>
          ${code('java', 'TraversalTiesExample.java', `import java.util.Locale;
import nsk.nu.ashcore.api.geometry.Ray;
import nsk.nu.ashcore.api.math.Vector3;
import nsk.nu.ashcore.api.spi.ServiceRegistry;
import nsk.nu.ashgrid.api.voxel.traversal.VoxelTraverser;

public class TraversalTiesExample {
    public static void main(String[] args) {
        VoxelTraverser dda = ServiceRegistry.of(VoxelTraverser.class).require("dda");
        Ray ray = new Ray(new Vector3(0, 0, 0), new Vector3(-1, -1, -1));
        dda.traverse(ray, 0.5, (x, y, z, enter, exit) -> {
            System.out.printf(Locale.ROOT,
                    "cell=(%d,%d,%d), enter=%.1f, exit=%.1f%n", x, y, z, enter, exit);
            return true;
        });
    }
}`)}
          ${code('output', 'Expected output', `cell=(0,0,0), enter=0.0, exit=0.0
cell=(-1,0,0), enter=0.0, exit=0.0
cell=(-1,-1,0), enter=0.0, exit=0.0
cell=(-1,-1,-1), enter=0.0, exit=0.5`)}
          <p>A zero direction component stays on the floor-selected side of its face. The provider applies no membership epsilon. Nearby floating-point values can therefore follow a different sequence from an exact tie.</p>
          ${note('DDA does not visit every contact cell', '<p>At a corner, DDA emits its X–Y–Z sequence rather than every adjacent cell. Use <a href="#/lines-regions?section=choose-a-line">supercover lines</a> when you need every closed cell touched by a segment between cell centers.</p>')}`
      },
      {
        id: 'clip-a-traversal',
        title: 'Clip a traversal',
        html: `<p>Wrap a traverser with <code>VoxelTraversers.clipped(delegate, box)</code> to restrict its ray interval to a finite, half-open <code>AxisAlignedBox</code>. Callback distances still refer to the original ray origin. A missed or empty box produces no callbacks.</p>
          <p>The wrapper retains the delegate’s entry-boundary callbacks. Entering the upper X face of <code>[0,2) × [0,1) × [0,1)</code> in a negative direction therefore emits cell <code>(2,0,0)</code> with zero length before cells inside the box.</p>
          ${code('java', 'ClippedTraversalExample.java', `import java.util.Locale;
import nsk.nu.ashcore.api.geometry.AxisAlignedBox;
import nsk.nu.ashcore.api.geometry.Ray;
import nsk.nu.ashcore.api.math.Vector3;
import nsk.nu.ashcore.api.spi.ServiceRegistry;
import nsk.nu.ashgrid.api.voxel.traversal.VoxelTraverser;
import nsk.nu.ashgrid.api.voxel.traversal.VoxelTraversers;

public class ClippedTraversalExample {
    public static void main(String[] args) {
        VoxelTraverser dda = ServiceRegistry.of(VoxelTraverser.class).require("dda");
        AxisAlignedBox box = new AxisAlignedBox(
                new Vector3(0, 0, 0), new Vector3(2, 1, 1));
        VoxelTraverser clipped = VoxelTraversers.clipped(dda, box);
        Ray ray = new Ray(new Vector3(3, 0.5, 0.5), new Vector3(-1, 0, 0));
        clipped.traverse(ray, 10, (x, y, z, enter, exit) -> {
            System.out.printf(Locale.ROOT,
                    "cell=(%d,%d,%d), enter=%.1f, exit=%.1f%n", x, y, z, enter, exit);
            return true;
        });
    }
}`)}
          ${code('output', 'Expected output', `cell=(2,0,0), enter=1.0, exit=1.0
cell=(1,0,0), enter=1.0, exit=2.0
cell=(0,0,0), enter=2.0, exit=3.0`)}
          <p>A parallel ray lying on an upper box face is excluded. The clipped provider ID is the delegate ID plus <code>-clipped</code>; wrapping <code>dda</code> produces <code>dda-clipped</code>.</p>
          ${note('Check storage bounds in the callback', '<p>Clipping limits the ray interval. It does not guarantee that every reported cell index lies inside your storage bounds, because it retains zero-length entry visits. Check indices before accessing a bounded grid. If your calculation requires positive travel only, also require <code>tExit &gt; tEnter</code>.</p>')}`
      },
      {
        id: 'stop-and-bound-work',
        title: 'Stop and bound work',
        html: `<p>Return <code>false</code> from the visitor when your operation has enough cells. Traversal stops before another callback or cell step. The operation runs synchronously on the calling thread; the traverser does not load Minecraft chunks or schedule server work.</p>
          <p><code>Double.POSITIVE_INFINITY</code> is accepted as a distance limit, but your callback must stop before traversal exhausts the signed <code>int</code> cell range. DDA throws <code>ArithmeticException</code> if the next step would overflow a coordinate. Prefer a finite distance for bounded queries.</p>
          <p>Repeatable results require fixed inputs, callback behavior, provider and dependency versions, and environment. If the callback reads a grid, keep that grid stable during the traversal. Provider enumeration order and bitwise identity across versions or platforms are not guaranteed.</p>`
      }
    ]
  }, {
    id: 'raycasting',
    category: 'Voxel queries',
    title: 'Raycast and line of sight',
    description: 'Find the first occupied callback cell or check whether cells block a segment.',
    kind: 'guide',
    readingTime: 7,
    intro: '<p><code>Raycast</code> and <code>LineOfSight</code> apply predicates to a <a href="#/traversal">voxel traversal</a>. Your predicate decides which cells count as occupied or blocking. The library supplies cell coordinates and traversal distances.</p>',
    sections: [
      {
        id: 'find-the-first-occupied-cell',
        title: 'Find the first occupied cell',
        html: `<p>Construct <code>Raycast</code> with a traverser, then call <code>first(ray, tMax, occupancy)</code>. The occupancy predicate returns <code>true</code> for a cell you consider solid. The query stops at the first matching callback and returns <code>Raycast.Hit</code>. It returns <code>null</code> when no callback matches.</p>
          <p>Toggle occupied cells to compare raycast with line of sight. Start with the occupied origin: raycast reports a hit there, while line of sight skips that first callback. Move the endpoint from a face into a cell to see when the endpoint cell is checked.</p>
          <div data-diagram="query-comparison"></div>
          <p>This example treats cell <code>(2,0,0)</code> as occupied. The ray reaches that cell after traveling <code>1.5</code> cells. A second query has no hit within its distance limit. A third query demonstrates that an occupied starting cell counts as a hit.</p>
          ${code('java', 'RaycastExample.java', `import java.util.Locale;
import nsk.nu.ashcore.api.geometry.Ray;
import nsk.nu.ashcore.api.math.Vector3;
import nsk.nu.ashcore.api.spi.ServiceRegistry;
import nsk.nu.ashgrid.api.voxel.query.Raycast;
import nsk.nu.ashgrid.api.voxel.traversal.VoxelTraverser;

public class RaycastExample {
    public static void main(String[] args) {
        VoxelTraverser dda = ServiceRegistry.of(VoxelTraverser.class).require("dda");
        Raycast raycast = new Raycast(dda);
        Ray ray = new Ray(new Vector3(0.5, 0.5, 0.5), new Vector3(1, 0, 0));
        Raycast.Hit hit = raycast.first(ray, 4,
                (x, y, z) -> x == 2 && y == 0 && z == 0);
        if (hit != null) {
            Vector3 entry = ray.at(hit.tEnter());
            System.out.printf(Locale.ROOT,
                    "cell=(%d,%d,%d), enter=%.1f, exit=%.1f, entryX=%.1f%n",
                    hit.x(), hit.y(), hit.z(), hit.tEnter(), hit.tExit(), entry.x());
        }
        System.out.println("miss=" + (raycast.first(ray, 4, (x, y, z) -> x == 8) == null));
        Raycast.Hit start = raycast.first(ray, 4, (x, y, z) -> x == 0);
        System.out.printf(Locale.ROOT, "startEnter=%.1f%n", start.tEnter());
    }
}`)}
          ${code('output', 'Expected output', `cell=(2,0,0), enter=1.5, exit=2.5, entryX=2.0
miss=true
startEnter=0.0`)}
          ${table(['Hit field', 'Meaning'], [
            ['<code>x(), y(), z()</code>', 'Integer coordinates of the first occupied callback cell.'],
            ['<code>tEnter()</code>', 'Entry distance from the ray origin in cells. It can be zero for a starting-cell hit.'],
            ['<code>tExit()</code>', 'Exit distance, capped by the traversal limit. It can equal <code>tEnter()</code> for a boundary visit.']
          ])}`
      },
      {
        id: 'understand-a-hit',
        title: 'Understand a hit',
        html: `<p>A hit means that your occupancy predicate accepted a callback cell. <code>Raycast</code> does not test the shape inside that cell, calculate a surface normal, or identify a Minecraft block face. For stairs, slabs, or custom geometry, a cell hit is a candidate for a separate shape-intersection test.</p>
          <p>When the origin is outside a cell, <code>ray.at(hit.tEnter())</code> gives its traversal entry point. When the origin is inside the hit cell, it gives the origin. Neither result alone establishes an intersection with the object’s surface.</p>
          <p><code>first</code> includes the starting cell and zero-length boundary callbacks. At ties, the selected traverser decides the order. With <code>dda</code>, X precedes Y, then Z. An occupied boundary-only cell can therefore become the first hit. A cell reached only at exactly <code>tMax</code> is excluded.</p>
          ${note('A clipped raycast retains entry-boundary hits', '<p><code>VoxelTraversers.clipped</code> preserves the delegate’s zero-length entry callbacks. An occupied upper-face cell can be returned even though its interval has no positive length inside the clip. Use <code>VoxelTraverser.traverse</code> directly when your acceptance rule must inspect the interval before testing occupancy.</p>')}`
      },
      {
        id: 'check-line-of-sight',
        title: 'Check line of sight',
        html: `<p>Call <code>LineOfSight.clear(a, b, occluder)</code> with finite points in unit-grid coordinates. The predicate returns <code>true</code> when a cell blocks the segment. The method returns <code>false</code> at the first blocking callback and <code>true</code> if none blocks it.</p>
          <p>The checked segment is <code>[a,b)</code>. The query skips the first callback cell. It checks every subsequent callback, including zero-length boundary visits. Equal finite endpoints return <code>true</code> without calling the occluder.</p>
          ${code('java', 'LineOfSightExample.java', `import nsk.nu.ashcore.api.math.Vector3;
import nsk.nu.ashcore.api.spi.ServiceRegistry;
import nsk.nu.ashgrid.api.voxel.query.LineOfSight;
import nsk.nu.ashgrid.api.voxel.traversal.VoxelTraverser;

public class LineOfSightExample {
    public static void main(String[] args) {
        VoxelTraverser dda = ServiceRegistry.of(VoxelTraverser.class).require("dda");
        LineOfSight sight = new LineOfSight(dda);
        Vector3 start = new Vector3(0.5, 0.5, 0.5);
        Vector3 boundaryEnd = new Vector3(2, 0.5, 0.5);
        Vector3 insideEnd = new Vector3(2.5, 0.5, 0.5);
        System.out.println("startOnly=" + sight.clear(start, boundaryEnd, (x, y, z) -> x == 0));
        System.out.println("middleBlocked=" + sight.clear(start, boundaryEnd, (x, y, z) -> x == 1));
        System.out.println("boundaryEnd=" + sight.clear(start, boundaryEnd, (x, y, z) -> x == 2));
        System.out.println("insideEnd=" + sight.clear(start, insideEnd, (x, y, z) -> x == 2));
        System.out.println("equalPoints=" + sight.clear(start, start, (x, y, z) -> true));
    }
}`)}
          ${code('output', 'Expected output', `startOnly=true
middleBlocked=false
boundaryEnd=true
insideEnd=false
equalPoints=true`)}
          <p>The endpoint point is excluded, but the cell containing that point may still be checked. When <code>b</code> is inside cell <code>(2,0,0)</code>, the ray travels through part of that cell before reaching <code>b</code>. The fourth result is therefore blocked.</p>
          <p>“Skip the first callback” also matters at boundaries and with clipping. DDA can emit a zero-length starting callback before entering the next cell. Line of sight skips only that first callback. If a clipped traverser begins later along the segment, line of sight skips its first emitted cell.</p>`
      },
      {
        id: 'supply-occupancy-data',
        title: 'Supply occupancy data',
        html: `<p>Use predicates to connect these queries to your data: a voxel array, an occupancy set, or a server-managed snapshot. Ashgrid does not choose which materials are solid. A boolean occupancy rule must come from your application.</p>
          <p>Keep occupancy and the grid stable while a query runs if you need repeatable results. Callbacks execute synchronously. Any Minecraft API access must follow that API’s thread and chunk-access rules; the query performs no scheduling or chunk loading for you.</p>
          <p>Raycast inherits the traverser’s distance and index limits. Line of sight rejects non-finite endpoints and a non-finite segment length. Both queries inherit the selected traversal’s boundary behavior. Use <a href="#/lines-regions?section=choose-a-line">supercover</a> when your rule requires all closed cell contacts between cell centers.</p>`
      }
    ]
  }, {
    id: 'lines-regions',
    category: 'Voxel queries',
    title: 'Lines, regions, and neighbors',
    description: 'Choose thin or supercover lines, enumerate regions, and select face, edge, or corner neighbors.',
    kind: 'guide',
    readingTime: 8,
    intro: '<p>Line tracing produces cell coordinates between integer endpoints. Region iterators enumerate cells selected by a box, sphere, or cylinder. Neighborhood arrays provide offsets for inspecting adjacent cells. Your callbacks perform the reads or changes.</p>',
    sections: [
      {
        id: 'choose-a-line',
        title: 'Choose a line',
        html: `<p>Choose the line algorithm from the contact rule your operation needs. A thin line is useful for a one-cell path. Supercover includes every cell touched by the continuous segment between the centers of the endpoint cells.</p>
          ${table(['API / provider ID', 'Cells visited', 'Boundary and order rules'], [
            ['<code>Line3D</code> / <code>bresenham3d</code>', 'A thin discrete line, including both endpoint cells.', 'Dominant-axis ties prefer X, then Y, then Z. Reversing endpoints may select different tie cells.'],
            ['<code>Line3DSupercover</code> / <code>supercover3d</code>', 'Every closed unit cell touched by the segment between endpoint cell centers, including edge and corner contacts.', 'Each cell appears once. Reversal preserves the set, but not necessarily the reverse order.'],
            ['<code>VoxelTraverser</code> / <code>dda</code>', 'Ordered callbacks along a continuous ray, with distance intervals.', 'Includes the starting cell, excludes the exact distance endpoint, and emits selected zero-length tie callbacks. See <a href="#/traversal">Voxel traversal</a>.']
          ])}
          <p>Both line APIs accept signed <code>int</code> endpoints and include both endpoints. Equal endpoints emit one cell. Returning <code>false</code> from the visitor stops immediately. Full integer endpoint differences are supported, but a long line still requires work proportional to the emitted cells.</p>`
      },
      {
        id: 'compare-corner-contacts',
        title: 'Compare corner contacts',
        html: `<p>Trace from cell <code>(0,0,0)</code> to cell <code>(1,1,1)</code>. The segment between their centers passes through a grid corner. Bresenham emits two cells. Supercover emits all eight cells sharing that corner.</p>
          <p>The diagram uses these same endpoints by default. Each panel shows separate XY slices. Switch to a planar segment or reverse its endpoints to compare membership and visit order.</p>
          <div data-diagram="line-coverage"></div>
          ${code('java', 'VoxelLinesExample.java', `import java.util.ArrayList;
import java.util.List;
import nsk.nu.ashcore.api.spi.ServiceRegistry;
import nsk.nu.ashgrid.api.voxel.draw.Line3D;
import nsk.nu.ashgrid.api.voxel.draw.Line3DSupercover;

public class VoxelLinesExample {
    public static void main(String[] args) {
        Line3D thin = ServiceRegistry.of(Line3D.class).require("bresenham3d");
        Line3DSupercover cover = ServiceRegistry.of(Line3DSupercover.class).require("supercover3d");
        List<String> thinCells = new ArrayList<>();
        List<String> coverCells = new ArrayList<>();
        thin.trace(0, 0, 0, 1, 1, 1, (x, y, z) -> {
            thinCells.add("(" + x + "," + y + "," + z + ")");
            return true;
        });
        cover.trace(0, 0, 0, 1, 1, 1, (x, y, z) -> {
            coverCells.add("(" + x + "," + y + "," + z + ")");
            return true;
        });
        System.out.println("thin=" + String.join(" ", thinCells));
        System.out.println("supercover=" + String.join(" ", coverCells));
    }
}`)}
          ${code('output', 'Expected output', `thin=(0,0,0) (1,1,1)
supercover=(0,0,0) (1,0,0) (0,1,0) (1,1,0) (0,0,1) (1,0,1) (0,1,1) (1,1,1)`)}
          <p>For simultaneous supercover contacts, axis masks are <code>X=1</code>, <code>Y=2</code>, and <code>Z=4</code>. The provider visits applicable masks in ascending order from <code>1</code> through <code>7</code>. At this three-axis corner, that means X, Y, XY, Z, XZ, YZ, then XYZ relative to the current cell.</p>
          <p>Supercover’s closed-cell contact rule deliberately includes cells touched only on an edge or corner. It differs from half-open cell ownership used to map a point to one cell. Neither line API supplies an entry distance or tests geometry inside the selected cells.</p>`
      },
      {
        id: 'enumerate-regions',
        title: 'Enumerate regions',
        html: `<p>Region APIs use two different selection rules. A continuous AABB selects unit voxels that intersect its half-open volume. Spheres and cylinders select cells by their center. Use the rule that matches your operation; a cell can intersect a sphere while its center remains outside.</p>
          <p>Switch the region below to compare the exact shapes used in the Java example. The 3D display shows selected unit cells. Choose 2D slices to read each fixed-Z layer separately. A radius-one sphere selects seven cells; the half-open unit box selects one.</p>
          <div data-diagram="region-selection"></div>
          ${table(['Operation', 'Selection', 'Callback order'], [
            ['<code>AABBVoxelIterator.forEachCell(box, consumer)</code>', 'Cells intersecting a finite half-open AABB. Per-axis indices run from <code>floor(min)</code> through <code>floor(nextDown(max))</code>, inclusive. Empty boxes emit nothing.', 'Z outermost, then Y, with X fastest.'],
            ['<code>RegionIterators.forEachCellBox(x0,y0,z0,x1,y1,z1,consumer)</code>', 'Integer cell ranges including both ends. A reversed axis makes the region empty.', 'Z, Y, X; X fastest.'],
            ['<code>RegionIterators.forEachCellSphere(center,radius,consumer)</code>', 'Cell centers inside or on a sphere in unit-grid coordinates.', 'Z, Y, X; X fastest.'],
            ['<code>RegionIterators.forEachCellCylinderXZ(center,radius,y0,y1,consumer)</code>', 'Cell centers inside or on the XZ circle, repeated for each integer Y from <code>y0</code> through <code>y1</code>. The circle does not use <code>center.y()</code>.', 'Y, Z, X; X fastest.']
          ])}
          <p>This example contrasts an inclusive integer box ending at <code>(1,1,1)</code> with a continuous box ending at the same coordinates. The integer box contains eight cells. The half-open continuous box covers one cell.</p>
          ${code('java', 'VoxelRegionsExample.java', `import nsk.nu.ashcore.api.geometry.AxisAlignedBox;
import nsk.nu.ashcore.api.math.Vector3;
import nsk.nu.ashgrid.api.voxel.region.RegionIterators;
import nsk.nu.ashgrid.implementation.voxel.region.AABBVoxelIterator;

public class VoxelRegionsExample {
    public static void main(String[] args) {
        int[] counts = new int[5];
        RegionIterators.forEachCellBox(0, 0, 0, 1, 1, 1,
                (x, y, z) -> counts[0]++);
        new AABBVoxelIterator().forEachCell(new AxisAlignedBox(
                new Vector3(0, 0, 0), new Vector3(1, 1, 1)),
                (x, y, z) -> counts[1]++);
        Vector3 center = new Vector3(0.5, 0.5, 0.5);
        RegionIterators.forEachCellSphere(center, 1, (x, y, z) -> counts[2]++);
        RegionIterators.forEachCellCylinderXZ(center, 1, 0, 1,
                (x, y, z) -> counts[3]++);
        RegionIterators.forEachCellSphere(new Vector3(0, 0, 0), 0,
                (x, y, z) -> counts[4]++);
        System.out.println("inclusiveBox=" + counts[0]);
        System.out.println("halfOpenBox=" + counts[1]);
        System.out.println("sphere=" + counts[2]);
        System.out.println("cylinder=" + counts[3]);
        System.out.println("zeroRadiusAtCorner=" + counts[4]);
    }
}`)}
          ${code('output', 'Expected output', `inclusiveBox=8
halfOpenBox=1
sphere=7
cylinder=10
zeroRadiusAtCorner=0`)}
          <p>The sphere includes its center cell and six face neighbors at radius <code>1</code>. The cylinder selects five cells per XZ layer across two Y layers. A radius-zero sphere at a grid corner selects nothing because no cell center lies there. At a cell center, a radius-zero sphere selects that cell.</p>
          <p>Shape centers and radii must be finite, and the radius must be nonnegative. Candidate cell bounds must fit signed <code>int</code>. Region callbacks return <code>void</code> and provide no normal early-stop signal. Keep regions bounded: these synchronous loops do not create a snapshot, schedule work, or impose a cell budget.</p>`
      },
      {
        id: 'select-neighbors',
        title: 'Select neighbors',
        html: `<p><code>Neighborhood3D</code> provides integer offsets around a cell. Add each offset to the cell coordinates to obtain a neighbor. The arrays do not include the center cell.</p>
          <p>Choose a neighborhood to reveal its face, edge, and corner contacts across all three layers. The center is shown for orientation and is never counted as a neighbor.</p>
          <div data-diagram="neighborhoods"></div>
          ${table(['Array', 'Neighbors', 'Contacts included'], [
            ['<code>N6</code>', '6', 'Faces only. Order: +X, −X, +Y, −Y, +Z, −Z.'],
            ['<code>N18</code>', '18', 'Faces and edges; excludes the eight corners.'],
            ['<code>N26</code>', '26', 'Faces, edges, and corners.']
          ])}
          <p>For a face-connected search, inspect <code>N6</code>. To let edge or corner contacts connect cells, choose <code>N18</code> or <code>N26</code> as appropriate. Use the same choice consistently when your operation depends on connectivity.</p>
          ${code('java', 'VoxelNeighborsExample.java', `import java.util.ArrayList;
import java.util.List;
import nsk.nu.ashgrid.api.voxel.neighborhood.Neighborhood3D;

public class VoxelNeighborsExample {
    public static void main(String[] args) {
        int x = 10, y = 20, z = 30;
        List<String> neighbors = new ArrayList<>();
        for (int[] offset : Neighborhood3D.N6) {
            int nx = Math.addExact(x, offset[0]);
            int ny = Math.addExact(y, offset[1]);
            int nz = Math.addExact(z, offset[2]);
            neighbors.add("(" + nx + "," + ny + "," + nz + ")");
        }
        System.out.println("neighbors=" + String.join(" ", neighbors));
    }
}`)}
          ${code('output', 'Expected output', `neighbors=(11,20,30) (9,20,30) (10,21,30) (10,19,30) (10,20,31) (10,20,29)`)}
          <p>The example uses <code>Math.addExact</code> so coordinate overflow fails explicitly. Apply your storage bounds before reading or writing the resulting neighbors.</p>
          ${note('Treat neighborhood arrays as read-only', '<p>The public arrays and their rows are shared mutable objects retained for compatibility. Do not modify them. Mutation affects later morphology and component operations; concurrent mutation is unsupported. For custom offsets, deep-copy every row before editing. Cloning only the outer array still shares the rows.</p>')}`
      }
    ]
  });
})();

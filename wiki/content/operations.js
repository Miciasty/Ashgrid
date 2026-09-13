/* Ashgrid 1.3.0 operations. Sources are recorded in authoring/operations-sources.md. */
(() => {
  const { code, table, note } = window.WIKI_HTML;

  window.WIKI_PAGES.push({
    id: 'flood-fill',
    category: 'Raster operations',
    title: 'Flood fill',
    description: 'Visit the cells reachable from a seed through matching face neighbors.',
    kind: 'guide',
    readingTime: 5,
    intro: '<p>A flood fill starts at one cell and visits its connected region. Supply a predicate to select visitable values and a callback to process each visited cell. This can classify an air pocket or recolor a region in your own grid data.</p>',
    sections: [
      {
        id: 'fill-region',
        title: 'Fill a region bounded by a wall',
        html: `<p>This example stores a two-row region in a <code>5 × 2 × 1</code> grid. Value <code>9</code> forms a wall at <code>x = 2</code>. The fill visits zero-valued cells reachable from <code>(0, 0, 0)</code> and changes each visited cell to <code>7</code>.</p>
          <div data-diagram="flood-fill"></div>
          ${code('java', 'FloodFillExample.java', `import nsk.nu.ashgrid.implementation.raster.arrays.ArrayGrid3i;
import nsk.nu.ashgrid.implementation.voxel.ops.floodfill.FloodFillQueue;

public class FloodFillExample {
    public static void main(String[] args) {
        var grid = new ArrayGrid3i(5, 2, 1);
        grid.set(2, 0, 0, 9);
        grid.set(2, 1, 0, 9);

        int visited = new FloodFillQueue().fill(
            grid, 0, 0, 0,
            value -> value == 0,
            (x, y, z, value) -> grid.set(x, y, z, 7)
        );

        System.out.println("visited=" + visited
            + ", left=" + grid.get(1, 0, 0)
            + ", wall=" + grid.get(2, 0, 0)
            + ", right=" + grid.get(3, 0, 0));
    }
}`)}
          ${code('output', 'Expected output', 'visited=4, left=7, wall=9, right=0')}
          <p>The callback receives the value read before its own edit. The returned count includes successful visits, so it excludes the wall cells tested and rejected by the predicate.</p>`
      },
      {
        id: 'fill-contract',
        title: 'Bounds, connectivity, and callback ownership',
        html: `<p><code>FloodFill.fill(ReadableGrid3i, sx, sy, sz, canVisit, visit)</code> uses six face neighbors. The bundled <code>FloodFillQueue</code> performs breadth-first search, adding neighbors in <code>+X, −X, +Y, −Y, +Z, −Z</code> order. Edge or corner contact alone does not connect cells.</p>
          ${table(['Condition', 'Behavior'], [
            ['Seed outside the declared dimensions or rejected by <code>grid.inside</code>', 'Returns <code>0</code> without visiting a cell.'],
            ['Seed value rejected by <code>canVisit</code>', 'Returns <code>0</code>; no neighbors are explored.'],
            ['Clamped view', 'Search stays within <code>[0, width) × [0, height) × [0, depth)</code>. Clamping does not extend the search.'],
            ['Grid dimensions', 'Each dimension must be positive; the product must fit an <code>int</code>.'],
            ['Callback edits', 'The callback may edit its current cell. Keep unvisited cells, dimensions, and predicate behavior stable.']
          ])}
          <p>A predicate must give repeatable answers for unchanged values. Avoid changing neighboring cells from the callback: later visits read live grid values, and the task does not take a snapshot.</p>`
      },
      {
        id: 'fill-cost',
        title: 'Cost and incremental execution',
        html: `<p>The queue processes each candidate at most once and checks at most six neighbors after a successful visit. A rejected candidate still consumes work. The bundled implementation uses up to <code>O(volume)</code> auxiliary storage for visited bits and its primitive queue, even when the source uses sparse storage.</p>
          <p><code>FloodFill</code> exposes the synchronous call. For work spread across several calls, use <code>FloodFillQueue.begin</code> and a <code>FloodFillQueue.Workspace</code>. See <a href="#/stepped-tasks">Stepped tasks</a> for work budgets, cancellation, and workspace reuse.</p>`
      }
    ]
  });

  window.WIKI_PAGES.push({
    id: 'components',
    category: 'Raster operations',
    title: 'Connected components',
    description: 'Label every foreground region and choose which neighboring cells connect.',
    kind: 'guide',
    readingTime: 5,
    intro: '<p>Connected components gives every connected foreground region its own positive integer label. A predicate selects foreground values. Background receives label <code>0</code>, and the method returns the number of components.</p>',
    sections: [
      {
        id: 'component-neighborhoods',
        title: 'Choose what connects two cells',
        html: `<div data-diagram="components"></div>${table(['Neighborhood', 'Connections', 'Number of neighbors'], [
          ['<code>N6</code>', 'Shared faces', '6'],
          ['<code>N18</code>', 'Shared faces or edges', '18'],
          ['<code>N26</code>', 'Shared faces, edges, or corners', '26']
        ])}
          <p>The three foreground cells below touch successively through an edge and a corner. With <code>N6</code>, they form three components. Adding edges joins the first pair. Adding corners joins all three.</p>
          ${code('java', 'ComponentsExample.java', `import nsk.nu.ashgrid.api.voxel.ops.components.ConnectedComponents;
import nsk.nu.ashgrid.implementation.raster.arrays.ArrayGrid3i;
import nsk.nu.ashgrid.implementation.voxel.ops.components.ConnectedComponentsBFS;

public class ComponentsExample {
    public static void main(String[] args) {
        var source = new ArrayGrid3i(3, 3, 2);
        source.set(0, 0, 0, 7);
        source.set(1, 1, 0, 7);
        source.set(2, 2, 1, 7);
        var components = new ConnectedComponentsBFS();

        for (var neighborhood : ConnectedComponents.Neighborhood.values()) {
            var labels = new ArrayGrid3i(3, 3, 2);
            int count = components.label(
                source, value -> value == 7, labels, neighborhood
            );
            System.out.println(neighborhood + ": components=" + count
                + ", labels=" + labels.get(0, 0, 0)
                + "," + labels.get(1, 1, 0)
                + "," + labels.get(2, 2, 1));
        }
    }
}`)}
          ${code('output', 'Expected output', `N6: components=3, labels=1,2,3
N18: components=2, labels=1,1,2
N26: components=1, labels=1,1,1`)}
          <p>Labels begin at <code>1</code>. <code>ConnectedComponentsBFS</code> discovers new components by scanning X fastest, then Y, then Z. With unchanged input, predicate, and neighborhood arrays, this order determines the label numbers.</p>`
      },
      {
        id: 'component-buffers',
        title: 'Provide a separate output grid',
        html: `<p><code>label</code> requires source and output grids with equal positive dimensions and an <code>int</code>-sized volume. It clears the output before labeling, including cells that previously contained a label. Keep the source and predicate stable until the call completes.</p>
          ${note('Separate storage is required', '<p>Source and output must not share backing storage, including through views. The implementation rejects the same object passed twice, but it cannot detect every pair of views sharing a backing grid.</p>', true)}
          <p>Choose an output backend that preserves positive integer labels. <code>BitGrid3iView</code> accepts only <code>0</code> and <code>1</code>; it rejects the second component's label. Use an integer grid for component output.</p>
          <p>The public arrays in <code>Neighborhood3D</code> are shared by morphology and component labeling. Treat both the arrays and their rows as read-only. Mutating them changes subsequent operations; active tasks do not copy them.</p>`
      },
      {
        id: 'component-cost',
        title: 'Account for the whole volume',
        html: `<p>The bundled implementation clears and scans every cell, then dequeues each foreground cell once. For volume <code>V</code> and foreground count <code>F</code>, a completed task uses <code>2V + F</code> work units. Each dequeue checks at most 6, 18, or 26 neighbors. Sparse source storage does not remove the volume scan.</p>
          <p>The primitive queue retains capacity proportional to the largest component it has processed. Use <code>ConnectedComponentsBFS.begin</code> with its own <code>Workspace</code> for <a href="#/stepped-tasks">incremental execution</a>. A partial output or intermediate <code>count()</code> does not describe the final component set.</p>`
      }
    ]
  });

  window.WIKI_PAGES.push({
    id: 'morphology-distance',
    category: 'Raster operations',
    title: 'Morphology and distance',
    description: 'Expand or shrink binary regions, and measure 3-4-5 chamfer distance to foreground.',
    kind: 'guide',
    readingTime: 8,
    intro: '<p>Morphology changes the shape of a foreground region. A distance transform assigns each cell a cost to its nearest foreground cell. Both operate on your supplied grid or mask; the predicate determines which values count as foreground.</p>',
    sections: [
      {
        id: 'morphology-step',
        title: 'Expand and shrink a region',
        html: `<p><code>dilate</code> writes <code>1</code> when the source cell or any chosen neighbor is foreground. <code>erode</code> writes <code>1</code> only when the source cell and every chosen neighbor are foreground. All other output cells receive <code>0</code>.</p>
          <p>Choose <code>Morphology.Neighborhood.N6</code>, <code>N18</code>, or <code>N26</code> for faces, faces plus edges, or all adjacent cells. A single interior seed therefore expands to 7, 19, or 27 cells, including itself.</p>
          <p>The diagram starts with the same center cell and value <code>7</code> as the Java example. Dilate produces seven foreground cells with <code>N6</code>. Select Close to apply dilation followed by erosion: the intermediate has seven cells and the result retains only the center. The source stays unchanged. The larger tunnel preset is a separate example for inspecting interior and border behavior.</p>
          <div data-diagram="morphology"></div>
          ${code('java', 'MorphologyExample.java', `import nsk.nu.ashgrid.api.voxel.ops.morphology.Morphology;
import nsk.nu.ashgrid.implementation.raster.arrays.ArrayGrid3i;
import nsk.nu.ashgrid.implementation.voxel.ops.morphology.MorphologyBasic;

public class MorphologyExample {
    public static void main(String[] args) {
        var source = new ArrayGrid3i(5, 5, 5);
        var expanded = new ArrayGrid3i(5, 5, 5);
        var restored = new ArrayGrid3i(5, 5, 5);
        source.set(2, 2, 2, 7);
        var morphology = new MorphologyBasic();

        morphology.dilate(source, value -> value == 7,
            expanded, Morphology.Neighborhood.N6);
        morphology.erode(expanded, value -> value != 0,
            restored, Morphology.Neighborhood.N6);

        int expandedCells = 0;
        int restoredCells = 0;
        for (int z = 0; z < 5; z++) {
            for (int y = 0; y < 5; y++) {
                for (int x = 0; x < 5; x++) {
                    expandedCells += expanded.get(x, y, z);
                    restoredCells += restored.get(x, y, z);
                }
            }
        }
        System.out.println("expanded=" + expandedCells
            + ", restored=" + restoredCells
            + ", center=" + restored.get(2, 2, 2));
    }
}`)}
          ${code('output', 'Expected output', 'expanded=7, restored=1, center=1')}
          <p>The first predicate selects the original value <code>7</code>. The second selects the binary output. Erosion treats neighbors outside <code>src.inside</code> as background. For an ordinary bounded grid, this removes exposed border cells; a grid view can change that boundary behavior.</p>`
      },
      {
        id: 'morphology-buffers',
        title: 'Compose morphology with separate buffers',
        html: `<p>Provide positive source dimensions whose volume fits an <code>int</code>. The output must accept every source coordinate; a readable output is checked for the same dimensions. Source and output must use disjoint storage, including views. Direct object aliases are rejected, but shared backing storage is the caller's responsibility.</p>
          ${table(['<code>MorphologyOps</code> helper', 'Operation'], [
            ['<code>open(m, src, fg, tmp, dst, nh)</code>', 'Erode the source into <code>tmp</code>, then dilate into <code>dst</code>.'],
            ['<code>close(m, src, fg, tmp, dst, nh)</code>', 'Dilate the source into <code>tmp</code>, then erode into <code>dst</code>.'],
            ['<code>dilateN(m, src, fg, tmp, dst, nh, n)</code>', 'Perform <code>n</code> dilations; the final result is in <code>dst</code>.'],
            ['<code>erodeN(m, src, fg, tmp, dst, nh, n)</code>', 'Perform <code>n</code> erosions; the final result is in <code>dst</code>.']
          ])}
          <p>All three buffers must have the same shape and separate storage. The helpers apply <code>fg</code> to the original source only, then classify intermediate binary masks with <code>value != 0</code>. Negative <code>n</code> is rejected. With <code>n = 0</code>, the helper copies the original values into <code>dst</code> without converting them to a binary mask.</p>
          <p>Keep the source, predicate, and shared neighborhood arrays stable. Each <code>MorphologyBasic</code> pass writes the full volume in X-fastest order and checks at most 26 neighbors per cell. It needs no volume-sized scratch buffer beyond the supplied output. Composed operations use your temporary grid and may need an extra copy to leave the result in <code>dst</code>.</p>`
      },
      {
        id: 'chamfer-distance',
        title: 'Read distance in raw chamfer units',
        html: `<p><code>Chamfer345Distance</code> returns the minimum cost of a path to a foreground cell. A face step costs <code>3</code>, an edge step <code>4</code>, and a corner step <code>5</code>. Foreground itself has distance <code>0</code>.</p>
          <div data-diagram="distance-map"></div>
          ${code('java', 'ChamferDistanceExample.java', `import nsk.nu.ashgrid.implementation.voxel.ops.distance.Chamfer345Distance;

public class ChamferDistanceExample {
    public static void main(String[] args) {
        int width = 3;
        int height = 3;
        int depth = 3;
        float[] distances = new float[width * height * depth];
        var transform = new Chamfer345Distance();
        transform.compute(width, height, depth,
            (x, y, z) -> x == 0 && y == 0 && z == 0,
            distances);

        System.out.println("foreground=" + distances[0]
            + ", face=" + distances[1]
            + ", edge=" + distances[width + 1]
            + ", corner=" + distances[width * height + width + 1]);

        transform.compute(width, height, depth,
            (x, y, z) -> false, distances);
        System.out.println("emptyMaskDistance=" + distances[0]);
    }
}`)}
          ${code('output', 'Expected output', `foreground=0.0, face=3.0, edge=4.0, corner=5.0
emptyMaskDistance=Infinity`)}
          <p>The output uses index <code>(z * height + y) * width + x</code>. Its length must equal the positive, <code>int</code>-sized volume exactly. Keep the source mask stable and independent of the output array while the operation runs.</p>
          ${note('Choose the foreground to match the measurement', '<p>If foreground represents obstacles, zero marks obstacle cells and positive values measure cost to them. Dividing by <code>3</code> gives an approximation in cell units. It is not exact Euclidean distance or a guarantee that a character can fit through a passage.</p>')}
          <p>If the mask contains no foreground, every result is <code>Float.POSITIVE_INFINITY</code>. Large path costs follow <code>float</code> precision. The transform initializes the full output, then runs forward and backward passes: <code>3 × volume</code> work units with no volume-sized scratch buffer.</p>
          <p>Use <code>MorphologyBasic.beginDilate</code>, <code>beginErode</code>, or <code>Chamfer345Distance.begin</code> to <a href="#/stepped-tasks">pause between work steps</a>.</p>`
      }
    ]
  });

  window.WIKI_PAGES.push({
    id: 'stepped-tasks',
    category: 'Raster operations',
    title: 'Stepped tasks',
    description: 'Budget algorithm work, resume the same task, and manage partial results and reusable workspaces.',
    kind: 'guide',
    readingTime: 7,
    intro: '<p>A <code>VoxelTask</code> is a synchronous work cursor. Call <code>step(maxWork)</code> to perform a bounded number of algorithm units, then resume the same task later. Your application chooses when to call it; Ashgrid does not supply a scheduler.</p>',
    sections: [
      {
        id: 'create-task',
        title: 'Use a concrete implementation to begin work',
        html: `<p>The operation interfaces expose synchronous methods for use through SPI. Their <code>begin</code> methods belong to the bundled concrete implementations. Construct the required implementation when your application needs its stepped API.</p>
          <p>The static <code>GridOps.beginFill</code> helper also returns a <code>VoxelTask</code>. Its one-write-per-unit rule makes the shared stepping and cancellation contract easy to inspect:</p>
          <div data-diagram="work-budget"></div>
          ${table(['Implementation', 'Task factory', 'Result'], [
            ['<code>FloodFillQueue</code>', '<code>begin(grid, sx, sy, sz, predicate, callback[, workspace])</code>', '<code>FloodFillQueue.Task</code> with <code>count()</code>'],
            ['<code>ConnectedComponentsBFS</code>', '<code>begin(src, predicate, labels, neighborhood[, workspace])</code>', '<code>ConnectedComponentsBFS.Task</code> with <code>count()</code>'],
            ['<code>MorphologyBasic</code>', '<code>beginDilate(src, predicate, dst, neighborhood)</code> or <code>beginErode(...)</code>', '<code>VoxelTask</code>; output is in <code>dst</code>'],
            ['<code>Chamfer345Distance</code>', '<code>begin(w, h, d, mask, out)</code>', '<code>VoxelTask</code>; output is in <code>out</code>']
          ])}
          <p>Keep task inputs stable between calls as well as during each call. For output-producing operations, reserve the destination for that task until it finishes or is cancelled. Flood fill permits its callback to edit only the current cell.</p>`
      },
      {
        id: 'resume-example',
        title: 'Resume a fill and reuse its workspace',
        html: `<p>The first fill processes two candidates per call. The second reuses the released workspace, then cancels after one visit. The final line shows that cancellation retains the first edit and leaves the other cell unchanged.</p>
          ${code('java', 'SteppedTasksExample.java', `import nsk.nu.ashgrid.implementation.raster.arrays.ArrayGrid3i;
import nsk.nu.ashgrid.implementation.voxel.ops.floodfill.FloodFillQueue;

public class SteppedTasksExample {
    public static void main(String[] args) {
        var fill = new FloodFillQueue();
        var workspace = new FloodFillQueue.Workspace();
        var grid = new ArrayGrid3i(3, 2, 1);

        try (var task = fill.begin(grid, 0, 0, 0,
                value -> value == 0,
                (x, y, z, value) -> grid.set(x, y, z, 7),
                workspace)) {
            task.step(2);
            System.out.println("status=" + task.status()
                + ", visited=" + task.count()
                + ", work=" + task.workDone());
            while (!task.isDone()) {
                task.step(2);
            }
            System.out.println("status=" + task.status()
                + ", visited=" + task.count()
                + ", work=" + task.workDone());
        }

        var nextGrid = new ArrayGrid3i(2, 1, 1);
        try (var task = fill.begin(nextGrid, 0, 0, 0,
                value -> value == 0,
                (x, y, z, value) -> nextGrid.set(x, y, z, 7),
                workspace)) {
            task.step(1);
            task.cancel();
            System.out.println("status=" + task.status()
                + ", visited=" + task.count()
                + ", first=" + nextGrid.get(0, 0, 0)
                + ", second=" + nextGrid.get(1, 0, 0));
        }
    }
}`)}
          ${code('output', 'Expected output', `status=RUNNING, visited=2, work=2
status=COMPLETED, visited=6, work=6
status=CANCELLED, visited=1, first=7, second=0`)}
          <p>The loop runs synchronously to demonstrate resumption. To distribute work over application updates, retain the task and call one step from your own update mechanism.</p>`
      },
      {
        id: 'task-budgets',
        title: 'Budget work units, not elapsed time',
        html: `<p><code>step(maxWork)</code> returns the number of units performed, from zero through <code>maxWork</code>. <code>step(0)</code> performs no work; a negative budget throws <code>IllegalArgumentException</code>. <code>workDone()</code> accumulates completed units. A valid step on a terminal task returns zero.</p>
          ${table(['Operation', 'One work unit', 'Completed work'], [
            ['<code>GridOps</code> fill', 'Write one cell in the selected region.', 'One unit per region cell.'],
            ['Flood fill', 'Dequeue one candidate and, if accepted, check at most six neighbors.', 'One unit per dequeued candidate, including rejected candidates.'],
            ['Components', 'Clear one cell, scan one cell, or dequeue one foreground cell and check its neighbors.', '<code>2 × volume + foreground cell count</code> units.'],
            ['Morphology', 'Write one output cell after checking its source and at most 26 neighbors.', '<code>volume</code> units per pass.'],
            ['Chamfer distance', 'Initialize or relax one cell; a relaxation checks at most 13 neighbors.', '<code>3 × volume</code> units.']
          ])}
          ${note('A unit is not a millisecond', '<p>Budgets do not bound elapsed time, allocations, or work performed inside your predicates and callbacks. Workspace clearing at task creation and buffer growth are not separately counted as cell work. Choose a budget using measurements from your own data and callbacks.</p>')}
          <p><code>runToCompletion()</code> continues synchronously until the task reaches a terminal state. It does not restart a cancelled or failed task. Pause by keeping the task without calling <code>step</code>; do not close a task you intend to resume.</p>`
      },
      {
        id: 'task-results',
        title: 'Check status before consuming a final result',
        html: `${table(['Status', 'Meaning'], [
          ['<code>RUNNING</code>', 'Work remains. Counts and output are partial.'],
          ['<code>COMPLETED</code>', 'All work finished. The count and output are final.'],
          ['<code>CANCELLED</code>', 'The caller cancelled or closed unfinished work. Existing edits remain; the task cannot resume.'],
          ['<code>FAILED</code>', 'A runtime exception or error escaped a work unit. The task releases its workspace and rethrows the failure; partial output remains.']
        ])}
          <p><code>isDone()</code> means the status is no longer <code>RUNNING</code>; it does not mean success. Check for <code>COMPLETED</code> before using labels or distances as final results. <code>count()</code> reports successful flood visits or components discovered so far.</p>
          <p><code>cancel()</code> and <code>close()</code> do not roll back output. Completed and cancelled tasks stay terminal. Calls to <code>step</code> or <code>cancel</code> from inside the task's own callback are rejected to prevent reentry.</p>`
      },
      {
        id: 'task-workspaces',
        title: 'Release and reuse workspaces',
        html: `<p><code>FloodFillQueue.Workspace</code> retains a primitive queue and visited bits, up to <code>O(volume)</code> capacity. <code>ConnectedComponentsBFS.Workspace</code> retains a primitive queue sized for the largest component processed. Reuse them across sequential tasks to reuse their buffers.</p>
          <p>Each workspace permits one active task and is not thread-safe. Beginning another task with a busy workspace throws <code>IllegalStateException</code>. Completion, cancellation, closure of unfinished work, or failure during a step releases the workspace automatically. An old terminal task does not interfere with a later task that uses the same workspace.</p>
          <p>Morphology and chamfer tasks need no reusable volume-sized workspace. Their destinations remain caller-owned, and the caller may reuse them after completion or cancellation. All <code>VoxelTask</code> instances are single-threaded: serialize access to each task and its data.</p>`
      }
    ]
  });
})();

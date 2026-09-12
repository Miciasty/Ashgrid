(() => {
  const {code, table, note} = window.WIKI_HTML;
  const card = (id, label, title, description) => `<a class="link-card block rounded-[6px] border border-line p-[18px] [background:linear-gradient(145deg,var(--surface),transparent)] transition-[border-color] duration-150 hover:border-accent" href="#/${id}"><span>${label} <b>↗</b></span><h3>${title}</h3><p>${description}</p></a>`;
  window.WIKI_PAGES.push(
    {
      id: 'overview', category: 'Getting started', title: 'Ashgrid documentation', navTitle: 'Overview', kind: 'guide', readingTime: 3,
      description: 'Store voxel data, trace through cells, and process connected regions in Java.',
      intro: '<p>Ashgrid provides integer grids, cell and chunk indexing, voxel queries, and raster operations. Use it inside a Minecraft plugin or another Java application that works with three-dimensional cell data.</p>',
      sections: [
        {id: 'start-building', title: 'Start with a working grid', html: '<p>This WIKI documents <strong>Ashgrid 1.3.0</strong>. It requires <strong>Java 21 or newer</strong> and resolves <strong>Ashcore 1.2.0</strong> as a dependency.</p>'+`<div class="link-cards my-[22px] grid grid-cols-2 gap-[13px] max-[680px]:grid-cols-1">${card('installation','01 · SETUP','Add Ashgrid','Configure Maven or Gradle and package the library with your plugin.')}${card('quick-start','02 · FIRST RESULT','Select a voxel','Create a small grid and find the first occupied cell on a ray.')}${card('storage','03 · DATA','Choose storage','Compare dense arrays, bits, sparse maps, and chunked storage.')}${card('flood-fill','04 · EDITING','Fill a region','Visit connected cells and control how their values change.')}</div>`},
        {id: 'choose-an-operation', title: 'Choose an operation', html: table(['Your task','Read','What you get'],[
          ['Map a position or split it into a chunk and local cell','<a href="#/coordinates">Coordinates and bounds</a>','Integer indices with floor-based negative-coordinate behavior.'],
          ['Find the first occupied cell along a direction','<a href="#/raycasting">Raycast and line of sight</a>','A cell coordinate and its entry parameter, or no hit.'],
          ['Visit a line, sphere, or box','<a href="#/lines-regions">Lines and regions</a>','Cells selected by the chosen rasterization or shape rule.'],
          ['Find separate islands in a mask','<a href="#/components">Connected components</a>','Labels and a component count for a bounded volume.'],
          ['Grow a mask or estimate distance from it','<a href="#/morphology-distance">Morphology and distance</a>','A binary mask or a chamfer cost map.'],
          ['Spread an operation over several updates','<a href="#/stepped-tasks">Stepped tasks</a>','An operation you resume with an explicit work budget.']
        ])},
        {id: 'minecraft-integration', title: 'Use Ashgrid inside your plugin', html: '<p>Your plugin supplies the data: positions, cell values, predicates, and callbacks. Ashgrid does not read a Minecraft world or register commands, permissions, events, or configuration files. There is no server entry point to install in the <code>plugins</code> directory.</p><p>For block targeting, pass eye coordinates and a direction in the same grid units as your occupancy callback. A hit identifies an occupied callback cell and its traversal interval. It does not resolve a slab, stair, entity hitbox, or other detailed collision shape inside that cell.</p><p>Core grids are mutable and are not thread-safe. Keep data stable while querying it. If your plugin needs a detached volume, use <a href="#/grid-operations">GridOps.snapshot</a> and arrange access to the source in the appropriate execution context for your server.</p>'},
        {id: 'terms', title: 'Terms used in this WIKI', html: table(['Term','Meaning'],[
          ['Voxel / cell','One integer coordinate (x, y, z) in a three-dimensional grid. A unit cell spans [x, x+1), [y, y+1), [z, z+1).'],
          ['Occupancy predicate','Your rule for whether a cell counts as solid or selected. An integer value alone does not imply a Minecraft block type.'],
          ['Half-open bounds','A range that includes its minimum and excludes its maximum: [min, max).'],
          ['View','A wrapper that reads or writes the same backing storage with different access rules. It remains live.'],
          ['Snapshot','An independent, mutable dense copy made by GridOps.snapshot. Later changes to the source do not change it.'],
          ['Neighborhood','The offsets that define adjacency. N6 uses face neighbors; N18 adds edges; N26 also adds corners.'],
          ['Work budget','A limit on the operation-specific units consumed by a task step. It is not a duration in milliseconds.']
        ])},
        {id: 'version', title: 'Version and related libraries', html: '<p>Maven coordinates use <code>dev.nasaka.blackframe:ashgrid:1.3.0</code>; imports use <code>nsk.nu.ashgrid</code>. Read <a href="#/migration">migration notes</a> before upgrading an existing consumer.</p><p>Ashcore supplies math, rays, and service discovery. Ashspace handles coordinate frames; Ashnav handles higher-level navigation. Ashgrid itself does not render meshes, apply world edits, or choose paths.</p><p>Browse the <a href="https://github.com/Miciasty/Ashgrid">source repository</a> or the <a href="https://central.sonatype.com/artifact/dev.nasaka.blackframe/ashgrid/1.3.0">versioned Maven artifact</a>. The library is distributed under the <a href="https://github.com/Miciasty/Ashgrid/blob/master/LICENSE">Apache License 2.0</a>.</p>'}
      ]
    },
    {
      id: 'installation', category: 'Getting started', title: 'Add Ashgrid to your project', navTitle: 'Installation', kind: 'guide', readingTime: 5,
      description: 'Add the Java dependency and keep its classes and service providers available at runtime.',
      sections: [
        {id: 'requirements', title: 'Requirements', html: table(['Requirement','Value'],[
          ['Compile and run','JDK 21 or newer.'],['Artifact','<code>dev.nasaka.blackframe:ashgrid:1.3.0</code>'],['Repository','Maven Central.'],['Transitive dependency','<code>dev.nasaka.blackframe:ashcore:1.2.0</code>'],['Source build','Maven 3.9+ and JDK 21+.']
        ])+'<p>Ashgrid has no dependency on Bukkit or Paper. Choose server compatibility in the plugin that embeds it. Do not force an older Ashcore version onto the runtime classpath.</p>'},
        {id: 'maven', title: 'Maven', html: '<p>Add this dependency to your existing <code>&lt;dependencies&gt;</code> element. Maven Central needs no additional repository declaration.</p>'+code('xml','pom.xml — dependency',`<dependency>
  <groupId>dev.nasaka.blackframe</groupId>
  <artifactId>ashgrid</artifactId>
  <version>1.3.0</version>
</dependency>`)+ '<p>Set your compiler release to 21 or newer. The <a href="#/quick-start">quick start</a> includes a complete consumer POM.</p>'},
        {id: 'gradle', title: 'Gradle', html: '<p>In a Gradle Java project, use Maven Central and a Java 21 toolchain. The dependency also brings in Ashcore.</p>'+code('kotlin','build.gradle.kts',`plugins {
    java
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("dev.nasaka.blackframe:ashgrid:1.3.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}`)},
        {id: 'package-with-plugin', title: 'Package the library with your plugin', html: '<p>A compile-time dependency must also be available when your plugin runs. For a private bundled copy, Maven Shade can include Ashgrid and Ashcore and relocate their packages. This example belongs inside the consumer POM’s <code>&lt;build&gt;&lt;plugins&gt;</code> element.</p><p>Replace <code>com.example.myplugin.libs</code> with a package owned by your plugin. Keep service descriptors when bundling: <code>ServiceRegistry</code> discovers Ashgrid providers through <code>META-INF/services</code>.</p>'+code('xml','pom.xml — bundled libraries',`<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-shade-plugin</artifactId>
  <version>3.6.2</version>
  <executions>
    <execution>
      <phase>package</phase>
      <goals><goal>shade</goal></goals>
      <configuration>
        <minimizeJar>false</minimizeJar>
        <relocations>
          <relocation>
            <pattern>nsk.nu.ashgrid</pattern>
            <shadedPattern>com.example.myplugin.libs.ashgrid</shadedPattern>
          </relocation>
          <relocation>
            <pattern>nsk.nu.ashcore</pattern>
            <shadedPattern>com.example.myplugin.libs.ashcore</shadedPattern>
          </relocation>
        </relocations>
        <transformers>
          <transformer implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer"/>
        </transformers>
      </configuration>
    </execution>
  </executions>
</plugin>`)+ '<p>The <a href="https://maven.apache.org/plugins/maven-shade-plugin/examples/resource-transformers.html#ServicesResourceTransformer">ServicesResourceTransformer</a> merges service descriptors and updates provider names after relocation. Run the <a href="#/services?section=verify-discovery">discovery example</a> against your final artifact to check packaging.</p>'+note('Keep shared API types compatible','<p>If plugins exchange Ashgrid or Ashcore objects, a relocated private copy changes their class identity. Decide which plugin supplies the shared API before choosing relocation.</p>')},
        {id: 'build-source', title: 'Build from source', html: '<p>From the Ashgrid repository root, run:</p>'+code('bash','Terminal',`mvn -B clean verify`)+ '<p>This compiles the library and runs its Maven verification lifecycle. The generated library, sources, and Javadoc artifacts appear under <code>target</code>. To make the current checkout available to another local Maven project, run:</p>'+code('bash','Terminal',`mvn -B install`)}
      ]
    },
    {
      id: 'quick-start', category: 'Getting started', title: 'Find the first occupied voxel', navTitle: 'Quick start', kind: 'guide', readingTime: 4,
      description: 'Build a small grid and query it with a ray in a standalone Java program.',
      sections: [
        {id: 'create-project', title: 'Create a consumer project', html: '<p>Create a directory with the following <code>pom.xml</code>. Use JDK 21 or newer. This example runs without a Minecraft server.</p>'+code('xml','pom.xml',`<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.example</groupId>
  <artifactId>ashgrid-example</artifactId>
  <version>1.0.0</version>
  <properties>
    <maven.compiler.release>21</maven.compiler.release>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  </properties>
  <dependencies>
    <dependency>
      <groupId>dev.nasaka.blackframe</groupId>
      <artifactId>ashgrid</artifactId>
      <version>1.3.0</version>
    </dependency>
  </dependencies>
  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.13.0</version>
      </plugin>
      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>exec-maven-plugin</artifactId>
        <version>3.5.0</version>
        <configuration><mainClass>AshgridQuickStart</mainClass></configuration>
      </plugin>
    </plugins>
  </build>
</project>`)},
        {id: 'create-grid', title: 'Create the grid and ray', html: '<p>Save this class as <code>src/main/java/AshgridQuickStart.java</code>. The grid contains eight cells along X. Only cell (3, 1, 0) is occupied.</p>'+code('java','AshgridQuickStart.java',`import nsk.nu.ashcore.api.geometry.Ray;
import nsk.nu.ashcore.api.math.Vector3;
import nsk.nu.ashcore.api.spi.ServiceRegistry;
import nsk.nu.ashgrid.api.voxel.query.Raycast;
import nsk.nu.ashgrid.api.voxel.traversal.VoxelTraverser;
import nsk.nu.ashgrid.implementation.raster.arrays.ArrayGrid3i;

public final class AshgridQuickStart {
    public static void main(String[] args) {
        var grid = new ArrayGrid3i(8, 3, 1);
        grid.set(3, 1, 0, 1);
        var traverser = ServiceRegistry.of(VoxelTraverser.class).require("dda");
        var raycast = new Raycast(traverser);
        var ray = new Ray(new Vector3(0.5, 1.5, 0.5), new Vector3(1, 0, 0));

        var hit = raycast.first(ray, 7.5, (x, y, z) ->
            grid.inside(x, y, z) && grid.get(x, y, z) != 0);

        if (hit == null) throw new IllegalStateException("Expected a voxel hit");
        System.out.println("cell=" + hit.x() + "," + hit.y() + "," + hit.z());
        System.out.println("tEnter=" + hit.tEnter());
    }
}`)+code('output','Expected output',`cell=3,1,0
tEnter=2.5`)+ '<p>The occupancy callback checks bounds before reading the dense grid. The ray enters cell (3, 1, 0) at X=3, which is 2.5 grid units from X=0.5. The ray’s Y and Z coordinates remain unchanged.</p>'},
        {id: 'run-example', title: 'Run the program', html: '<p>In the directory containing your POM, run:</p>'+code('bash','Terminal',`mvn -q compile exec:java`)+ '<p>The program prints the cell and entry parameter shown above. The first call to <code>ServiceRegistry.of</code> loads the provider descriptors from the classpath.</p>'},
        {id: 'adapt-example', title: 'Use your own data', html: '<p>Replace the stored integers or the occupancy callback with your application’s cell data. Keep the ray origin, distance limit, and callback coordinates in the same grid space. With one cell per block, a distance of 2.5 grid units means 2.5 blocks.</p><p>Ashcore normalizes the ray direction. <code>tEnter</code> therefore measures distance in the supplied coordinate units. <code>Raycast.first</code> returns <code>null</code> when it finds no occupied cell before the excluded endpoint. It includes the starting cell and zero-length DDA boundary visits.</p><p>Continue with <a href="#/coordinates">coordinates</a> for scaled cells, <a href="#/raycasting">raycast contracts</a> for boundary behavior, or <a href="#/views">views</a> to expose a bounded window of sparse storage.</p>'}
      ]
    }
  );
})();

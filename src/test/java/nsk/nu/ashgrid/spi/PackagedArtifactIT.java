package nsk.nu.ashgrid.spi;

import nsk.nu.ashcore.api.geometry.Ray;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.ToolProvider;
import java.io.DataInputStream;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class PackagedArtifactIT {
    private final Path build = Path.of(System.getProperty("ashgrid.buildDirectory"));
    private final String finalName = System.getProperty("ashgrid.finalName");
    private static final String ROOT = "nsk.nu.ashgrid.";
    private static final Map<String,String> SERVICES = Map.of(
            "traversal.VoxelTraverser", "dda",
            "draw.Line3D", "bresenham3d",
            "draw.Line3DSupercover", "supercover3d",
            "ops.floodfill.FloodFill", "floodfill-queue",
            "ops.components.ConnectedComponents", "ConnectedComponentsBFS",
            "ops.morphology.Morphology", "MorphologyBasic",
            "ops.distance.DistanceTransform", "Chamfer345Distance");

    @TempDir Path temp;

    @Test
    void artifacts_contain_java21_classes_sources_docs_and_exact_coordinates() throws Exception {
        // GIVEN / WHEN
        try (JarFile jar = new JarFile(mainJar().toFile());
             JarFile sources = new JarFile(build.resolve(finalName+"-sources.jar").toFile());
             JarFile docs = new JarFile(build.resolve(finalName+"-javadoc.jar").toFile());
             JarFile core = new JarFile(coreJar().toFile())) {
            // THEN
            String entry = "nsk/nu/ashgrid/api/voxel/traversal/VoxelTraverser";
            try (DataInputStream data = new DataInputStream(jar.getInputStream(jar.getJarEntry(entry+".class")))) {
                assertEquals(0xcafebabe,data.readInt()); data.readUnsignedShort();
                assertEquals(65,data.readUnsignedShort());
            }
            assertNotNull(sources.getJarEntry(entry+".java"));
            assertNotNull(docs.getJarEntry(entry+".html"));
            assertNotNull(docs.getJarEntry("index.html"));
            assertFalse(jar.stream().anyMatch(e -> e.getName().startsWith("org/junit/") || e.getName().contains("Test.class")));
            assertEquals(System.getProperty("ashgrid.version"),coordinates(jar,"ashgrid").getProperty("version"));
            assertEquals(System.getProperty("ashgrid.ashcoreVersion"),coordinates(core,"ashcore").getProperty("version"));
            for (String service : SERVICES.keySet()) {
                String resource = "META-INF/services/"+ROOT+"api.voxel."+service;
                assertNotNull(jar.getJarEntry(resource),resource);
            }
        }
    }

    @Test
    void readme_example_compiles_and_runs_with_only_packaged_dependencies() throws Exception {
        // GIVEN
        String readme = Files.readString(Path.of(System.getProperty("ashgrid.basedir"),"README.md"));
        var example = Pattern.compile("```java\\s*\\R(.*?)```",Pattern.DOTALL).matcher(readme);
        assertTrue(example.find());
        Path source = temp.resolve("AshgridQuickStart.java"); Files.writeString(source,example.group(1));
        compile(source);
        ClassLoader previous = Thread.currentThread().getContextClassLoader();
        // WHEN / THEN
        try (URLClassLoader loader = loader(temp)) {
            Thread.currentThread().setContextClassLoader(loader);
            loader.loadClass("AshgridQuickStart").getMethod("main",String[].class).invoke(null,(Object)new String[0]);
        } finally { Thread.currentThread().setContextClassLoader(previous); }
    }

    @Test
    void every_advertised_provider_is_loaded_from_the_main_jar_by_exact_id() throws Exception {
        // GIVEN / WHEN / THEN
        try (URLClassLoader loader = loader(temp)) {
            Class<?> registry = loader.loadClass("nsk.nu.ashcore.api.spi.ServiceRegistry");
            for (var entry : SERVICES.entrySet()) {
                Class<?> service = loader.loadClass(ROOT+"api.voxel."+entry.getKey());
                Object loaded = registry.getMethod("of",Class.class,ClassLoader.class).invoke(null,service,loader);
                assertEquals(1,registry.getMethod("size").invoke(loaded));
                Object provider = registry.getMethod("require",String.class).invoke(loaded,entry.getValue());
                assertEquals(mainJar().toUri().toURL(),provider.getClass().getProtectionDomain().getCodeSource().getLocation());
            }
        }
    }

    @Test
    void missing_and_duplicate_ids_fail_predictably_from_packaged_resources() throws Exception {
        // GIVEN
        Path source = temp.resolve("DuplicateDDA.java");
        Files.writeString(source,"""
                import nsk.nu.ashcore.api.geometry.Ray;
                import nsk.nu.ashgrid.api.voxel.traversal.*;
                public final class DuplicateDDA implements VoxelTraverser {
                    public String id() { return "dda"; }
                    public void traverse(Ray ray,double max,CellVisitor visitor) {}
                }
                """);
        compile(source);
        Path fixture = temp.resolve("duplicate.jar");
        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(fixture))) {
            jar.putNextEntry(new JarEntry("DuplicateDDA.class"));
            Files.copy(temp.resolve("DuplicateDDA.class"),jar); jar.closeEntry();
            jar.putNextEntry(new JarEntry("META-INF/services/"+ROOT+"api.voxel.traversal.VoxelTraverser"));
            jar.write("DuplicateDDA\n".getBytes(StandardCharsets.UTF_8)); jar.closeEntry();
        }
        // WHEN / THEN
        try (URLClassLoader loader = loader(temp)) {
            Class<?> registry = loader.loadClass("nsk.nu.ashcore.api.spi.ServiceRegistry");
            Object loaded = registry.getMethod("of",Class.class,ClassLoader.class).invoke(null,
                    loader.loadClass(ROOT+"api.voxel.traversal.VoxelTraverser"),loader);
            InvocationTargetException failure = assertThrows(InvocationTargetException.class,
                    () -> registry.getMethod("require",String.class).invoke(loaded,"absent"));
            assertInstanceOf(IllegalStateException.class,failure.getCause());
        }
        try (URLClassLoader loader = loader(fixture)) {
            Class<?> registry = loader.loadClass("nsk.nu.ashcore.api.spi.ServiceRegistry");
            InvocationTargetException failure = assertThrows(InvocationTargetException.class,
                    () -> registry.getMethod("of",Class.class,ClassLoader.class).invoke(null,
                            loader.loadClass(ROOT+"api.voxel.traversal.VoxelTraverser"),loader));
            assertInstanceOf(IllegalStateException.class,failure.getCause());
        }
    }

    private void compile(Path source) throws Exception {
        assertNotNull(ToolProvider.getSystemJavaCompiler(),"verification requires a JDK");
        assertEquals(0,ToolProvider.getSystemJavaCompiler().run(null,null,null,"--release","21","-encoding","UTF-8",
                "-classpath",mainJar()+File.pathSeparator+coreJar(),"-d",temp.toString(),source.toString()));
    }

    private URLClassLoader loader(Path extra) throws Exception {
        return new URLClassLoader(new URL[]{mainJar().toUri().toURL(),coreJar().toUri().toURL(),extra.toUri().toURL()},
                ClassLoader.getPlatformClassLoader());
    }
    private Path mainJar() { return build.resolve(finalName+".jar"); }
    private static Path coreJar() throws Exception { return Path.of(Ray.class.getProtectionDomain().getCodeSource().getLocation().toURI()); }
    private static Properties coordinates(JarFile jar,String artifact) throws Exception {
        Properties p = new Properties();
        try (var in = jar.getInputStream(jar.getJarEntry("META-INF/maven/dev.nasaka.blackframe/"+artifact+"/pom.properties"))) { p.load(in); }
        return p;
    }
}

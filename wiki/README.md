# Ashgrid WIKI

English documentation for Ashgrid 1.3.0, using the shared Minecraft documentation template and the same visual system as Ashcore. The site has local search, keyboard navigation, dark/light themes, code copying, and local syntax highlighting. It loads no remote scripts, fonts, or analytics.

## Preview

Open `index.html` directly, or use Node.js 20+ to serve the authoring files:

```powershell
node preview.mjs
```

The preview listens on `http://127.0.0.1:4173`. Set `PORT` to choose a different port. Stop it with Ctrl+C. Content reloads when you refresh the browser.

## Build

From this directory:

```powershell
npm ci
npm run build
```

The build compiles Tailwind CSS, validates content and links, and stages browser files in `wiki/_site`. It includes `index.html`, `.nojekyll`, `assets`, `content`, and the library's `LICENSE` and `NOTICE`. Authoring notes, source styles, package files, and development tools are excluded. `_site` and `node_modules` are ignored by Git.

Run `npm run dev` for a CSS watcher and local preview. Edit `src/*.css`, the HTML shell, or complete utility class strings in the JavaScript files; `assets/styles.css` is generated.

## Verify Java examples

Install Apache Maven 3.9+ and JDK 21 or newer. Set `JAVA_HOME` when the default Java is older. From the repository root, resolve the versioned Ashcore dependency:

```powershell
mvn -B org.apache.maven.plugins:maven-dependency-plugin:3.8.1:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target/wiki-dependencies
```

Then, from `wiki`:

```powershell
npm run check:examples
```

The checker also looks in the current user's Maven repository. Set `ASHCORE_JAR` to an existing Ashcore 1.2.0 JAR to use another location. The checker copies the dependency into `.verification`, compiles the actual article examples with the current Ashgrid source using `--release 21`, copies the real service descriptors, and runs each program. Its output must match the adjacent expected-output block exactly. Every Java block must be a complete program with a unique public class and `main` method.

## Publish with GitHub Pages

The repository workflow is `.github/workflows/pages.yml` (**Ashgrid WIKI**). It builds the site and verifies examples on pull requests. It deploys only from `master`, on a matching push or a manual workflow run.

1. Push and merge the WIKI changes into `master`.
2. In the repository's **Settings → Pages → Build and deployment**, select **GitHub Actions** as the source.
3. Run **Actions → Ashgrid WIKI → Run workflow** on `master` if the initial push ran before Pages was configured.
4. Open the URL returned by the deployment job. With the repository's default Pages domain, it is `https://miciasty.github.io/Ashgrid/`.
5. Check navigation, search, examples, diagrams, and a narrow screen at that URL.

The workflow grants Pages write and OIDC permissions only to the deployment job. Pull requests build without deployment permissions. It uploads the generated `_site` directory. See [GitHub's custom-workflow guide](https://docs.github.com/en/pages/getting-started-with-github-pages/using-custom-workflows-with-github-pages).

Creating these local files does not enable Pages or publish a deployment. Repository settings and the deployed URL must be checked when releasing.

## Edit articles

`content/site.js` defines identity and navigation. `content/pages.js` defines reusable component helpers. Subject files append articles to `window.WIKI_PAGES`. Keep content scripts before `assets/app.js` in `index.html`.

Use `#/page-id` and `#/page-id?section=section-id` for links. Assets use relative paths so the site works under `/Ashgrid/`. Update the version in `content/site.js` with `pom.xml`; `npm run check` rejects mismatches, missing links, and demonstration content.

## Edit visual explanations

The articles contain 17 interactive figures. Put each figure beside the concept and code it explains, with its coordinate system, displayed slice, input values, and limits stated in the caption. Use 2D for state, ranges, queues, and slices; use 3D when depth or spatial contact matters. Prefer controls that expose a useful difference: a boundary crossing, a changed neighborhood, a live write, or a work step. Keep results readable as text as well as color. General operation, dependency, and service-lookup flows use the article's prose and tables.

`assets/visuals.js` supplies the shared frame, SVG matrix, and event cleanup helpers; `src/visuals.css` defines their presentation. The subject modules `assets/diagrams-grids.js`, `diagrams-queries.js`, `diagrams-operations.js`, and `diagrams-overview.js` register factories in `window.WikiDiagramFactories`. `assets/diagrams.js` mounts them together with the original coordinate and traversal figures and disposes their listeners when the article changes. The figures run local JavaScript models of finite examples; they do not execute Java or connect to Minecraft.

`assets/volume.js` supplies perspective SVG scenes for DDA ties, regions, neighborhoods, connected components, and morphology. Their shaded cells, ground grid, orientation key, view buttons, and camera controls follow Ashspace's 3D figures. Drag or use arrow keys to orbit; scroll, press + / −, or use the zoom buttons to zoom. Home and Reset view restore the camera. Camera events repaint the existing SVG from the retained cells without rerunning the algorithm. Both morphology panes share a camera. The scenes use Y up; the matching XY slices label Z and use Y down. No external renderer is loaded.

Every figure has Reset example, which restores its documented inputs, edits, progression, and camera. DDA ties, corner line coverage, region selection, connected components, and the default morphology mask use the nearby Java examples. Alternate presets are identified separately. Cleanup releases pointer capture, listeners, resize observers, cached scenes, and pending animation frames. On narrow screens, the shared shell moves Maven Central from the topbar into the navigation drawer.

Add a figure with `<div data-diagram="factory-id"></div>` in an article. Each factory accepts its host element and returns a cleanup function. Register new modules in `index.html` before `assets/app.js` and in the Tailwind scan sources in `src/input.css`. `npm run check` rejects unknown figure IDs. After a change, rebuild, exercise its controls in the browser, and compare algorithm outputs with the Java source. Use numeric tick offsets when the displayed region does not start at zero.

The evidence and authoring decisions are recorded in [authoring/SOURCES.md](authoring/SOURCES.md); validation results are in [authoring/VALIDATION.md](authoring/VALIDATION.md). Keep those notes out of the published site.

// Validate the Java programs and outputs that readers actually copy from the WIKI.
import {readFile, readdir, mkdir, mkdtemp, writeFile, cp, access} from 'node:fs/promises';
import {fileURLToPath} from 'node:url';
import path from 'node:path';
import os from 'node:os';
import vm from 'node:vm';
import {spawnSync} from 'node:child_process';

const root = fileURLToPath(new URL('.', import.meta.url));
const repository = path.resolve(root, '..');
const pom = await readFile(path.join(repository, 'pom.xml'), 'utf8');
const coreVersion = pom.match(/<ashcore.version>([^<]+)<\/ashcore.version>/)[1];
const jarName = `ashcore-${coreVersion}.jar`;
const candidates = process.env.ASHCORE_JAR ? [process.env.ASHCORE_JAR] : [
  path.join(repository, 'target/wiki-dependencies', jarName),
  path.join(os.homedir(), '.m2/repository/dev/nasaka/blackframe/ashcore', coreVersion, jarName)
];
let dependency;
for (const candidate of candidates) { try { await access(candidate); dependency = candidate; break; } catch {} }
if (!dependency) throw new Error('Ashcore dependency not found. Run the dependency command in wiki/README.md or set ASHCORE_JAR.');
const verification = path.join(repository, '.verification');
await mkdir(verification, {recursive:true});
const work = await mkdtemp(path.join(verification, 'wiki-examples-'));
const classes = path.join(work, 'classes');
await mkdir(classes);
const localJar = path.join(work, jarName);
await cp(dependency, localJar);

const context = vm.createContext({window:{}});
const shell = await readFile(path.join(root, 'index.html'), 'utf8');
for (const [,file] of shell.matchAll(/<script src="\.\/(content\/[^" ]+)"/g)) {
  vm.runInContext(await readFile(path.join(root,file),'utf8'),context,{filename:file,timeout:1000});
}
const decode = value => value.replace(/&(amp|lt|gt|quot|#39);/g,(_,entity)=>({amp:'&',lt:'<',gt:'>',quot:'"','#39':"'"}[entity]));
const examples = [];
for (const page of context.window.WIKI_PAGES) {
  for (const section of [{id:'intro', html:page.intro || ''}, ...page.sections]) {
    const blocks = [...section.html.matchAll(/<div[^>]*data-language="([^"]+)"[^>]*>[\s\S]*?<pre><code>([\s\S]*?)<\/code><\/pre>/g)];
    for (let i=0; i<blocks.length; i++) {
      if (blocks[i][1] !== 'java') continue;
      const source = decode(blocks[i][2]);
      const name = source.match(/public\s+(?:final\s+)?class\s+(\w+)/)?.[1];
      if (!name || !/static\s+void\s+main\s*\(/.test(source)) throw new Error(`${page.id}/${section.id}: Java example must be a complete runnable class.`);
      if (examples.some(example=>example.name===name)) throw new Error(`Duplicate example class ${name}`);
      const following = blocks[i+1];
      if (!following || !['output','stdout','text'].includes(following[1])) throw new Error(`${name}: missing expected output block.`);
      const file = path.join(work, `${name}.java`);
      await writeFile(file, source);
      examples.push({name, file, page:page.id, expected:decode(following[2]).trim().replaceAll('\r\n','\n')});
    }
  }
}
if (!examples.length) throw new Error('No Java examples found.');
async function javaFiles(directory) {
  const files=[];
  for (const entry of await readdir(directory,{withFileTypes:true})) {
    const file=path.join(directory,entry.name);
    if(entry.isDirectory()) files.push(...await javaFiles(file));
    else if(entry.name.endsWith('.java')) files.push(file);
  }
  return files;
}
const sources = [...await javaFiles(path.join(repository,'src/main/java')), ...examples.map(example=>example.file)];
const argsFile = path.join(work,'sources.txt');
await writeFile(argsFile,sources.map(file=>`"${file.replaceAll('\\','/')}"`).join('\n'));
const executable = name => process.env.JAVA_HOME ? path.join(process.env.JAVA_HOME,'bin',name+(process.platform==='win32'?'.exe':'')) : name;
function run(name,args) {
  const result=spawnSync(executable(name),args,{encoding:'utf8',windowsHide:true,timeout:60000,cwd:work});
  if(result.error || result.status!==0) throw new Error(`${name} failed:\n${result.error || ''}\n${result.stdout || ''}${result.stderr || ''}`);
  return result.stdout.trim().replaceAll('\r\n','\n');
}
run('javac',['--release','21','-encoding','UTF-8','-cp',localJar,'-d',classes,`@${argsFile}`]);
await cp(path.join(repository,'src/main/resources'),classes,{recursive:true});
for (const example of examples) {
  const actual=run('java',['-ea','-Duser.language=en','-Duser.country=US','-cp',classes+path.delimiter+localJar,example.name]);
  if(actual!==example.expected) throw new Error(`${example.page}/${example.name}: output mismatch\nExpected:\n${example.expected}\nActual:\n${actual}`);
  console.log(`PASS ${example.page}: ${example.name}`);
}
console.log(`Compiled Ashgrid source with Ashcore ${coreVersion}; ran ${examples.length} examples and matched every displayed result.`);

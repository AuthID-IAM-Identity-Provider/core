const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');
const crypto = require('crypto');

// Get Release Info from command-line arguments
const version = process.argv[2];
const notes = process.argv[3];
const commitsRaw = process.argv[4];

console.log(`Version: ${version}`);
console.log(`Notes: ${notes}`);
console.log(`Commits: ${commitsRaw}`);


if (!version || !notes || !commitsRaw) {
  console.error('Error: Release information was not provided via command-line arguments.');
  process.exit(1);
}

const commits = JSON.parse(commitsRaw);

// --- Helper function to run shell commands ---
function runCommand(command) {
  console.log(`> ${command}`);
  execSync(command, { stdio: 'inherit' });
}

// --- Path Definitions ---
const majorVersion = `v${version.split('.')[0]}`;
const changelogsDir = 'changelogs';
const mdDetailDir = path.join(changelogsDir, 'details');
const mdDetailFile = path.join(mdDetailDir, `${version}.md`);
const mdRootChangelog = 'CHANGELOG.md';
const jsonApiDir = path.join(changelogsDir, 'api', majorVersion);
const jsonDetailDir = path.join(jsonApiDir, 'releases');
const jsonDetailFile = path.join(jsonDetailDir, `${version}.json`);
const jsonIndexFile = path.join(jsonApiDir, 'index.json');

// --- Main Preparation Function ---
function prepareRelease() {
  console.log(`--- Preparing release for version ${version} ---`);

  // Step 1: Update pom.xml version
  console.log(`Updating pom.xml to version ${version}...`);
  runCommand(`mvn versions:set -DnewVersion=${version} -DprocessAllModules`);

  // Step 2: Generate all changelog files (JSON and Markdown)
  console.log('Generating changelog files...');
  generateAllChangelogs();

  // Step 3: Build the project and create the JAR
  console.log('Building project with Maven...');
  runCommand('mvn -B package -DskipTests');

  // Step 4: Prepare and name the release assets
  console.log('Preparing release assets...');
  prepareAssets();

  console.log('--- Release preparation complete ---');
}

// --- Changelog Generation Logic ---
function generateAllChangelogs() {
  const releaseJson = createReleaseJsonObject();
  generateMarkdownFiles(releaseJson);
  generateJsonApiFiles(releaseJson);
}

function createReleaseJsonObject() {
    const releaseData = { version, date: new Date().toISOString(), notes, commits: { features: [], fixes: [], performance: [], refactoring: [], tests: [], styles: [], docs: [], chores: [], breakingChanges: [] } };
    commits.forEach(commit => {
        const commitData = { commit: commit.hash.substring(0, 7), author: commit.author.name, date: commit.committerDate, message: commit.subject, scope: commit.scope || 'general', issue: (commit.message.match(/#(\d+)/) || [])[1] || null };
        const typeMap = { feat: 'features', fix: 'fixes', perf: 'performance', refactor: 'refactoring', test: 'tests', style: 'styles', docs: 'docs', chore: 'chores' };
        if (typeMap[commit.type]) releaseData.commits[typeMap[commit.type]].push(commitData);
        if (commit.notes.some(note => note.title.toUpperCase().includes('BREAKING CHANGE'))) releaseData.commits.breakingChanges.push(commitData);
    });
    return releaseData;
}

function generateMarkdownFiles(releaseJson) {
    fs.mkdirSync(mdDetailDir, { recursive: true });
    fs.writeFileSync(mdDetailFile, releaseJson.notes);
    if (!fs.existsSync(mdRootChangelog)) fs.writeFileSync(mdRootChangelog, `# Changelog\n\nAll notable changes to this project will be documented in this file.\n\n`);
    const newEntry = `* [**${releaseJson.version}**](./${mdDetailFile}) - ${new Date().toISOString().split('T')[0]}`;
    const header = fs.readFileSync(mdRootChangelog, 'utf-8').split('\n').slice(0, 4).join('\n');
    const body = fs.readFileSync(mdRootChangelog, 'utf-8').split('\n').slice(4).join('\n');
    fs.writeFileSync(mdRootChangelog, `${header}\n${newEntry}\n${body}`);
}

function generateJsonApiFiles(releaseJson) {
    fs.mkdirSync(jsonDetailDir, { recursive: true });
    fs.writeFileSync(jsonDetailFile, JSON.stringify(releaseJson, null, 2));
    let indexData = { releases: [] };
    if (fs.existsSync(jsonIndexFile)) indexData = JSON.parse(fs.readFileSync(jsonIndexFile, 'utf-8'));
    const newIndexEntry = { version: releaseJson.version, date: releaseJson.date, path: `./releases/${version}.json` };
    indexData.releases = [newIndexEntry, ...indexData.releases.filter(r => r.version !== releaseJson.version)];
    fs.writeFileSync(jsonIndexFile, JSON.stringify(indexData, null, 2));
}

// --- Asset Preparation Logic ---
function prepareAssets() {
  const assetsDir = 'target/release-assets';
  fs.mkdirSync(assetsDir, { recursive: true });

  const jarFile = fs.readdirSync('target').find(file => file.endsWith('.jar'));
  if (!jarFile) {
    console.error('ERROR: JAR file not found in target directory!');
    process.exit(1);
  }
  
  const sourcePath = path.join('target', jarFile);
  const assetName = `app-v${version}.jar`;
  const destPath = path.join(assetsDir, assetName);
  
  fs.renameSync(sourcePath, destPath);
  console.log(`Renamed JAR to ${assetName}`);

  // Generate checksum
  const fileBuffer = fs.readFileSync(destPath);
  const hashSum = crypto.createHash('sha256');
  hashSum.update(fileBuffer);
  const hex = hashSum.digest('hex');
  fs.writeFileSync(path.join(assetsDir, 'checksums.txt'), `${hex}  ${assetName}\n`);
  console.log('Generated checksums.txt');
}

// --- Execute Script ---
prepareRelease();

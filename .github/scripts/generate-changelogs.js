const fs = require('fs');
const path = require('path');

// Get Release Info from Environment Variables
const version = process.argv[2];
const notes = process.env.SEMANTIC_RELEASE_NEXT_RELEASE_NOTES;
const commitsRaw = process.env.SEMANTIC_RELEASE_COMMITS;

if (!version || !notes || !commitsRaw) {
  console.error('Error: Release information was not found in environment variables.');
  process.exit(1);
}

const commits = JSON.parse(commitsRaw);

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

// --- Main Generation Function ---
function generateAllChangelogs() {
  console.log(`--- Generating changelog files for version ${version} ---`);
  const releaseJson = createReleaseJsonObject();
  generateMarkdownFiles(releaseJson);
  generateJsonApiFiles(releaseJson);
  console.log('--- Changelog generation complete ---');
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

// --- Execute Script ---
generateAllChangelogs();

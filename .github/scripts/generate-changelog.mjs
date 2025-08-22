const fs = require('fs');
const path = require('path');

// This script is called by semantic-release.
const { nextRelease, commits } = require('semantic-release/lib/get-release-info');

// Get the version from the command line argument passed by the 'prepareCmd'
const version = process.argv[2];

if (!version) {
  console.error('Error: Release version was not provided as an argument.');
  process.exit(1);
}

// --- Path Definitions ---
const majorVersion = `v${version.split('.')[0]}`;
const changelogsDir = 'changelogs';

// Markdown Paths
const mdDetailDir = path.join(changelogsDir, 'details');
const mdDetailFile = path.join(mdDetailDir, `${version}.md`);
const mdRootChangelog = 'CHANGELOG.md';

// JSON API Paths
const jsonApiDir = path.join(changelogsDir, 'api', majorVersion);
const jsonDetailDir = path.join(jsonApiDir, 'releases');
const jsonDetailFile = path.join(jsonDetailDir, `${version}.json`);
const jsonIndexFile = path.join(jsonApiDir, 'index.json');


// --- Main Generation Function ---
function generateChangelogs() {
  if (!nextRelease || !commits) {
    console.log('No release info found. Skipping changelog generation.');
    return;
  }

  // 1. Create the structured JSON object for the current release
  const releaseJson = createReleaseJsonObject();

  // 2. Generate all Markdown files
  generateMarkdownFiles(releaseJson);

  // 3. Generate all JSON API files
  generateJsonApiFiles(releaseJson);

  console.log(`Successfully generated all changelog artifacts for version ${version}`);
}

// --- Helper Functions ---

/**
 * Creates a structured JSON object from the semantic-release context.
 */
function createReleaseJsonObject() {
  const releaseData = {
    version: nextRelease.version,
    date: new Date().toISOString(),
    notes: nextRelease.notes, // The full markdown notes
    commits: {
      features: [],
      fixes: [],
      performance: [],
      refactoring: [],
      tests: [],
      styles: [],
      docs: [],
      chores: [],
      breakingChanges: [],
    },
  };

  commits.forEach(commit => {
    const commitData = {
      commit: commit.hash.substring(0, 7),
      author: commit.author.name,
      date: commit.committerDate,
      message: commit.subject,
      scope: commit.scope || 'general',
      issue: (commit.message.match(/#(\d+)/) || [])[1] || null,
    };

    const typeMap = {
        feat: 'features',
        fix: 'fixes',
        perf: 'performance',
        refactor: 'refactoring',
        test: 'tests',
        style: 'styles',
        docs: 'docs',
        chore: 'chores',
    };

    if (typeMap[commit.type]) {
        releaseData.commits[typeMap[commit.type]].push(commitData);
    }

    if (commit.notes.some(note => note.title.toUpperCase().includes('BREAKING CHANGE'))) {
      releaseData.commits.breakingChanges.push(commitData);
    }
  });

  return releaseData;
}

/**
 * Generates the root CHANGELOG.md and the detailed markdown file.
 * @param {object} releaseJson - The structured release data.
 */
function generateMarkdownFiles(releaseJson) {
  // Create detailed markdown file
  fs.mkdirSync(mdDetailDir, { recursive: true });
  fs.writeFileSync(mdDetailFile, releaseJson.notes);
  console.log(`Created detailed markdown changelog: ${mdDetailFile}`);

  // Update the root CHANGELOG.md
  if (!fs.existsSync(mdRootChangelog)) {
    fs.writeFileSync(mdRootChangelog, `# Changelog\n\nAll notable changes to this project will be documented in this file.\n\n`);
  }

  const newEntry = `* [**${releaseJson.version}**](./${mdDetailFile}) - ${new Date().toISOString().split('T')[0]}`;
  const header = fs.readFileSync(mdRootChangelog, 'utf-8').split('\n').slice(0, 4).join('\n');
  const body = fs.readFileSync(mdRootChangelog, 'utf-8').split('\n').slice(4).join('\n');
  fs.writeFileSync(mdRootChangelog, `${header}\n${newEntry}\n${body}`);
  console.log(`Updated root changelog: ${mdRootChangelog}`);
}

/**
 * Generates the versioned JSON API files (index and details).
 * @param {object} releaseJson - The structured release data.
 */
function generateJsonApiFiles(releaseJson) {
  // Create detailed JSON file
  fs.mkdirSync(jsonDetailDir, { recursive: true });
  fs.writeFileSync(jsonDetailFile, JSON.stringify(releaseJson, null, 2));
  console.log(`Created detailed JSON changelog: ${jsonDetailFile}`);

  // Update the JSON index file
  let indexData = { releases: [] };
  if (fs.existsSync(jsonIndexFile)) {
    indexData = JSON.parse(fs.readFileSync(jsonIndexFile, 'utf-8'));
  }

  const newIndexEntry = {
    version: releaseJson.version,
    date: releaseJson.date,
    path: `./releases/${version}.json`,
  };

  // Add new entry to the top, preventing duplicates
  indexData.releases = [newIndexEntry, ...indexData.releases.filter(r => r.version !== releaseJson.version)];

  fs.writeFileSync(jsonIndexFile, JSON.stringify(indexData, null, 2));
  console.log(`Updated JSON index file: ${jsonIndexFile}`);
}

// --- Execute Script ---
generateChangelogs();

const fs = require("fs");
const path = require("path");
const { execSync } = require("child_process");
const crypto = require("crypto");

// Get Release Info from command-line arguments
const version = process.argv[2];
const notes = process.argv[3];
const commitsRaw = process.argv[4];
// Get additional metadata from the GitHub Actions environment
const branchName = process.env.GITHUB_REF_NAME;
const buildNumber = process.env.GITHUB_RUN_NUMBER;
const commitSha = process.env.GITHUB_SHA;
const actor = process.env.GITHUB_ACTOR;
const repository = process.env.GITHUB_REPOSITORY;

console.log("Release Manifest Information:");
console.log(`Version: ${version}`);
console.log(`Notes: ${notes}`);
console.log(`Commits: ${commitsRaw}`);
console.log(`Build Number: ${buildNumber}`);
console.log(`Branch Name: ${branchName}`);

if (!version || !notes || !commitsRaw || !branchName || !buildNumber) {
  console.error('Error: Missing required arguments or environment variables.');
  process.exit(1);
}

const commits = JSON.parse(commitsRaw);
console.log({commits});

// --- Helper function to run shell commands ---
function runCommand(command) {
  console.log(`> ${command}`);
  execSync(command, { stdio: "inherit" });
}

// --- Path Definitions ---
const majorVersion = `v${version.split(".")[0]}`;
const changelogsDir = "changelogs";
const mdDetailDir = path.join(changelogsDir, "details");
const mdDetailFile = path.join(mdDetailDir, `${version}.md`);
const mdRootChangelog = "CHANGELOG.md";
const jsonApiDir = path.join(changelogsDir, "api", majorVersion);
const jsonDetailDir = path.join(jsonApiDir, "releases");
const jsonDetailFile = path.join(jsonDetailDir, `${version}.json`);
const jsonIndexFile = path.join(jsonApiDir, "index.json");

// --- Main Preparation Function ---
function prepareRelease() {
  console.log(`--- Preparing release for version ${version} ---`);

  // Step 1: Update pom.xml version
  console.log(`Updating pom.xml to version ${version}...`);
  runCommand(`mvn versions:set -DnewVersion=${version} -DprocessAllModules`);

  // Step 2: Generate all changelog files (JSON and Markdown)
  console.log("Generating changelog files...");
  generateAllChangelogs();

  // Step 3: Build the project and create the JAR
  console.log("Building project with Maven...");
  runCommand("mvn -B package -DskipTests");

  // Step 4: Prepare and name the release assets
  console.log("Preparing release assets...");
  prepareAssets();

  console.log("--- Release preparation complete ---");
}

// --- Changelog Generation Logic ---
function generateAllChangelogs() {
  const releaseJson = createReleaseJsonObject();
  generateMarkdownFiles(releaseJson);
  generateJsonApiFiles(releaseJson);
}

function createReleaseJsonObject() {
  const conventionalCommitRegex = /^(?<type>[a-zA-Z]+)(?:\((?<scope>[\w\s-]+)\))?!?: (?<subject>.*)$/;

  const releaseData = {
    version,
    date: new Date().toISOString(),
    notes,
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
  commits.forEach((commit) => {

    // Regex to parse the conventional commit message
    const match = commit.message.split('\n')[0].match(conventionalCommitRegex);
    const commitType = match ? match.groups.type : null;
    const commitData = {
      commit: commit.hash.substring(0, 7),
      author: commit.author.name,
      date: commit.committerDate,
      message: commit.subject,
      scope: commit.scope || "general",
      issue: (commit.message.match(/#(\d+)/) || [])[1] || null,
    };
    const typeMap = {
      feat: "features",
      fix: "fixes",
      perf: "performance",
      refactor: "refactoring",
      test: "tests",
      style: "styles",
      docs: "docs",
      chore: "chores",
    };
    if (commitType && typeMap[commitType]) {
        releaseData.commits[typeMap[commitType]].push(commitData);
    }

    if (commit.message.includes('BREAKING CHANGE:')) {
        releaseData.commits.breakingChanges.push(commitData);
    }
  });
  return releaseData;
}

function generateMarkdownFiles(releaseJson) {

    // --- NEW LOGIC TO BUILD MARKDOWN TABLE ---
    const typeSections = {
        breakingChanges: '💥 BREAKING CHANGES',
        features: '✨ New Features',
        fixes: '🐛 Bug Fixes',
        performance: '⚡️ Performance Improvements',
        refactoring: '♻️ Code Refactoring',
        tests: '🧪 Tests',
        styles: '🎨 Styles',
        docs: '📝 Documentation',
        chores: '⚙️ Miscellaneous Chores',
    };

    let markdownNotes = `# ${releaseJson.version} (${new Date().toISOString().split('T')[0]})\n\n`;

    const createTable = (commitList) => {
        let table = '| Commit | Scope | Description | PR / Issue | Author |\n';
        table += '|---|---|---|---|---|\n';
        commitList.forEach(c => {
            const issue = c.issue ? `[#${c.issue}](https://github.com/${repository}/issues/${c.issue})` : 'N/A';
            const commitLink = `[${c.commit}](https://github.com/${repository}/commit/${c.commit})`;
            table += `| ${commitLink} | \`${c.scope}\` | ${c.message} | ${issue} | ${c.author} |\n`;
        });
        return table;
    };

    for (const key in typeSections) {
        if (releaseJson.commits[key] && releaseJson.commits[key].length > 0) {
            markdownNotes += `### ${typeSections[key]}\n\n` + createTable(releaseJson.commits[key]) + '\n';
        }
    }
    // --- END OF NEW LOGIC ---

  fs.mkdirSync(mdDetailDir, { recursive: true });
  fs.writeFileSync(mdDetailFile, markdownNotes);
  if (!fs.existsSync(mdRootChangelog))
    fs.writeFileSync(
      mdRootChangelog,
      `# Changelog\n\nAll notable changes to this project will be documented in this file.\n\n`
    );
  const newEntry = `* [**${releaseJson.version}**](./${mdDetailFile}) - ${
    new Date().toISOString().split("T")[0]
  }`;
  const header = fs
    .readFileSync(mdRootChangelog, "utf-8")
    .split("\n")
    .slice(0, 4)
    .join("\n");
  const body = fs
    .readFileSync(mdRootChangelog, "utf-8")
    .split("\n")
    .slice(4)
    .join("\n");
  fs.writeFileSync(mdRootChangelog, `${header}\n${newEntry}\n${body}`);
}

function generateJsonApiFiles(releaseJson) {
  fs.mkdirSync(jsonDetailDir, { recursive: true });
  fs.writeFileSync(jsonDetailFile, JSON.stringify(releaseJson, null, 2));
  let indexData = { releases: [] };
  if (fs.existsSync(jsonIndexFile))
    indexData = JSON.parse(fs.readFileSync(jsonIndexFile, "utf-8"));
  const newIndexEntry = {
    version: releaseJson.version,
    date: releaseJson.date,
    path: `./releases/${version}.json`,
  };
  indexData.releases = [
    newIndexEntry,
    ...indexData.releases.filter((r) => r.version !== releaseJson.version),
  ];
  fs.writeFileSync(jsonIndexFile, JSON.stringify(indexData, null, 2));
}

// --- Asset Preparation Logic ---
// --- Asset Preparation Logic ---
function getChannel(branch) {
    if (branch === 'main') return 'stable';
    if (branch === 'next') return 'next';
    if (branch === 'beta') return 'rc';
    if (branch === 'dev') return 'nightly';
    if (branch.match(/^\d+\.x$/)) return 'lts';
    return 'snapshot'; // Fallback
}

function prepareAssets() {
  const appBundlename = "auth4j.id"
  const assetsDir = 'target/release-assets';
  fs.mkdirSync(assetsDir, { recursive: true });

  const jarFile = fs.readdirSync('target').find(file => file.endsWith('.jar'));
  if (!jarFile) {
    console.error('ERROR: JAR file not found in target directory!');
    process.exit(1);
  }
  
  const sourcePath = path.join('target', jarFile);

  // --- NEW ASSET NAMING LOGIC ---
  const channel = getChannel(branchName);
  const date = new Date().toISOString().slice(0, 10).replace(/-/g, ''); // YYYYMMDD
  const time = new Date().toTimeString().slice(0, 8).replace(/:/g, ''); // HHMMSS
  
  const assetName = `${appBundlename}-${version}-${channel}-${date}-${time}-build.${buildNumber}.jar`;
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
  generateBuildManifest(assetName, assetsDir, hex, channel);
}

function generateBuildManifest(assetName, assetsDir, checksum, channel) {
    console.log(`Generating build-manifest.json... into: ${assetsDir}`);
    const manifestPath = path.join(assetsDir, 'build-manifest.json');
    let manifest = {
        version: version,
        channel: channel,
        build: {
            number: parseInt(buildNumber, 10),
            timestamp: new Date().toISOString(),
            triggeredBy: actor,
            commit: commitSha,
        },
        project: {
            repository: repository,
            branch: branchName,
        },
        assets: [
            {
                filename: assetName,
                size: fs.statSync(path.join(assetsDir, assetName)).size,
                checksum: {
                    sha256: checksum
                }
            },
            {
                filename: 'checksums.txt',
                size: fs.statSync(path.join(assetsDir, 'checksums.txt')).size
            }
        ]
    };

    // First, write the manifest without its own checksum to calculate it
    fs.writeFileSync(manifestPath, JSON.stringify(manifest, null, 2));

    // Now, calculate the checksum of the manifest file itself
    const manifestBuffer = fs.readFileSync(manifestPath);
    const manifestHashSum = crypto.createHash('sha256');
    manifestHashSum.update(manifestBuffer);
    const manifestChecksum = manifestHashSum.digest('hex');

    // Add the self-checksum to the manifest object
    manifest.manifest_checksum = {
        sha256: manifestChecksum
    };
    
    // Add the manifest file itself to the assets list
    manifest.assets.push({
        filename: 'build-manifest.json',
        size: fs.statSync(manifestPath).size,
        checksum: {
            sha256: manifestChecksum
        }
    });

    // Finally, overwrite the file with the complete manifest
    fs.writeFileSync(manifestPath, JSON.stringify(manifest, null, 2));
    console.log('Successfully generated self-verifying build-manifest.json');
}

// --- Execute Script ---
prepareRelease();

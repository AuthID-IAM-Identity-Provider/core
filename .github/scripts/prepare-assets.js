const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');
const crypto = require('crypto');

// Get the version from the command line argument
const version = process.argv[2];

if (!version) {
  console.error('Error: Release version was not provided as an argument.');
  process.exit(1);
}

// --- Helper function to run shell commands ---
function runCommand(command) {
  console.log(`> ${command}`);
  execSync(command, { stdio: 'inherit' });
}

// --- Main Preparation Function ---
function prepareAssets() {
  console.log(`--- Preparing assets for version ${version} ---`);

  // Step 1: Update pom.xml version
  console.log(`Updating pom.xml to version ${version}...`);
  runCommand(`mvn versions:set -DnewVersion=${version} -DprocessAllModules`);

  // Step 2: Build the project and create the JAR
  console.log('Building project with Maven...');
  runCommand('mvn -B package -DskipTests');

  // Step 3: Prepare and name the release assets
  console.log('Preparing release assets...');
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
  
  console.log('--- Asset preparation complete ---');
}

// --- Execute Script ---
prepareAssets();

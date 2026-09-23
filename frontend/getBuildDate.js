/**
 * Build-time script that generates src/app/app-info.json with the current
 * appVersion, alfVersion, and buildDate. It also copies i18n translations into a
 * versioned assets folder (src/assets/<version>/i18n).
 *
 * The generated JSON is git-ignored and imported at compile time
 * (see main.ts). Keeping it out of version control avoids the constant
 * buildDate churn that a tracked file would produce.
 *
 * Usage:
 *   node getBuildDate.js               Full run: (re)write app-info.json with a
 *                                      fresh buildDate + copy i18n translations.
 *                                      Used by real builds/serves.
 *   node getBuildDate.js --if-missing  Guard mode: write app-info.json only when
 *                                      it does not exist, and skip the i18n copy.
 *                                      Used by postinstall / pre* hooks so a fresh
 *                                      checkout compiles without forcing churn.
 *
 * Uses only node:fs (no external deps) so it is safe to run from postinstall.
 */
const fs = require('node:fs');

const fileName = 'src/app/app-info.json';
const appVersion = require('./package.json').version;
const alfVersion = '26.1 (OSS)';
const onlyIfMissing = process.argv.includes('--if-missing');

function writeAppInfo() {
  const appInfo = {
    appVersion,
    alfVersion,
    buildDate: new Date().toString(),
  };
  fs.writeFileSync(fileName, `${JSON.stringify(appInfo, null, 2)}\n`);
  console.log(`Generated ${fileName}`);
}

function deleteOldTranslationFolders(assetsDir, currentVersion) {
  const versionPattern = /^\d+\.\d+\.\d+\.\d+$/;
  for (const entry of fs.readdirSync(assetsDir)) {
    if (entry !== currentVersion && versionPattern.test(entry)) {
      fs.rmSync(`${assetsDir}/${entry}`, { recursive: true, force: true });
      console.log(`Deleted old translation folder: ${entry}`);
    }
  }
}

function copyTranslations(from, to) {
  deleteOldTranslationFolders('src/assets', appVersion);
  fs.cpSync(from, to, { recursive: true });
}

if (onlyIfMissing) {
  // Guard mode: only ensure the file exists so compilation/tests don't fail on a
  // fresh checkout. Do not refresh buildDate or touch translations.
  if (fs.existsSync(fileName)) {
    console.log(`${fileName} already exists, skipping (--if-missing)`);
  } else {
    writeAppInfo();
  }
} else {
  writeAppInfo();
  copyTranslations('src/assets/i18n', `src/assets/${appVersion}/i18n`);
  console.log('Copy translation!');
}

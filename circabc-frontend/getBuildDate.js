var fs = require('fs');
const fse = require('fs-extra');

let fileName = 'src/app/app-info.ts';
// executes `svn info`
const appVersion = '4.2.4.3';
const alfVersion = '4.2.f (OSS)';
fs.exists(fileName, (exists) => {
  if (exists) {
    copyFolderSync('src/assets/i18n',`src/assets/${appVersion}/i18n`);
    console.log('Copy translation!');
    updateFile();
  } else {
    console.log('file not exists ');
    let text = `/* tslint:disable */
  export const appInfo = {
  appVersion: '${appVersion}',
  alfVersion: '${alfVersion}',
  buildDate: ${new Date().toString()},
 };`;
    fs.writeFile(fileName, text, function (err) {
      if (err) {
        return console.log(err);
      }
      copyFolderSync('src/assets/i18n',`src/assets/${appVersion}/i18n`);
      console.log('Copy translation!');
      updateFile();
      console.log('The file was saved!');
    });
  }
});

function updateFile() {
  fs.readFile(fileName, 'utf8', (error, data) => {
    if (error) {
      console.error('error in update file ', error);
    }
    var lines = data.split('\n');
    lines.forEach((line) => {
      if (line !== '') {
        var newLine = line;

        if (line.indexOf('buildDate') !== -1) {
          var dateBuild = new Date();
          newLine = `\tbuildDate: '${dateBuild.toString()}',`;
        } 
        if (line.indexOf('appVersion') !== -1) {
          newLine = `\tappVersion: '${appVersion}',`;
        }
        if (line.indexOf('alfVersion') !== -1) {
          newLine = `\talfVersion: '${alfVersion}',`;
        }
        fs.appendFileSync('src/app/app-info.ts.tmp', newLine + '\n');
      }
    });

    fs.unlinkSync(fileName);
    fs.renameSync('src/app/app-info.ts.tmp', fileName);
    console.log('Successfully updated value');
  });
}
function copyFolderSync(from, to) {
  fse.copySync(from, to);
}

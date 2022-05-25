var fs = require('fs');
let fileName = 'src/app/app-info.ts';
// executes `svn info`
const appVersion = '4.2.0.0';
fs.exists(fileName, (exists) => {
  if (exists) {
    updateFile();
  } else {
    console.log('file not exists ');
    let text = `/* tslint:disable */
  export const appInfo = {
  appVersion: '${appVersion}',
  alfVersion: '4.2.4',
  buildDate: ${new Date()}
 };`;
    fs.writeFile(fileName, text, function (err) {
      if (err) {
        return console.log(err);
      }
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
          newLine = `\tbuildDate: '${dateBuild.toUTCString()}'`;
        } else if (line.indexOf('appVersion') !== -1) {
          newLine = `\tappVersion: '${appVersion}',`;
        }
        fs.appendFileSync('src/app/app-info.ts.tmp', newLine + '\n');
      }
    });

    fs.unlinkSync(fileName);
    fs.renameSync('src/app/app-info.ts.tmp', fileName);
    console.log('Successfully updated value');
  });
}

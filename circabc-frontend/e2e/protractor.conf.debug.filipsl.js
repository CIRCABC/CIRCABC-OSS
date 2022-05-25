// Protractor configuration file, see link for more information
// https://github.com/angular/protractor/blob/master/lib/config.ts

const { SpecReporter } = require('jasmine-spec-reporter');

exports.config = {
  allScriptsTimeout: 1100000,
  specs: [
    './dist/out-tsc-e2e/**/*.e2e-spec.js'
  ],
  capabilities: {
    'browserName': 'chrome',
    'chromeOptions': {
      'useAutomationExtension': false,
      'args': ['--start-maximized'],
    }
  },
  directConnect: true,
  baseUrl: 'http://d02di1637770dit.net1.cec.eu.int:4200/',
  framework: 'jasmine',
  jasmineNodeOpts: {
    showColors: true,
    defaultTimeoutInterval: 30000,
    print: function () { }
  },
  beforeLaunch: function () {
    require('ts-node').register({
      project: 'e2e/tsconfig.e2e.json'
    });
  },
  onPrepare() {
    jasmine.getEnv().addReporter(new SpecReporter({ spec: { displayStacktrace: true } }));
  }
};

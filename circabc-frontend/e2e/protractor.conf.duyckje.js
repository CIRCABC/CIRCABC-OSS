// Protractor configuration file, see link for more information
// https://github.com/angular/protractor/blob/master/lib/config.ts

const { SpecReporter } = require('jasmine-spec-reporter');

exports.config = {
  allScriptsTimeout: 11000,
  specs: [
    './e2e/**/*.e2e-spec.ts'
  ],
  capabilities: {
    'browserName': 'chrome',
    'chromeOptions': {
      'useAutomationExtension': false,
      'args': ['--start-maximized'],
    },

    seleniumAddress: 'http://d02di1335287dit.net1.cec.eu.int:4444/wd/hub'
  },
  directConnect: false,
  baseUrl: 'http://d02di1335287dit.net1.cec.eu.int:4200/',
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
    jasmine.getEnv().addReporter(new SpecReporter({ spec: { displaySuccessesSummary: true, displayStacktrace: true, displayDuration: true } }));
  },

  params: {
    login: {
      userLogin: 'duyckjeFromConf',
      password: 'myPasswordFromConf'
    }
  }
};

#!/usr/bin/env zx

$.verbose = true;

if (process.argv.length !== 4) {
  console.log(chalk.red('Please specify environment'));
  process.exit(1);
}

const myArgs = process.argv.slice(3);

let environment = myArgs[0].toLowerCase();
switch (environment) {
  case 'acceptance':
  case 'acc':
    environment = 'acc';
    break;
  case 'development':
  case 'dev':
    environment = 'dev';
    break;
  case 'test':
    environment = 'test';
    break;
  case 'training':
    environment = 'training';
    break;
  case 'stress':
  case 'load':
    environment = 'stress';
    break;
  case 'prod':
  case 'production':
    environment = 'prod';
    break;
  case 'acceptance-shs':
  case 'acc-shs':
    environment = 'acc-shs';
    break;
  case 'production-shs':
  case 'prod-shs':
    environment = 'prod-shs';
    break;
  case 'acceptance-enisa':
  case 'acc-enisa':
    environment = 'acc-enisa';
    break;
  case 'production-enisa':
  case 'prod-enisa':
    environment = 'prod-enisa';
    break;
  case 'acceptance-comm':
  case 'acc-comm':
    environment = 'acc-comm';
    break;
  case 'production-comm':
  case 'prod-comm':
    environment = 'prod-comm';
    break;
  case 'acceptance-echa':
  case 'acc-echa':
    environment = 'acc-echa';
    break;
  case 'production-echa':
  case 'prod-echa':
    environment = 'prod-echa';
    break;

  default:
    console.log(chalk.red('invalid enviroment: ' + environment));
    process.exit(2);
}

let dgName = 'DIGIT';
let isName = 'CIRCABC';
let nexusRepository =
  'https://weblogic-nexus.devops.tech.ec.europa.eu/repository/weblogic-circabc-snapshots';


let envName = 'Development';
let adminServer = 'wlsopert,wlstd04903,1041,CIRCABC001_DEVserver';
let wlTargets = ['CIRCABC001_DEVwls11', 'CIRCABC001_DEVwls21'];
let domain = 'CIRCABC001_DEV';
let artifactId = `circabc-backend-${environment}`;

switch (environment) {
  case 'acc':
    envName = 'Acceptance';
    adminServer = 'wlsopert,wlstd04908,1041,CIRCABC_ACCserver';
    wlTargets = ['CIRCABC_ACCwls11', 'CIRCABC_ACCwls21'];
    domain = 'CIRCABC_ACC';
    break;
  case 'dev':
    //dev already done
    break;
  case 'test':
    envName = 'Test';
    adminServer = 'wlsopert,wlstd04849,1041,CIRCABC001_TSTserver';
    wlTargets = ['CIRCABC001_TSTwls11', 'CIRCABC001_TSTwls21'];
    domain = 'CIRCABC001_TST';
    break;
  case 'training':
    envName = 'Training';
    adminServer = 'wlsopert,wlmcl01450,1041,CIRCABC001_TNGserver';
    wlTargets = ['CIRCABC001_TNGwls11'];
    domain = 'CIRCABC001_TNG';
    break;
  case 'stress':
    envName = 'Stress';
    adminServer = 'wlsopert,wlstl00513,1041,CIRCABC001_LDTserver';
    wlTargets = ['CIRCABC001_LDTwls11', 'CIRCABC001_LDTwls21'];
    domain = 'CIRCABC001_LDT';
    break;
  case 'prod':
    envName = 'Production';
    adminServer = 'wlsopert,wlmcl01458,1041,CIRCABC_PRDserver';
    wlTargets = ['CIRCABC_PRDwls11', 'CIRCABC_PRDwls21', 'CIRCABC_PRDwls31'];
    domain = 'CIRCABC_PRD';
    break;
  case 'acc-shs':
    envName = 'Acceptance';
    adminServer = 'wlsopert,wlscl19030,1041,S-CIRCABC_ACCserver';
    wlTargets = ['S-CIRCABC_ACCwls11', 'S-CIRCABC_ACCwls21'];
    domain = 'S-CIRCABC_ACC';
    isName = 'S-CIRCABC';
    nexusRepository =
      'https://weblogic-nexus.devops.tech.ec.europa.eu/repository/weblogic-s-circabc-snapshots';
    break;
  case 'prod-shs':
    envName = 'Production';
    adminServer = 'wlsopert,wlscl19031,1041,S-CIRCABC_PRODserver';
    wlTargets = ['S-CIRCABC_PRODwls11', 'S-CIRCABC_PRODwls21'];
    domain = 'S-CIRCABC_PROD';
    isName = 'S-CIRCABC';
    nexusRepository =
      'https://weblogic-nexus.devops.tech.ec.europa.eu/repository/weblogic-s-circabc-snapshots';
    break;
  case 'acc-enisa':
    envName = 'Acceptance';
    adminServer = 'wlsopert,wlstd04900,1041,CIRCABC001_ACCserver';
    wlTargets = ['CIRCABC001_ACCwls11', 'CIRCABC001_ACCwls21'];
    domain = 'CIRCABC001_ACC';
    isName = 'CIRCABC';
    dgName = 'ENISA';
    break;
  case 'prod-enisa':
    envName = 'Production';
    adminServer = 'wlsopert,wlmcl01483,1041,CIRCABC001_PRDserver';
    wlTargets = ['CIRCABC001_PRDwls11', 'CIRCABC001_PRDwls21'];
    domain = 'CIRCABC001_PRD';
    isName = 'CIRCABC';
    dgName = 'ENISA';
    break;
  case 'acc-comm':
    envName = 'Acceptance';
    adminServer = 'wlsopert,wlstd03314,1041,CIRCABCEW001_ACCserver';
    wlTargets = ['CIRCABCEW001_ACCwls11', 'CIRCABCEW001_ACCwls21'];
    domain = 'CIRCABCEW001_ACC';
    isName = 'CIRCABC EWPP';
    dgName = 'COMM';
    break;
  case 'prod-comm':
    envName = 'Production';
    adminServer = 'wlsopert,wlccl00078,1041,CIRCABCEW001_PRDserver';
    wlTargets = ['CIRCABCEW001_PRDwls11', 'CIRCABCEW001_PRDwls21'];
    domain = 'CIRCABCEW001_PRD';
    isName = 'CIRCABC EWPP';
    dgName = 'COMM';
    break;
  case 'acc-echa':
    envName = 'Acceptance';
    adminServer = 'wlsopert,wlstd04897,1041,CIRCABC-ECHA_ACCserver';
    wlTargets = ['CIRCABC-ECHA_ACCwls11', 'CIRCABC-ECHA_ACCwls21'];
    domain = 'CIRCABC-ECHA_ACC';
    isName = 'CIRCABC';
    dgName = 'ECHA';
    break;
  case 'prod-echa':
    envName = 'Production';
    adminServer = 'wlsopert,wlmcl01463,1041,CIRCABC-ECHA_PRDserver';
    wlTargets = ['CIRCABC-ECHA_PRDwls11', 'CIRCABC-ECHA_PRDwls21'];
    domain = 'CIRCABC-ECHA_PRD';
    isName = 'CIRCABC';
    dgName = 'ECHA';
    break;
  default:
    console.log(chalk.red('invalid enviroment: ' + environment));
    process.exit(2);
}



const isSCircabc = environment === 'acc-shs' || environment === 'prod-shs';

let user = process.env.USER_NAME;
let password = process.env.PASSWORD;
let ldapUser = process.env.LDAP_USER;

for (let index = 0; index < 1 ; index++) {
  const wlTarget = wlTargets[index];
  const oneWlTarget = [wlTarget];

  const undeployBody = {
    ldapUser: ldapUser,
    dgName: dgName,
    isName: isName,
    envName: envName,
    adminServer: adminServer,
    wlTargets: oneWlTarget,
    domain: domain,
    product: 'WLS',
    operation: 'UNDEPLOY_APPLICATION',
    nexusRepository: nexusRepository,
    groupId: 'circabc',
    artifactId: artifactId,
    buildVersion: '4.2.0.0-SNAPSHOT',
    applicationName: 'Circabc_Enterprise'
  };

  const url =
    'https://intragate.ec.europa.eu/jasspr/services/weblogic12/v1/application/manage';

  try {

    let resp = await fetch(url, {
      method: 'post',
      body: JSON.stringify(undeployBody),
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Basic ${Buffer.from(user + ':' + password).toString(
          'base64'
        )}`,
      },
    });
    if (resp.ok) {
      const response = await resp.json();
      console.log(chalk.green(response.SMT.ticket.number));
      const deployStatusUrl = `https://intragate.ec.europa.eu/jasspr/services/weblogic12/v1/application/deploy-status?smt-ticket-number=${response.SMT.ticket.number}`;
      while (true) {
        let deployStatusResp = await fetch(deployStatusUrl);
        if (deployStatusResp.ok) {
          const deployStatusResponse = await deployStatusResp.json();
          const status = deployStatusResponse.status;
          if (status === 'IN PROGRESS') {
            console.log(chalk.yellow('deployment in progress'));
            await sleep(60000);
          }
          if (status === 'SUCCESS') {
            console.log(chalk.green('deployment succed'));
            break;
          }
          if (status === 'ERROR') {
            console.log(chalk.red('deployment failed'));
            break;
          }
        } else {
          break;
        }
      }
    } else {
      console.log(chalk.red('server error: ' + resp.status));
      console.log(chalk.red('server error: ' + resp.statusText));
      console.log(chalk.red(await resp.text()));
      process.exit(4);
    }
  } catch (error) {
    console.error(error);
    process.exit(5);
  }
}

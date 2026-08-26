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

let frontendTarget = 'development';
let backendTarget =
  environment.startsWith('acc') || environment.startsWith('prod')
    ? `wls${environment}`
    : `${environment}`;
let earFileSufix =
  environment.startsWith('acc') || environment.startsWith('prod')
    ? `wls${environment}`
    : `${environment}`;
let envName = 'Development';
let adminServer = 'wlsopert,wlstd02006,1041,CIRCABC001_DEVserver';
let wlTargets = ['CIRCABC001_DEVwls11', 'CIRCABC001_DEVwls21'];
let domain = 'CIRCABC001_DEV';
let artifactId = `circabc-backend-${environment}`;

switch (environment) {
  case 'acc':
    frontendTarget = 'acceptance';
    envName = 'Acceptance';
    adminServer = 'wlsopert,wlstd06062,1041,CIRCABC_ACCserver';
    wlTargets = ['CIRCABC_ACCwls11', 'CIRCABC_ACCwls21'];
    domain = 'CIRCABC_ACC';
    break;
  case 'dev':
    //dev already done
    break;
  case 'test':
    frontendTarget = 'test';
    envName = 'Test';
    adminServer = 'wlsopert,wlstd02004,1041,CIRCABC001_TSTserver';
    wlTargets = ['CIRCABC001_TSTwls11', 'CIRCABC001_TSTwls21'];
    domain = 'CIRCABC001_TST';
    break;
  case 'training':
    frontendTarget = 'build-training';
    envName = 'Training';
    adminServer = 'wlsopert,wlmcl00503,1041,CIRCABC001_TNGserver';
    wlTargets = ['CIRCABC001_TNGwls11'];
    domain = 'CIRCABC001_TNG';
    break;
  case 'stress':
    frontendTarget = 'stress';
    envName = 'Stress';
    adminServer = 'wlsopert,wlstl00187,1041,CIRCABC001_LDTserver';
    wlTargets = ['CIRCABC001_LDTwls11', 'CIRCABC001_LDTwls21'];
    domain = 'CIRCABC001_LDT';
    break;
  case 'prod':
    frontendTarget = 'production';
    envName = 'Production';
    adminServer = 'wlsopert,wlmcl09071,1041,CIRCABC_PRDserver';
    wlTargets = ['CIRCABC_PRDwls11', 'CIRCABC_PRDwls21', 'CIRCABC_PRDwls31'];
    domain = 'CIRCABC_PRD';
    break;
  case 'acc-shs':
    frontendTarget = 'build-shs-acceptance';
    envName = 'Acceptance';
    adminServer = 'wlsopert,wlscl09030,1041,S-CIRCABC_ACCserver';
    wlTargets = ['S-CIRCABC_ACCwls11', 'S-CIRCABC_ACCwls21'];
    domain = 'S-CIRCABC_ACC';
    isName = 'S-CIRCABC';
    nexusRepository =
      'https://weblogic-nexus.devops.tech.ec.europa.eu/repository/weblogic-s-circabc-snapshots';
    break;
  case 'prod-shs':
    frontendTarget = 'build-shs-prod';
    envName = 'Production';
    adminServer = 'wlsopert,wlscl09031,1041,S-CIRCABC_PRODserver';
    wlTargets = ['S-CIRCABC_PRODwls11', 'S-CIRCABC_PRODwls21'];
    domain = 'S-CIRCABC_PROD';
    isName = 'S-CIRCABC';
    nexusRepository =
      'https://weblogic-nexus.devops.tech.ec.europa.eu/repository/weblogic-s-circabc-snapshots';
    break;
  case 'acc-enisa':
    frontendTarget = 'build-enisa-acceptance';
    envName = 'Acceptance';
    adminServer = 'wlsopert,wlstd01231,1041,CIRCABC001_ACCserver';
    wlTargets = ['CIRCABC001_ACCwls11', 'CIRCABC001_ACCwls21'];
    domain = 'CIRCABC001_ACC';
    isName = 'CIRCABC';
    dgName = 'ENISA';
    break;
  case 'prod-enisa':
    frontendTarget = 'build-enisa-prod';
    envName = 'Production';
    adminServer = 'wlsopert,wlmcl00278,1041,CIRCABC001_PRDserver';
    wlTargets = ['CIRCABC001_PRDwls11', 'CIRCABC001_PRDwls21'];
    domain = 'CIRCABC001_PRD';
    isName = 'CIRCABC';
    dgName = 'ENISA';
    break;
  case 'acc-comm':
    frontendTarget = 'build-comm-acceptance';
    envName = 'Acceptance';
    adminServer = 'wlsopert,wlstd03314,1041,CIRCABCEW001_ACCserver';
    wlTargets = ['CIRCABCEW001_ACCwls11', 'CIRCABCEW001_ACCwls21'];
    domain = 'CIRCABCEW001_ACC';
    isName = 'CIRCABC EWPP';
    dgName = 'COMM';
    break;
  case 'prod-comm':
    frontendTarget = 'build-comm-prod';
    envName = 'Production';
    adminServer = 'wlsopert,wlccl00078,1041,CIRCABCEW001_PRDserver';
    wlTargets = ['CIRCABCEW001_PRDwls11', 'CIRCABCEW001_PRDwls21'];
    domain = 'CIRCABCEW001_PRD';
    isName = 'CIRCABC EWPP';
    dgName = 'COMM';
    break;
  case 'acc-echa':
    frontendTarget = 'build-scircabc-acc';
    envName = 'Acceptance';
    adminServer = 'wlsopert,wlstd06065,1041,CIRCABC-ECHA_ACCserver';
    wlTargets = ['CIRCABC-ECHA_ACCwls11', 'CIRCABC-ECHA_ACCwls21'];
    domain = 'CIRCABC-ECHA_ACC';
    isName = 'CIRCABC';
    dgName = 'ECHA';
    break;
  case 'prod-echa':
    frontendTarget = 'build-scircabc-prod';
    envName = 'Production';
    adminServer = 'wlsopert,wlmcl09070,1041,CIRCABC-ECHA_PRDserver';
    wlTargets = ['CIRCABC-ECHA_PRDwls11', 'CIRCABC-ECHA_PRDwls21'];
    domain = 'CIRCABC-ECHA_PRD';
    isName = 'CIRCABC';
    dgName = 'ECHA';
    break;
  default:
    console.log(chalk.red('invalid enviroment: ' + environment));
    process.exit(2);
}

await $`git pull`;
cd(`${__dirname}/../circabc-frontend`);

const nodeVersion = await $`node --version`;

const useNpm = nodeVersion.stdout.trim() >= 'v18.19.1'; // Angular version 18.1.x || 18.2.x   nodejs version	^18.19.1 || ^20.11.1 || ^22.0.0

if (useNpm) {
  await $`npm install --force`;
  await $`npm run ${frontendTarget}`;
} else {
  // use earhly to build becuase can not install node 18 on AmazonWorkSpaces
  await $`rm -rf dist`;
  await $`earthly prune --reset`;
  await $`earthly +build-app --environment=${frontendTarget}`;
}

// disable service worker
// await $`rm ./dist/circabc/ngsw.json`;
// await $`cp -f ./dist/circabc/safety-worker.js ./dist/circabc/ngsw-worker.js`;

// overwrite REST API documentation 
await $`cp -f ./apis/openapi.yaml ../circabc-backend/source/web/swagger-ui/swagger.yaml`;

cd(`${__dirname}/..`);
await $`mvn clean`;
await $`mvn clean package --settings settings.xml -D"backend-target.env"=${backendTarget}  -D"frontend-target.env"=${frontendTarget} -D"server.node"=N1 -Dweblogic.version=12.2.1 -D"skip.installnodenpm"=true -D"skip.npm"=true  -D"migration.export.enable"=false`;
await $`mv circabc-backend/target/circabc-backend-2.0-${earFileSufix}.ear circabc-backend/target/SDK_Circa_ENT-2.0-${earFileSufix}.ear`;

cd(`${__dirname}/../circabc-backend/deploy_dc`);
await $`mkdir tmp`;
await $`cp ../target/*.ear tmp/`;
await $`mv tmp/SDK_Circa_ENT*.ear tmp/circabc-backend.ear`;

const isSCircabc = environment === 'acc-shs' || environment === 'prod-shs';

if (isSCircabc) {
  await $`mvn --settings settings.xml clean deploy -Ps-circabc -Dbackend-target.env=${environment} -Drevision=4.2.0.0-SNAPSHOT`;
} else {
  await $`mvn --settings settings.xml clean deploy -Dbackend-target.env=${environment} -Drevision=4.2.0.0-SNAPSHOT`;
}
await $`rm -rf tmp`;

let user = process.env.USER_NAME;
let password = process.env.PASSWORD;
let ldapUser = process.env.LDAP_USER;

for (let index = 0; index < 1 ; index++) {
  const wlTarget = wlTargets[index];
  const oneWlTarget = [wlTarget];

  const body = {
    ldapUser: ldapUser,
    dgName: dgName,
    isName: isName,
    envName: envName,
    adminServer: adminServer,
    wlTargets: oneWlTarget,
    domain: domain,
    product: 'WLS',
    operation: 'DEPLOY_APPLICATION',
    nexusRepository: nexusRepository,
    groupId: 'circabc',
    artifactId: artifactId,
    buildVersion: '4.2.0.0-SNAPSHOT',
    restart: true,
    cleanCache: true,
    cleanLog: true,
  };

  const url =
    'https://intragate.ec.europa.eu/jasspr/services/weblogic12/v1/application/manage';

  try {
    let resp = await fetch(url, {
      method: 'post',
      body: JSON.stringify(body),
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

// tslint:disable:no-console
// tslint:disable:prefer-template
// tslint:disable:no-relative-imports
// tslint:disable:no-require-imports
// tslint:disable:max-func-body-length
import {
  browser,
  ElementFinder,
  ExpectedConditions,
  WebElementPromise
} from 'protractor';

export async function createScreenShot(title: string, state: string) {
  let fileName: string;

  const date = new Date();
  // tslint:disable-next-line:max-line-length
  fileName = `./e2e/${title}_${state}_${date.getFullYear()}${date.getMonth()}${date.getDate()}${date.getHours()}${date.getMinutes()}${date.getSeconds()}${date.getMilliseconds()}.png`;

  console.info(
    `Start of the creation of the screen shot with the name ${fileName}`
  );
  const png = await browser.takeScreenshot();
  const fs = require('fs');
  const stream = fs.createWriteStream(fileName);
  stream.write(new Buffer(png, 'base64'));
  stream.end();
}

export async function waitFor(waitCondition: ElementFinder) {
  return browser.wait(ExpectedConditions.presenceOf(waitCondition), 12000);
}

export async function scrollToElement(elem: WebElementPromise) {
  await browser.driver.executeScript('arguments[0].scrollIntoView();', elem);
  console.log('Execute script scrollIntoView ');
}

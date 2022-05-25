// tslint:disable:no-console
// tslint:disable:prefer-template
// tslint:disable:no-relative-imports
// tslint:disable:no-require-imports
// tslint:disable:max-func-body-length
// tslint:disable:promise-function-async

import { WelcomePage } from './app.po';

describe('circabc App', () => {
  let page: WelcomePage;

  beforeEach(() => {
    page = new WelcomePage();
  });

  it('title should be Circabc', async () => {
    await page.navigateTo();
    const title = await page.title();
    await expect(title).toEqual('Circabc');
  });

  it('wrong credentials', async () => {
    await page.navigateTo();
    await page.goToLoginPage();
    const errorMessage = await page.loginFailed('beaurpi', 'MyWrongPassword1');
    await expect(errorMessage).not.toBeNull();
  });

  it('user should be logged', async () => {
    await page.navigateTo();
    await page.goToLoginPage();
    const uName = await page.login('beaurpi', '');
    await expect(uName.toLowerCase()).toEqual('pierre beauregard');
  });
});

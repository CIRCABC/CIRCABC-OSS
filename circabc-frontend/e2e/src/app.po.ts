// tslint:disable:no-console
// tslint:disable:prefer-template
// tslint:disable:no-relative-imports
// tslint:disable:no-require-imports
// tslint:disable:max-func-body-length
// tslint:disable:promise-function-async

import {
  browser,
  by,
  element,
  ElementArrayFinder,
  ElementFinder,
  ExpectedConditions,
  WebElement
} from 'protractor';
import * as e2eUtils from './app.e2e.utils';
import { E2eResult } from './e2eResult';

export class WelcomePage {
  async navigateTo() {
    return await browser.get('/welcome');
  }
  async title() {
    return await browser.getTitle();
  }

  async goToLoginPage() {
    const loginButton = element(by.css('a.cta'));
    await loginButton.click();
  }

  async login(userName: string, password: string) {
    await this.fillLogin(userName, password);
    const loginButton = element(by.css('button.cta'));
    await loginButton.click();

    try {
      await browser.wait(
        ExpectedConditions.presenceOf(
          element(by.css('div.personal-menu>span.personal-menu__name>span'))
        ),
        12000
      );
      const userNameInHeader = element(
        by.css('div.personal-menu>span.personal-menu__name>span')
      );
      return await userNameInHeader.getText();
    } catch (error) {
      await browser.waitForAngular();
      await element
        .all(by.css('div.personal-menu>span.personal-menu__name>span'))
        .then();
      console.error(`Error on search for name ${error.toString()}`);
      await e2eUtils.createScreenShot('LoginNotInHomePage', 'NOK');
      return 'wrong';
    }
  }

  async loginFailed(userName: string, password: string) {
    await this.fillLogin(userName, password);

    const loginButton = element(by.css('button.cta'));
    await loginButton.click();

    await browser.wait(
      ExpectedConditions.presenceOf(element(by.id('idLoginInError'))),
      12000
    );

    const countOfElements = await element.all(by.id('idLoginInError')).count();
    if (countOfElements > 0) {
      return element(by.id('idLoginInError')).getText();
    } else {
      return null;
    }
  }

  async fillLogin(userName: string, password: string) {
    const userNameEl = element(by.id('username'));
    await userNameEl.clear();
    await userNameEl.sendKeys(userName);
    const passwordEl = element(by.id('password'));
    await passwordEl.sendKeys(password);
  }

  async autoLogin(
    userName: string,
    password: string,
    displayName: string
  ): Promise<boolean> {
    await this.navigateTo();
    await this.goToLoginPage();
    const isAlreadyLogged = await e2eUtils.waitFor(
      element(by.css('div.personal-menu>span.personal-menu__name>span'))
    );
    if (!isAlreadyLogged) {
      await e2eUtils.createScreenShot('autoLogin', 'AfterLoginPage');
      await this.fillLogin(userName, password);
      const loginButton = element(by.css('button.cta'));
      await loginButton.click();

      try {
        await e2eUtils.waitFor(
          element(by.css('div.personal-menu>span.personal-menu__name>span'))
        );
        const userNameInHeader = element(
          by.css('div.personal-menu>span.personal-menu__name>span')
        );
        await expect(userNameInHeader).toBe(displayName);
        return true;
      } catch (error) {
        console.error(`Error on search for name ${error}`);
        await e2eUtils.createScreenShot('LoginNotInHomePage', 'NOK');
        return false;
      }
    } else {
      return true;
    }
  }

  getGroup(groupName: string): ElementArrayFinder {
    const lstLink = element.all(
      by.css('cbc-user-memberships>section>div.main>ul>li>a')
    );
    let result = null;
    result = lstLink.filter((item: ElementFinder) => {
      return item.getText().then((currentItem: string) => {
        return currentItem.localeCompare(groupName) === 0;
      });
    });
    return result;
  }

  async openLibrary() {
    const elFind = element(
      by.css('cbc-navigator>nav>div>ul>li>a.tab--library')
    );
    const elLibrary = await e2eUtils.waitFor(elFind);
    await elFind.click();
  }

  async checkMyGroups(group2Check: string): Promise<number> {
    const matchItem = this.getGroup(group2Check);
    return matchItem.count();
  }

  async checkOverviewOfAGroup(group2Check: string): Promise<boolean> {
    await this.getGroup(group2Check)
      .first()
      .click();
    await e2eUtils.waitFor(
      element(by.css('#description>cbc-timeline>article>span'))
    );
    const value: ElementFinder = element(
      by.css('#description>cbc-timeline>article>span')
    );
    return await value
      .getText()
      .then((textItem: string) =>
        textItem.toUpperCase().includes(group2Check.toUpperCase())
      );
  }

  async checkOverviewOfAGroupAndBack2Home(
    group2Check: string
  ): Promise<number> {
    await this.getGroup(group2Check)
      .first()
      .click();
    browser.navigate().back();
    await e2eUtils.waitFor(
      element(by.css('cbc-user-memberships>section>div.main>ul>li>a'))
    );
    const matchItem = this.getGroup(group2Check);
    return matchItem.count();
  }

  async OpenLibraryFromGroup(group2Check: string): Promise<boolean> {
    await this.getGroup(group2Check)
      .first()
      .click();
    await this.openLibrary();
    const elTitleLib = element(
      by.css('div.fluid>div.page-header>span.page-header__title')
    );
    await e2eUtils.waitFor(elTitleLib);

    return await elTitleLib
      .getText()
      .then(
        (title: string) =>
          title
            .toUpperCase()
            .localeCompare(
              'Stock, manage and share your documents'.toUpperCase()
            ) === 0
      );
  }

  async BrowseLibraryFromGroup(
    group2Check: string,
    browsePath: string[]
  ): Promise<boolean> {
    await this.getGroup(group2Check)
      .first()
      .click();
    await this.openLibrary();
    await e2eUtils.waitFor(
      element(by.css('div.fluid>div.page-header>span.page-header__title'))
    );

    let result = true;
    let waitClick = false;
    for (const elPath of browsePath) {
      const elLink = element.all(
        by.css(
          'cbc-library-browser>section.table-container>table.main td.cell-file-name>div.file-name>a'
        )
      );

      if (waitClick) {
        await e2eUtils.waitFor(
          element(
            by.css('cbc-library-browser>section.table-container>table.main')
          )
        );
      } else {
        waitClick = true;
      }

      const itemsFound = elLink.filter((el2Search: ElementFinder) =>
        el2Search
          .getText()
          .then((val2Search: string) => val2Search.localeCompare(elPath) === 0)
      );

      const nbrItem = await itemsFound
        .count()
        .then((valCount: number) => valCount);
      if (nbrItem === 1) {
        await itemsFound.first().click();
      } else {
        console.log(
          `Count on the library name ${elPath}  IS ${nbrItem} return false`
        );
        result = false;
        break;
      }
    }
    return result;
  }

  async CreateFolderInLibrary(
    group2Check: string,
    browsePath: string[],
    folderName: string
  ): Promise<E2eResult> {
    const browseLib = await this.BrowseLibraryFromGroup(
      group2Check,
      browsePath
    );
    if (browseLib) {
      await e2eUtils.waitFor(
        element(by.css('body>cbc-app>cbc-group>div>cbc-library>div.fluid.page-container>cbc-library-browser>section>table>tbody'))
      );
      const elLink = element.all(by.linkText(folderName));
      const nbrItem = await elLink.count().then((res: number) => res);
      if (nbrItem === 1) {
        return new E2eResult(
          false,
          'The Folder ' + folderName + ' already exist',
          ''
        );
      }

      const elMenu = element(by.css('cbc-add-dropdown>div.dropdown-trigger'));
      await elMenu.click();

      const subEl = element(by.id('createFolderInLib'));
      await subEl.click();

      // await e2eUtils.waitFor(element(by.css("cbc-add-space>cbc-modal")));
      const elDialog = element(by.css('cbc-add-space>cbc-modal'));

      const elFolderName = element(by.id('name'));
      await elFolderName.sendKeys(folderName);

      const elFolderTitle = element(by.css('input[placeholder=Title]'));
      // await elFolderTitle.sendKeys('Title for ' + folderName);
      await elFolderTitle.sendKeys( folderName);
      const elFolderDescription = element(
        by.css('cbc-multilingual-input div.ql-editor>p')
      );
      await browser.executeScript(
        "document.getElementsByClassName('cbc-multilingual-input div.ql-editor>p').innerHTML='Description for " +
          folderName +
          "';"
      );

      // await elFolderDescription.sendKeys("Description for " + folderName);
      const elCreate = element(
        by.css('section.modal-footer>div.buttons-group>a.cta')
      );
      await elCreate.click();

      await e2eUtils.waitFor(
        element(by.css('div.fluid>div.page-header>span.page-header__title'))
      );
      const elLink2Added = element.all(by.linkText(folderName));
      const nbrItemAdded = await elLink.count().then((res: number) => res);
      if (nbrItemAdded === 1) {
        return new E2eResult(true, '', '');
      } else {
        return new E2eResult(
          false,
          'The Folder ' + folderName + ' has not been created',
          ''
        );
      }
    } else {
      return new E2eResult(false, 'Problem when trying to get the Library', '');
    }
  }

  async RemoveFolderInLibrary(
    group2Check: string,
    browsePath: string[],
    folderName: string,
    checkExist: boolean
  ): Promise<E2eResult> {
    console.log('Start RemoveFolder ' + folderName);
    const browseLib = await this.BrowseLibraryFromGroup(
      group2Check,
      browsePath
    );
    if (browseLib) {
      await e2eUtils.waitFor(
        element(by.css('div.fluid>div.page-header>span.page-header__title'))
      );
      const elTable = element.all(
        by.css('cbc-library-browser>section.table-container>table.main tr.row')
      );
      const elLink = elTable.filter((rowItem: ElementFinder) => {
        return rowItem
          .element(by.css('td.cell-file-name>div.file-name>a'))
          .getText()
          .then((valTd: string) => {
            return valTd.localeCompare(folderName) === 0;
          });
      });
      const nbrItem = await elLink.count().then((res: number) => res);
      if (nbrItem !== 1) {
        if (checkExist) {
          console.log(`Folder found ${nbrItem} don't continue exit with false`);
          return new E2eResult(
            false,
            `The Folder ${folderName} is not existing`,
            ''
          );
        } else {
          console.log(` Folder found ${nbrItem} don't continue exit with true`);
          return new E2eResult(true, `there is ${nbrItem} folder(s) found`, '');
        }
      }
      const elFileLink = elLink.first().element(by.css('td.cell-file-name'));
      await elFileLink.getWebElement().then((val: WebElement) => {
        console.log('Start the mouse move on cell file name');
        browser.actions().mouseMove(val);
      });
      await e2eUtils.waitFor(
        element(
          by.css('td.cell-file-name>ul.actions>li>cbc-delete-action>a.delete')
        )
      );
      const elDelFolder = elFileLink.element(
        by.css('ul.actions>li>cbc-delete-action>a.delete')
      );
      await elDelFolder.click();
      await e2eUtils.waitFor(
        element(
          by.css(
            'cbc-delete-action>cbc-modal>section.modal>section.modal-footer>div.buttons-group>a.cta'
          )
        )
      );
      const elDeleteDial = element(
        by.css(
          'cbc-delete-action>cbc-modal>section.modal>section.modal-footer>div.buttons-group>a.cta'
        )
      );
      await elDeleteDial.click();
      return new E2eResult(true, '', '');
    } else {
      return new E2eResult(false, 'Problem when trying to get the Library', '');
    }
  }

  /**
   * Get a document or a folder from the list of element in the library / folder
   *
   * @param  file2Upload The File to be searched for
   */
  async CountElementFromLibrary(file2Upload: string): Promise<number> {
    const elTable = element.all(
      by.css(
        'cbc-library-browser>section.table-container>table.main td.cell-file-name>div.file-name>a'
      )
    );
    return elTable
      .filter((rowItem: ElementFinder) => {
        return rowItem.getText().then((valText: string) => {
          return valText === file2Upload;
        });
      })
      .count()
      .then((val: number) => val);
  }

  async GetCreatedElementFromLibrary(file2Upload: string): Promise<string> {
    const elTable = element.all(
      by.css('cbc-library-browser>section.table-container>table.main tr.row')
    );
    const file2Search = file2Upload.split('.');

    const matchExpression =
      file2Search[0] +
      '(([(]\\d+[)]).' +
      file2Search[1] +
      '|.' +
      file2Search[1] +
      ')*';
    console.log('This is the match string ' + matchExpression);

    let maxTime = 0;
    let lastElementTitle = '';
    let currentTitle = '';
    await elTable
      .filter((rowItem: ElementFinder) => {
        return rowItem
          .element(by.css('td.cell-file-name>div.file-name>a'))
          .getText()
          .then((valTd: string) => {
            const matches = valTd.match(matchExpression);
            return matches != null;
          });
      })
      .each(async (elFind: ElementFinder | undefined) => {
        if (elFind !== undefined) {
          const elTime = elFind.element(
            by.css('td.cell-last-modification>span.date')
          );
          const elTitle = elFind.element(
            by.css('td.cell-file-name>div.file-name>a')
          );
          currentTitle = await elTitle.getText();
          const time = await elTime.getText();
          const valTime = Date.parse(time);
          if (valTime > maxTime) {
            maxTime = valTime;
            lastElementTitle = currentTitle;
          }
        }
      });
    return lastElementTitle;
  }
}

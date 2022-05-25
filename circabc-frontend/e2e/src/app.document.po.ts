// tslint:disable:no-console
// tslint:disable:prefer-template
// tslint:disable:no-relative-imports
// tslint:disable:no-require-imports
// tslint:disable:max-func-body-length
// tslint:disable:no-floating-promises
// tslint:disable:promise-function-async
import {
  browser,
  by,
  element,
  ElementFinder,
  until,
  WebElement
} from 'protractor';
import * as e2eUtils from './app.e2e.utils';
import { WelcomePage } from './app.po';
import { E2eResult } from './e2eResult';

const welcome: WelcomePage = new WelcomePage();

export class DocumentPage {
  async CheckExistence(fileUploaded: string): Promise<E2eResult> {
    await e2eUtils.waitFor(
      element(by.css('div.fluid>div.page-header>span.page-header__title'))
    );
    const numberDocExisting = welcome.CountElementFromLibrary(fileUploaded);
    const resValue = await numberDocExisting.then((val: number) => val);

    if (resValue !== 1) {
      return new E2eResult(
        false,
        `Unable to retrieve the document searched for ${fileUploaded} ${resValue} occurence(s) found`,
        ''
      );
    } else {
      return new E2eResult(true, '', '');
    }
  }

  async Create(
    group2Check: string,
    browsePath: string[],
    file2Upload: string
  ): Promise<E2eResult> {
    const browseLib = await welcome.BrowseLibraryFromGroup(
      group2Check,
      browsePath
    );
    if (browseLib) {
      await e2eUtils.waitFor(
        element(
          by.css(
            'body>cbc-app>cbc-group>div>cbc-library>div.fluid.page-container>cbc-library-browser>section>table>tbody'
          )
        )
      );

      const numberDocExisting = welcome.CountElementFromLibrary(file2Upload);
      const resValue = await numberDocExisting.then((val: number) => val);
      const elMenu = element(by.css('cbc-add-dropdown>div.dropdown-trigger'));
      await elMenu.click();

      const subEl = element(by.id('createDocInLib'));
      await subEl.click();
      const elDialog = element(by.css('cbc-add-space>cbc-modal'));

      const elFileUpload = element(by.id('file'));
      const path = require('path');
      const absPath: string = path.resolve('./e2e/documentUpload.pdf');
      await elFileUpload.sendKeys(absPath);
      const elCreate = element(
        by.css('section.modal-footer>div.buttons-group>a.cta')
      );
      try {
        await elCreate.click();
      } catch (error) {
        console.log(`Error on uploading the document ${error}`);
      }

      let continueLoop = true;
      while (continueLoop) {
        try {
          await e2eUtils.waitFor(element(by.id('idButtonFinishUpload')));
          const elFinish = element(by.id('idButtonFinishUpload'));
          if (elFinish.isDisplayed()) {
            continueLoop = false;
            await elFinish.click();
          }
        } catch (error) {
          console.log(`waiting Exception is exit from while ${error}`);
          continueLoop = false;
        }
      }
      await e2eUtils.waitFor(element(by.css('cbc-ui-message-system')));
      await e2eUtils.waitFor(
        element(
          by.css('cbc-library-browser>section.table-container>table.main tr')
        )
      );

      const numberDocUploaded = await welcome
        .CountElementFromLibrary(file2Upload)
        .then((val: number) => val);
      const fileNameUploaded = await welcome.GetCreatedElementFromLibrary(
        file2Upload
      );

      if (numberDocUploaded === 1) {
        return new E2eResult(true, '', fileNameUploaded);
      } else {
        return new E2eResult(
          false,
          'The document ' + file2Upload + ' has not been uploaded',
          ''
        );
      }
    } else {
      return new E2eResult(false, 'Problem when trying to get the Library', '');
    }
  }

  async EditTitle(
    group2Check: string,
    browsePath: string[],
    fileUploaded: string,
    newTitles: string[][]
  ): Promise<E2eResult> {
    console.log('start edition of document ' + fileUploaded);
    const browseLib = await welcome.BrowseLibraryFromGroup(
      group2Check,
      browsePath
    );
    if (browseLib) {
      const resultCheck = await this.CheckExistence(fileUploaded);
      if (!resultCheck.result) {
        return resultCheck;
      }
      const elTable = element.all(
        by.css('cbc-library-browser>section.table-container>table.main tr.row')
      );
      const elUploaded = elTable.filter((rowItem: ElementFinder) => {
        return rowItem
          .element(by.css('td.cell-file-name>div.file-name>a'))
          .getText()
          .then((valTd: string) => {
            return valTd.localeCompare(fileUploaded) === 0;
          });
      });
      await elUploaded
        .first()
        .element(by.css('td.cell-file-name>div.file-name>a'))
        .click();
      await e2eUtils.waitFor(element(by.css('cbc-node-properties')));
      try {
        const elEdit = element(
          by.css('div.main>section.actions>ul.actions__main-actions>li>a.edit')
        );
        await elEdit.click();
      } catch (error) {
        await e2eUtils.createScreenShot('Edititle', 'ErrorOnClickEdit');
      }
      await e2eUtils.waitFor(element(by.css('cbc-edit-node')));

      // set the default title (EN)
      const elTitle = element(
        by.css(
          "section.file-informations>form>cbc-multilingual-input[formcontrolname='title']"
        )
      );
      const elTitleTextBox = elTitle.element(by.css('div.field>input'));

      console.log('Send Default EN title to text box ' + newTitles[0][1]);

      await elTitleTextBox.sendKeys(newTitles[0][1]);

      if (newTitles.length > 1) {
        const lgIcon = elTitle.all(by.css('div.translationPanel>ul a'));
        const resultLg = lgIcon.filter((currentLg: ElementFinder) =>
          currentLg.getText().then((val: string) => {
            const valReturn = newTitles.find(
              (itemLg: string[], idxLg: number, _arLg: string[][]) => {
                if (idxLg > 0) {
                  const isoLg = itemLg[0];
                  return itemLg[0].localeCompare(val) === 0;
                }
                return false;
              }
            );
            if (valReturn !== undefined) {
              return val.localeCompare(valReturn[0]) === 0;
            } else {
              return false;
            }
          })
        );
        await resultLg.each(
          async (currentLgItem: ElementFinder | undefined) => {
            if (currentLgItem !== undefined) {
              await currentLgItem.click();
            }
          }
        );

        newTitles.forEach(
          async (
            currentNewTitle: string[],
            _idx: number,
            _arrayLg: string[][]
          ) => {
            const elTextLg = element.all(
              by.css(
                'section.file-informations>form>' +
                  "cbc-multilingual-input[formcontrolname='title']>div.field>div.translationPanel>div>" +
                  "div.inputDisplay>input.inputTransation[name='" +
                  currentNewTitle[0].toLowerCase() +
                  "']"
              )
            );

            await elTextLg.each(async (elText: ElementFinder | undefined) => {
              if (elText !== undefined) {
                await elText.sendKeys(currentNewTitle[1]);
              }
            });
          }
        );
      }

      await e2eUtils.waitFor(element(by.css('cbc-edit-node')));
      const btnSave = element(by.css('div.buttons-group>a.cta'));
      await browser.executeScript(
        'arguments[0].scrollIntoView();',
        btnSave.getWebElement()
      );
      await e2eUtils.scrollToElement(btnSave.getWebElement());
      await btnSave.click();
      await e2eUtils.waitFor(element(by.css('cbc-node-properties')));
      return new E2eResult(true, '', fileUploaded + ' has been edited');
    } else {
      return new E2eResult(
        false,
        'Unable to edit the document ' + fileUploaded,
        ''
      );
    }
  }

  async DoAction(fileUploaded: string, actionName: string) {
    const elTable = element.all(
      by.css('cbc-library-browser>section.table-container>table.main tr.row')
    );
    const elUploaded = elTable.filter((rowItem: ElementFinder) => {
      return rowItem
        .element(by.css('td.cell-file-name>div.file-name>a'))
        .getText()
        .then((valTd: string) => {
          return valTd.localeCompare(fileUploaded) === 0;
        });
    });

    const count = await elUploaded.count();
    console.log(`the filter of uploaded document is ${count}`);
    const v = await elUploaded.getWebElements();
    browser.actions().mouseMove(v[0]);
    await e2eUtils.waitFor(
      element(by.css('td.cell-file-name>ul.actions>li>a'))
    );
    if (actionName.toLocaleLowerCase().localeCompare('delete') === 0) {
      const elPreview = elUploaded.all(
        by.css('td.cell-file-name>ul.actions>li>cbc-delete-action>a.delete')
      );
      await elPreview.click();
    } else {
      const elPreview = elUploaded.all(
        by.css('td.cell-file-name>ul.actions>li>a#' + actionName)
      );
      await elPreview.click();
    }
  }

  async Preview(
    group2Check: string,
    browsePath: string[],
    fileUploaded: string
  ): Promise<E2eResult> {
    console.log('start show preview of document ' + fileUploaded);
    const browseLib = await welcome.BrowseLibraryFromGroup(
      group2Check,
      browsePath
    );
    if (browseLib) {
      const resultCheck = await this.CheckExistence(fileUploaded);
      if (!resultCheck.result) {
        return resultCheck;
      }
      await this.DoAction(fileUploaded, 'previewContentId');
      await e2eUtils.waitFor(
        element(by.css('cbc-content-preview>cbc-modal>section.modal'))
      );
      return new E2eResult(true, '', '');
    } else {
      return new E2eResult(false, '', '');
    }
  }

  async Copy(
    group2Check: string,
    browsePath: string[],
    fileUploaded: string
  ): Promise<E2eResult> {
    console.log('start copy of document ' + fileUploaded);
    const browseLib = await welcome.BrowseLibraryFromGroup(
      group2Check,
      browsePath
    );
    if (browseLib) {
      const resultCheck = await this.CheckExistence(fileUploaded);
      if (!resultCheck.result) {
        return resultCheck;
      }
      await this.DoAction(fileUploaded, 'copyContentId');
      await e2eUtils.waitFor(element(by.css('cbc-library>span.info-badge')));
      const elClipboard = element(by.css('cbc-library>span.info-badge'));
      const nbrClipboard = await elClipboard.getText().then((v: string) => v);
      const checkClipboard = nbrClipboard.localeCompare('1') === 0;
      return new E2eResult(checkClipboard, '', '');
    } else {
      return new E2eResult(false, '', '');
    }
  }

  async Delete(
    group2Check: string,
    browsePath: string[],
    fileUploaded: string
  ): Promise<E2eResult> {
    console.log('start Deletion of document ' + fileUploaded);
    const browseLib = await welcome.BrowseLibraryFromGroup(
      group2Check,
      browsePath
    );
    if (browseLib) {
      const resultCheck = await this.CheckExistence(fileUploaded);
      if (!resultCheck.result) {
        return resultCheck;
      }
      await this.DoAction(fileUploaded, 'Delete');
      await e2eUtils.waitFor(element(by.css('cbc-modal>section.modal')));
      const elClipboard = element(
        by.css(
          'cbc-modal>section.modal>section.modal-footer>div.buttons-group>a.cta'
        )
      );
      await elClipboard.click();

      await e2eUtils.waitFor(
        element(by.css('div.fluid>div.page-header>span.page-header__title'))
      );
      const numberDocAfterDeletion = welcome.CountElementFromLibrary(
        fileUploaded
      );
      const resValueAfterDeletion = await numberDocAfterDeletion.then(
        (val: number) => val
      );
      let errorMsg = '';
      if (resValueAfterDeletion !== 0) {
        errorMsg = `Error on deletion of the document count item found ${resValueAfterDeletion}`;
      }
      return new E2eResult(resValueAfterDeletion === 0, errorMsg, '');
    } else {
      return new E2eResult(false, '', '');
    }
  }

  async OpenClipboard() {
    await e2eUtils.waitFor(element(by.css('cbc-library>div.half-oval>a')));
    const elClipboard = element(by.css('cbc-library>div.half-oval>a'));
    await elClipboard.click();
    await e2eUtils.waitFor(element(by.css('cbc-clipboard>div#theSidenav')));
  }

  async CloseClipboard(waitFinishWorking: boolean) {
    if (waitFinishWorking) {
      const valPresent = await element(by.id('clipboardSpinnerId')).isPresent();
      if (valPresent) {
        const spinnerEl = await browser.driver.wait(
          until.elementLocated(by.id('clipboardSpinnerId')),
          12000
        );

        await browser.driver.wait(until.elementIsVisible(spinnerEl), 12000);
      }
    }
    const elClose = element(by.css('cbc-clipboard>div#theSidenav>a.closebtn'));
    await elClose.click();
    await e2eUtils.waitFor(element(by.css('cbc-clipboard>div.close-sidenav')));
  }

  async CopyHereFromClipboard(
    group2Check: string,
    browsePath: string[],
    fileUploaded: string
  ): Promise<E2eResult> {
    console.log('start copy here from clipboard for document ' + fileUploaded);
    const browseLib = await welcome.BrowseLibraryFromGroup(
      group2Check,
      browsePath
    );
    if (browseLib) {
      const resultCheck = await this.CheckExistence(fileUploaded);
      if (!resultCheck.result) {
        return resultCheck;
      }
      await this.OpenClipboard();
      const elClipItems = element.all(
        by.css('cbc-clipboard>div#theSidenav>ul>li')
      );
      const elItem = elClipItems.filter((elClipItem: ElementFinder) => {
        return elClipItem
          .element(by.css('div.item-name'))
          .getText()
          .then((fileClipboard: string) => {
            console.log('The Document in clipboard is ' + fileClipboard);
            return fileClipboard.localeCompare(fileUploaded) === 0;
          });
      });
      await e2eUtils.waitFor(
        element(by.css('div.item-actions>div#copyItemFromClipboardId'))
      );
      const el2Click = elItem
        .first()
        .element(by.css('div.item-actions>div#copyItemFromClipboardId>a>img'));

      await e2eUtils.scrollToElement(el2Click.getWebElement());
      await el2Click.click();
      await this.CloseClipboard(true);

      this.CheckMessageShown();

      browser.driver
        .wait(until.elementLocated(by.css('cbc-ui-message-rendered')), 10000)
        .then((messageEl: WebElement) =>
          browser.driver.wait(until.elementIsVisible(messageEl), 10000)
        );

      await e2eUtils.waitFor(
        element(by.css('div.fluid>div.page-header>span.page-header__title'))
      );
      const copyFileName = 'Copy_' + fileUploaded;
      const numberDocCopyExisting = welcome.CountElementFromLibrary(
        copyFileName
      );
      const resValueCopy = await numberDocCopyExisting.then(
        (val: number) => val
      );

      if (resValueCopy !== 1) {
        return new E2eResult(
          false,
          `Unable to retrieve the document searched for ${copyFileName} ${resValueCopy} occurence(s) found`,
          ''
        );
      } else {
        return new E2eResult(true, '', '');
      }
    } else {
      return new E2eResult(false, '', '');
    }
  }

  private CheckMessageShown() {
    element(by.css('cbc-ui-message-rendered'))
      .isDisplayed()
      .then(
        (valMsg: boolean) => {
          if (!valMsg) {
            browser.driver
              .wait(
                until.elementLocated(by.css('cbc-ui-message-rendered')),
                12000
              )
              .then((messageEl: WebElement) =>
                browser.driver.wait(until.elementIsVisible(messageEl))
              );
          }
        },
        () => {
          return;
        }
      );
  }

  async LinkHereFromClipboard(
    group2Check: string,
    browsePath: string[],
    fileUploaded: string
  ): Promise<E2eResult> {
    console.log('start link here from clipboard for document ' + fileUploaded);
    const browseLib = await welcome.BrowseLibraryFromGroup(
      group2Check,
      browsePath
    );
    if (browseLib) {
      const resultCheck = await this.CheckExistence(fileUploaded);
      if (!resultCheck.result) {
        return resultCheck;
      }
      await this.OpenClipboard();
      const elClipItems = element.all(
        by.css('cbc-clipboard>div#theSidenav>ul>li')
      );
      const elItem = elClipItems.filter((elClipItem: ElementFinder) => {
        return elClipItem
          .element(by.css('div.item-name'))
          .getText()
          .then((fileClipboard: string) => {
            console.log('The Document in clipboard is ' + fileClipboard);
            return fileClipboard.localeCompare(fileUploaded) === 0;
          });
      });

      await e2eUtils.waitFor(
        element(by.css('div.item-actions>div#linkItemFromClipboardId'))
      );
      const el2Click = elItem
        .first()
        .element(by.css('div.item-actions>div#linkItemFromClipboardId>a'));
      await e2eUtils.scrollToElement(el2Click.getWebElement());
      await el2Click.click();
      await this.CloseClipboard(true);

      this.CheckMessageShown();

      browser.driver
        .wait(until.elementLocated(by.css('cbc-ui-message-rendered')), 10000)
        .then((messageEl: WebElement) =>
          browser.driver.wait(until.elementIsVisible(messageEl), 10000)
        );

      await e2eUtils.waitFor(
        element(by.css('div.fluid>div.page-header>span.page-header__title'))
      );
      const copyFileName = 'Link_' + fileUploaded + '.url';
      const numberDocCopyExisting = welcome.CountElementFromLibrary(
        copyFileName
      );
      const resValueCopy = await numberDocCopyExisting.then(
        (val: number) => val
      );

      if (resValueCopy !== 1) {
        return new E2eResult(
          false,
          `Unable to retrieve the document searched for ${copyFileName} ${resValueCopy} occurence(s) found`,
          ''
        );
      } else {
        return new E2eResult(true, '', '');
      }
    } else {
      return new E2eResult(false, '', '');
    }
  }

  async AddPremissionOfDocument(
    group2Check: string,
    browsePath: string[],
    fileUploaded: string,
    _systemPermission: string[][]
  ): Promise<E2eResult> {
    console.log('start AddPremissionOfDocument ' + fileUploaded);

    const browseLib = await welcome.BrowseLibraryFromGroup(
      group2Check,
      browsePath
    );
    if (browseLib) {
      const resultCheck = await this.CheckExistence(fileUploaded);
      if (!resultCheck.result) {
        return resultCheck;
      }
      await this.DoAction(fileUploaded, 'DetailContentId');
      await e2eUtils.waitFor(element(by.css('cbc-node-properties')));
      const elPermission = element(
        by.css(
          'div.page-header__actions>cbc-reponsive-sub-menu>section.menu-container>div.sub-menu>a:nth-child(2)'
        )
      );
      await elPermission.click();
      await e2eUtils.waitFor(element(by.css('cbc-permissions')));

      const elAddUser = element(by.css('div.page-header__actions>a.cta'));
      await elAddUser.click();
      await e2eUtils.waitFor(element(by.css('cbc-add-permissions')));
      const txtSearch = element(by.id('searchText'));
      await txtSearch.sendKeys('duyck');
      const btnSearch = element(by.id('search'));
      await btnSearch.click();
      await e2eUtils.waitFor(element(by.id('clearUserProfile')));

      const countItems = element.all(by.css('#selectMultiple option'));
      await countItems.first().click();

      const elAdd = element(by.id('addToList'));
      await elAdd.click();
      console.log('Click on AddToList');
      const lstPerm = element.all(by.css('#type option'));

      await lstPerm.last().click();
      console.log('Select user on list');
      const elAssign = element(by.id('assign'));
      console.log('Click on assign');
      await elAssign.click();
      const elAddToDoc = element(
        by.css('section.modal-footer>div.buttons-group>a.cta')
      );
      await elAddToDoc.click();
      console.log('Click on Add');
      await e2eUtils.waitFor(element(by.css('cbc-permissions')));
      return new E2eResult(true, '', '');
    } else {
      return new E2eResult(true, '', '');
    }
  }

  async CheckPremissionOfDocument(
    group2Check: string,
    browsePath: string[],
    fileUploaded: string,
    userName: string,
    permName: string
  ): Promise<E2eResult> {
    console.log('start CheckPremissionOfDocument ' + fileUploaded);
    const browseLib = await welcome.BrowseLibraryFromGroup(
      group2Check,
      browsePath
    );
    if (browseLib) {
      const resultCheck = await this.CheckExistence(fileUploaded);
      if (!resultCheck.result) {
        return resultCheck;
      }
      await this.DoAction(fileUploaded, 'DetailContentId');
      await e2eUtils.waitFor(element(by.css('cbc-node-properties')));
      const elPermission = element(
        by.css(
          'div.page-header__actions>cbc-reponsive-sub-menu>section.menu-container>div.sub-menu>a:nth-child(2)'
        )
      );
      await elPermission.click();
      await e2eUtils.waitFor(element(by.css('cbc-permissions')));

      const elTablesPerm = element.all(
        by.css(
          '#usersPermissionTableId>div.table-container>table.main>tbody tr.row'
        )
      );
      const elFind = elTablesPerm.filter((elSearch: ElementFinder) => {
        return elSearch
          .element(by.css('td:nth-child(3)'))
          .getText()
          .then((vName: string) => {
            return vName.localeCompare(userName) === 0;
          });
      });

      const elResultFind = elFind.filter((elSearch: ElementFinder) => {
        return elSearch
          .element(by.css('td:nth-child(4)'))
          .getText()
          .then((vName: string) => {
            return vName.localeCompare(permName) === 0;
          });
      });
      const resultNumber = await elResultFind.count();
      return new E2eResult(resultNumber === 1, '', '');
    } else {
      return new E2eResult(true, '', '');
    }
  }

  async RemovePremissionOfDocument(
    group2Check: string,
    browsePath: string[],
    fileUploaded: string,
    userName: string
  ): Promise<E2eResult> {
    console.log('start RemovePremissionOfDocument ' + fileUploaded);
    const browseLib = await welcome.BrowseLibraryFromGroup(
      group2Check,
      browsePath
    );
    if (browseLib) {
      const resultCheck = await this.CheckExistence(fileUploaded);
      if (!resultCheck.result) {
        return resultCheck;
      }
      await this.DoAction(fileUploaded, 'DetailContentId');
      await e2eUtils.waitFor(element(by.css('cbc-node-properties')));
      const elPermission = element(
        by.css(
          'div.page-header__actions>cbc-reponsive-sub-menu>section.menu-container>div.sub-menu>a:nth-child(2)'
        )
      );
      await elPermission.click();
      await e2eUtils.waitFor(element(by.css('cbc-permissions')));
      await e2eUtils.waitFor(element(by.id('cmn-toggle-1')));
      const chkPerm = element(by.id('cmn-toggle-1'));
      // const chkPerm = element(by.css("cbc-permissions>div.wrap>section>div.main>div.field>input"));
      await e2eUtils.scrollToElement(chkPerm.getWebElement());

      const isDisplayed = await chkPerm.isDisplayed();
      if (!isDisplayed) {
        console.log('RemovePermission-Click with Java Script');
        await browser.executeScript(
          "document.getElementById('cmn-toggle-1').style.visibility='visible';" +
            "document.getElementById('cmn-toggle-1').click()"
        );
      }
      await e2eUtils.waitFor(element(by.id('changePermNo')));
      const elTablesPerm = element.all(
        by.css(
          '#usersPermissionTableId>div.table-container>table.main>tbody tr.row'
        )
      );
      const elFind = elTablesPerm.filter((elSearch: ElementFinder) => {
        return elSearch
          .element(by.css('td:nth-child(3)'))
          .getText()
          .then((vName: string) => {
            return vName.localeCompare(userName) === 0;
          });
      });

      const elDelete = elFind
        .first()
        .element(by.css('td:nth-child(5)>cbc-inline-delete>a'));
      await elDelete.click();
      await e2eUtils.waitFor(element(by.id('confirmDeletePerm')));
      const elDeleteYes = elFind.first().element(by.id('confirmDeletePerm'));
      await elDeleteYes.click();

      await e2eUtils.waitFor(element(by.css('cbc-permissions')));

      const elTablesPermNew = element.all(
        by.css(
          '#usersPermissionTableId>div.table-container>table.main>tbody tr.row'
        )
      );
      const elFindNew = elTablesPermNew.filter((elSearch: ElementFinder) => {
        return elSearch
          .element(by.css('td:nth-child(3)'))
          .getText()
          .then((vName: string) => {
            return vName.localeCompare(userName) === 0;
          });
      });

      const resultNumber = await elFindNew.count();
      return new E2eResult(resultNumber === 0, '', '');
    } else {
      return new E2eResult(true, '', '');
    }
  }

  async CheckOutDocument(
    group2Check: string,
    browsePath: string[],
    fileUploaded: string
  ): Promise<E2eResult> {
    console.log('start CheckOutDocument ' + fileUploaded);
    const browseLib = await welcome.BrowseLibraryFromGroup(
      group2Check,
      browsePath
    );
    if (browseLib) {
      const resultCheck = await this.CheckExistence(fileUploaded);
      if (!resultCheck.result) {
        return resultCheck;
      }
      await this.DoAction(fileUploaded, 'DetailContentId');
      await e2eUtils.waitFor(element(by.css('cbc-node-properties')));
      // await e2eUtils.createScreenShot("CheckOutDocument", "BeforeShowMoreMenu");
      const elMore = element(
        by.css(
          'div.main>section.actions>ul.actions__secondary-actions--left>li.dropdown-trigger>a.more'
        )
      );
      await elMore.click();
      // e2eUtils.createScreenShot("CheckOutDocument", "ShowMoreMenu");
      await e2eUtils.waitFor(element(by.css('cbc-node-properties')));
      const elCheckout = element(
        by.css(
          'div.main>section.actions>ul.actions__secondary-actions--left>' +
            'li.dropdown-trigger>ul.dropdown>span>li>a.checkout'
        )
      );
      // e2eUtils.createScreenShot("CheckOutDocument", "SelectMenu");
      await elCheckout.click();
      await e2eUtils.waitFor(element(by.id('lockedContentId')));
      // e2eUtils.createScreenShot("CheckOutDocument", "CheckOutDone");

      return new E2eResult(true, '', '');
    } else {
      return new E2eResult(true, '', '');
    }
  }
  async CancelCheckOutDocument(
    group2Check: string,
    browsePath: string[],
    fileUploaded: string
  ): Promise<E2eResult> {
    console.log('start CheckOutDocument ' + fileUploaded);
    const browseLib = await welcome.BrowseLibraryFromGroup(
      group2Check,
      browsePath
    );
    if (browseLib) {
      const resultCheck = await this.CheckExistence(fileUploaded);
      if (!resultCheck.result) {
        return resultCheck;
      }
      await this.DoAction(fileUploaded, 'DetailContentId');
      await e2eUtils.waitFor(
        element(
          by.css(
            'body>cbc-app>cbc-group>div>cbc-node-properties>div>div>div>' +
              'section:nth-child(2)>div>section.actions>ul.actions__secondary-actions--left>li:nth-child(2)>a'
          )
        )
      );
      const elCancelCheckout = element(
        by.css(
          'body>cbc-app>cbc-group>div>cbc-node-properties>div>div>div>' +
            'section:nth-child(2)>div>section.actions>ul.actions__secondary-actions--left>li:nth-child(2)>a'
        )
      );
      await elCancelCheckout.click();
      await e2eUtils.waitFor(
        element(
          by.css(
            'body>cbc-app>cbc-group>div>cbc-node-properties>div>div>div>' +
              'section:nth-child(2)>div>cbc-cancel-checkout>cbc-modal>section>section.modal-footer>div>a.cta'
          )
        )
      );

      const elAccept = element(
        by.css(
          'body>cbc-app>cbc-group>div>cbc-node-properties>div>div>div>' +
            'section:nth-child(2)>div>cbc-cancel-checkout>cbc-modal>section>section.modal-footer>div>a.cta'
        )
      );
      await elAccept.click();
      return new E2eResult(true, '', '');
    } else {
      return new E2eResult(true, '', '');
    }
  }
  async CheckInDocument(
    group2Check: string,
    browsePath: string[],
    fileUploaded: string
  ): Promise<E2eResult> {
    console.log('start CheckOutDocument ' + fileUploaded);
    const browseLib = await welcome.BrowseLibraryFromGroup(
      group2Check,
      browsePath
    );
    if (browseLib) {
      const resultCheck = await this.CheckExistence(fileUploaded);
      if (!resultCheck.result) {
        return resultCheck;
      }
      await this.DoAction(fileUploaded, 'DetailContentId');
      await e2eUtils.waitFor(
        element(
          by.css(
            'body>cbc-app>cbc-group>div>cbc-node-properties>div>div>div>' +
              'section:nth-child(2)>div>section.actions>ul.actions__secondary-actions--left>li:nth-child(1)>a'
          )
        )
      );
      const elCancelCheckout = element(
        by.css(
          'body>cbc-app>cbc-group>div>cbc-node-properties>div>div>div>' +
            'section:nth-child(2)>div>section.actions>ul.actions__secondary-actions--left>li:nth-child(1)>a'
        )
      );
      await elCancelCheckout.click();
      await e2eUtils.waitFor(
        element(
          by.css(
            'body>cbc-app>cbc-group>div>cbc-node-properties>div>div>div>' +
              'section:nth-child(2)>div>cbc-checkin>cbc-modal>section>section.modal-footer>div>a.cta'
          )
        )
      );

      const elAccept = element(
        by.css(
          'body>cbc-app>cbc-group>div>cbc-node-properties>div>div>div>' +
            'section:nth-child(2)>div>cbc-checkin>cbc-modal>section>section.modal-footer>div>a.cta'
        )
      );
      await elAccept.click();
      return new E2eResult(true, '', '');
    } else {
      return new E2eResult(true, '', '');
    }
  }
}

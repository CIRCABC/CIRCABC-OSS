// tslint:disable:no-console prefer-template no-relative-imports no-require-imports  max-func-body-length
import { DocumentPage } from './app.document.po';
import { WelcomePage } from './app.po';

// const interestGroupName: string = 'Cours de printemps';
const interestGroupName = 'Cours de printemps';
// const interestGroupName: string = 'Interest Group 111';
// const interestGroupName: string = 'e2e-old-access';
// const interestGroupName: string = 'oldig1n';

const folderName = 'e2eTest';

const userName = 'beaurpi';

const password = '';

const fullNameLowerCase = 'pierre beauregard';

const fullName = 'Pierre BEAUREGARD';

const subFolderName = 'MyFolder3';

const newFolderName = 'MyFolder5';

describe('browse CircaBC', () => {
  let page: WelcomePage;
  let documentPage: DocumentPage;
  let documentNameUploaded = 'documentUpload.pdf';

  beforeEach(() => {
    page = new WelcomePage();
    documentPage = new DocumentPage();
  });

  it('Workspace - title should be Circabc From Browse', async () => {
    const loginOk = await page.autoLogin(
      userName,
      password,
      fullNameLowerCase
    );
    await expect(loginOk).toBe(true);
  });

  it('Workspace - Check that My Groups has the good informations', async () => {
    const loginOk = await page.autoLogin(
      userName,
      password,
      fullNameLowerCase
    );
    const v = await page.checkMyGroups(interestGroupName);
    await expect(v).toBe(1);
  });

  it('Workspace - Check that I can see overview of Interest Groups', async () => {
    const loginOk = await page.autoLogin(
      userName,
      password,
      fullNameLowerCase
    );
    const v = await page.checkOverviewOfAGroup(interestGroupName);
    await expect(v).toBe(true);
  });

  it('Workspace - Go to one Interest Groups and go back to home page', async () => {
    const loginOk = await page.autoLogin(
      userName,
      password,
      fullNameLowerCase
    );
    const v = await page.checkOverviewOfAGroupAndBack2Home(
      interestGroupName
    );
    await expect(v).toBe(1);
  });

  it('Workspace - Open library from an Interest Group', async () => {
    const loginOk = await page.autoLogin(
      userName,
      password,
      fullNameLowerCase
    );
    const v = await page.OpenLibraryFromGroup(interestGroupName);
    await expect(v).toBe(true);
  });

  it('Workspace - Browse the library', async () => {
    const loginOk = await page.autoLogin(
      userName,
      password,
      fullNameLowerCase
    );
    const v = await page.BrowseLibraryFromGroup(interestGroupName, [
      folderName
    ]);
    await expect(v).toBe(true);
  });

  it('Workspace - Remove the folder that will be created in the library', async () => {
    const loginOk = await page.autoLogin(
      userName,
      password,
      fullNameLowerCase
    );
    const v = await page.RemoveFolderInLibrary(
      interestGroupName,
      [folderName],
      newFolderName,
      false
    );
    await expect(v.result).toBe(true, v.comment);
  });

  it('Workspace - Create a folder in the library', async () => {
    const loginOk = await page.autoLogin(
      userName,
      password,
      fullNameLowerCase
    );
    const v = await page.CreateFolderInLibrary(
      interestGroupName,
      [folderName],
      newFolderName
    );
    await expect(v.result).toBe(true, v.comment);
  });

  it('Workspace - Remove the folder just created in the library', async () => {
    const loginOk = await page.autoLogin(
      userName,
      password,
      fullNameLowerCase
    );
    const v = await page.RemoveFolderInLibrary(
      interestGroupName,
      [folderName],
      newFolderName,
      true
    );
    await expect(v.result).toBe(true, v.comment);
  });

  it('Workspace - Create a document in the library', async () => {
    const loginOk = await page.autoLogin(
      userName,
      password,
      fullNameLowerCase
    );
    const v = await documentPage.Create(
      interestGroupName,
      [folderName, subFolderName],
      'documentUpload.pdf'
    );
    if (v.result && v.returnData !== '') {
      documentNameUploaded = v.returnData;
    }
    await expect(v.result).toBe(true, v.comment);
  });

  it('Workspace - Edit titles in the uploaded document in the library', async () => {
    const loginOk = await page.autoLogin(
      userName,
      password,
      fullNameLowerCase
    );
    const titlesValues: string[][] = [
      ['EN', 'DefaultTitle'],
      ['FR', 'TitleFR'],
      ['DE', 'TitleDE']
    ];
    const v = await documentPage.EditTitle(
      interestGroupName,
      [folderName, subFolderName],
      documentNameUploaded,
      titlesValues
    );
    await expect(v.result).toBe(true, v.comment);
  });

  it('Workspace - Preview the uploaded document in the library', async () => {
    const loginOk = await page.autoLogin(
      userName,
      password,
      fullNameLowerCase
    );
    const v = await documentPage.Preview(
      interestGroupName,
      [folderName, subFolderName],
      documentNameUploaded
    );
    await expect(v.result).toBe(true, v.comment);
  });

  it('Workspace - Copy the uploaded document in the clipboard', async () => {
    const loginOk = await page.autoLogin(
      userName,
      password,
      fullNameLowerCase
    );
    const v = await documentPage.Copy(
      interestGroupName,
      [folderName, subFolderName],
      documentNameUploaded
    );
    await expect(v.result).toBe(true, v.comment);
  });

  it('Workspace - Copy here the uploaded document from the clipboard', async () => {
    const loginOk = await page.autoLogin(
      userName,
      password,
      fullNameLowerCase
    );
    const v = await documentPage.CopyHereFromClipboard(
      interestGroupName,
      [folderName, subFolderName],
      documentNameUploaded
    );
    await expect(v.result).toBe(true, v.comment);
  });

  it('Workspace - Link here the uploaded document from the clipboard', async () => {
    const loginOk = await page.autoLogin(
      userName,
      password,
      fullNameLowerCase
    );
    const v = await documentPage.LinkHereFromClipboard(
      interestGroupName,
      [folderName, subFolderName],
      documentNameUploaded
    );
    await expect(v.result).toBe(true, v.comment);
  });

  it('Workspace - Add a permission to the document', async () => {
    const loginOk = await page.autoLogin(
      userName,
      password,
      fullNameLowerCase
    );
    const permValues: string[][] = [
      ['Reviewer', 'Edit Only'],
      ['Contributor', 'Access'],
      ['Registered', 'No Access dd'],
      ['Leader', 'Administer']
    ];
    const v = await documentPage.AddPremissionOfDocument(
      interestGroupName,
      [folderName, subFolderName],
      documentNameUploaded,
      permValues
    );
    await expect(v.result).toBe(true, v.comment);
  });

  it('Workspace - Check the added permission to the document', async () => {
    const loginOk = await page.autoLogin(
      userName,
      password,
      fullNameLowerCase
    );
    const v = await documentPage.CheckPremissionOfDocument(
      interestGroupName,
      [folderName, subFolderName],
      documentNameUploaded,
      fullName,
      'Administer'
    );
    await expect(v.result).toBe(true, v.comment);
  });

  it('Workspace - Remove the added permission to the document', async () => {
    const loginOk = await page.autoLogin(
      userName,
      password,
      fullNameLowerCase
    );
    const v = await documentPage.RemovePremissionOfDocument(
      interestGroupName,
      [folderName, subFolderName],
      documentNameUploaded,
      fullName
    );
    await expect(v.result).toBe(true, v.comment);
  });

  it('Workspace - Check out the document', async () => {
    const loginOk = await page.autoLogin(
      userName,
      password,
      fullNameLowerCase
    );
    const v = await documentPage.CheckOutDocument(
      interestGroupName,
      [folderName, subFolderName],
      documentNameUploaded
    );
    await expect(v.result).toBe(true, v.comment);
  });
  it('Workspace - Cancel check out the document', async () => {
      const loginOk = await page.autoLogin(
        userName,
        password,
        fullNameLowerCase
      );
      const v = await documentPage.CancelCheckOutDocument(
        interestGroupName,
        [folderName, subFolderName],
        documentNameUploaded.replace('.pdf', ' (Working Copy).pdf')
      );
      await expect(v.result).toBe(true, v.comment);
    });
  it('Workspace - Second Check out the document', async () => {
    const loginOk = await page.autoLogin(
      userName,
      password,
      fullNameLowerCase
    );
    const v = await documentPage.CheckOutDocument(
      interestGroupName,
      [folderName, subFolderName],
      documentNameUploaded
    );
    await expect(v.result).toBe(true, v.comment);
  });
  it('Workspace - Check in the document', async () => {
      const loginOk = await page.autoLogin(
        userName,
        password,
        fullNameLowerCase
      );
      const v = await documentPage.CheckInDocument(
        interestGroupName,
        [folderName, subFolderName],
        documentNameUploaded.replace('.pdf', ' (Working Copy).pdf')
      );
      await expect(v.result).toBe(true, v.comment);
    });
});

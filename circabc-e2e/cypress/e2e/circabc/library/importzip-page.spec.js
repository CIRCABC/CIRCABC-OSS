describe('The importzip Page', function () {
  beforeEach(function () {
    cy.login(
      Cypress.env('interest.group.admin.username'),
      Cypress.env('interest.group.admin.password'),
    );
  });
  it('successfully import zip file', function () {
    cy.visit('me/roles', {
      failOnStatusCode: false,
    });
    cy.contains(Cypress.env('interest.group.title')).click();
    cy.get('[data-cy=library]').should('be.visible').click();
    cy.get('[data-cy=add]').click();
    cy.get('.import').click();

    const fileName = 'cypress/fixtures/files/ImportZipTest.zip';

    cy.get('[id="file"]').selectFile(fileName, { force: true });

    cy.get('.modal-footer > .buttons-group > .cta').click();
    cy.contains('Success');
  });
});

afterEach(function () {
  cy.checkA11yWithLogging(undefined, undefined);
});

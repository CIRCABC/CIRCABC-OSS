describe('Test FTP Connection', function () {
  beforeEach(function () {
    cy.login(
      Cypress.env('interest.group.admin.username'),
      Cypress.env('interest.group.admin.password'),
    );
  });
  it('successfully test FTP connection', function () {
    cy.visit('me/roles', {
      failOnStatusCode: false,
    });
    cy.contains(Cypress.env('interest.group.title')).click();
    cy.get('[data-cy="admin"]').should('be.visible').click();
    cy.get('[data-cy="autoupload-tab"]').should('be.visible').click();
    cy.get('[data-cy="add-configuration"]').should('be.visible').click();
    cy.get('#ftpHost').type('ftp');
    cy.get('#ftpPort').type('21');
    //cy.get('#pathToFile').type("");
    cy.get('#username').type('one');
    cy.get('#password').type('1234');
    cy.get('[data-cy="test-connection"]').should('be.visible').click();
    cy.contains('Success');
  });
});

afterEach(function () {
  cy.checkA11yWithLogging(undefined, undefined);
});

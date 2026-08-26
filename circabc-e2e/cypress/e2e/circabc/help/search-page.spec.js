describe('The Search Page', function () {
  beforeEach(function () {
    cy.login(Cypress.env('admin.username'), Cypress.env('admin.password'));
  });

  it('successfully create help section', function () {
    // give chance alfresco indexing
    cy.wait(20000);
    cy.visit('help/start', {
      failOnStatusCode: false,
    });
    cy.get('.search-bar').click();
    cy.get('.search-bar').type('Link1');
    cy.get('.links-list > h2').should('have.length', 1);
  });
});
afterEach(function () {
  cy.checkA11yWithLogging(undefined, undefined);
});

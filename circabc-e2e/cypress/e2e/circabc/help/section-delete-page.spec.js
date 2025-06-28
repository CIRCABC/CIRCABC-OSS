describe('The Delete Help Section Page', function () {
  beforeEach(function () {
    cy.login(Cypress.env('admin.username'), Cypress.env('admin.password'));
  });

  it('successfully delete help section', function () {
    cy.visit('help/start', {
      failOnStatusCode: false,
    });
    cy.wait(5000);
    cy.get('.help-categories > ul > :nth-child(1) > a').click();
    cy.get('.selected > a').click();
    cy.get('.actions > :nth-child(2)').click();
    cy.get('[data-cy="ok"]').click();
    cy.contains('Success');
  });
});
afterEach(function () {
  cy.checkA11yWithLogging(undefined, undefined);
});

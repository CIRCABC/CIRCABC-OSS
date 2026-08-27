describe('The Delete Help Section Page', function () {
  beforeEach(function () {
    cy.login(Cypress.env('admin.username'), Cypress.env('admin.password'));
  });

  it('successfully delete help section', function () {
    cy.visit('help/start', {
      failOnStatusCode: false,
    });
    cy.wait(5000);
    cy.get('[data-cy="category-header"]').first().click();
    cy.get('[data-cy="delete-section"]').first().click();
    cy.get('[data-cy="ok"]').click();
    cy.contains('Success');
  });
});
afterEach(function () {
  cy.checkA11yWithLogging(undefined, undefined);
});

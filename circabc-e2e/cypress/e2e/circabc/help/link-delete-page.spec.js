describe('The Create Help Section Page', function () {
  beforeEach(function () {
    cy.login(Cypress.env('admin.username'), Cypress.env('admin.password'));
  });

  it('successfully create help section', function () {
    cy.visit('help/start', {
      failOnStatusCode: false,
    });
    cy.get('[data-cy="delete-inline"]').first().click();
    cy.get('[data-cy="delete-inline-confirm"]').click();
    cy.contains('Success');
  });

  afterEach(function () {
    cy.checkA11yWithLogging(undefined, undefined);
  });
});

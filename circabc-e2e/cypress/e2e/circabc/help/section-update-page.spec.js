describe('The Update Help Section Page', function () {
  beforeEach(function () {
    cy.login(Cypress.env('admin.username'), Cypress.env('admin.password'));
  });

  it('successfully update help section', function () {
    cy.visit('help/start', {
      failOnStatusCode: false,
    });

    cy.get('[data-cy="category-header"]').first().click();
    cy.get('[data-cy="edit-section"]').first().click();
    cy.get('[data-cy="text"]').type(' updated');
    cy.get('[data-cy="ok"]').click();

    cy.contains('Success');
  });

  afterEach(function () {
    cy.checkA11yWithLogging(undefined, undefined);
  });
});

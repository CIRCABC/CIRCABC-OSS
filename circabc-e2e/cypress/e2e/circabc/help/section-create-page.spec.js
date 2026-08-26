describe('The Create Help Section Page', function () {
  beforeEach(function () {
    cy.login(Cypress.env('admin.username'), Cypress.env('admin.password'));
  });

  it('successfully create help section', function () {
    cy.visit('help/start', {
      failOnStatusCode: false,
    });
    cy.contains('Add section').click();
    cy.get('[data-cy=section]').should('be.visible').click();
    cy.get('[data-cy="text"]').type('Section1');
    cy.get('[data-cy="ok"]').click();
    cy.contains('Success');
  });

  afterEach(function () {
    cy.checkA11yWithLogging(undefined, undefined);
  });
});

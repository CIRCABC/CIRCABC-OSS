describe('The Create Help Section Page', function () {
  beforeEach(function () {
    cy.login(Cypress.env('admin.username'), Cypress.env('admin.password'));
  });

  it('successfully create help section', function () {
    cy.visit('help/start', {
      failOnStatusCode: false,
    });
    cy.get(
      ':nth-child(1) > .actions > cbc-inline-delete > .question > .ng-star-inserted',
    ).click();
    cy.get('#confirmDeletePerm').click();
    cy.contains('Success');
  });

  afterEach(function () {
    cy.checkA11yWithLogging(undefined, undefined);
  });
});

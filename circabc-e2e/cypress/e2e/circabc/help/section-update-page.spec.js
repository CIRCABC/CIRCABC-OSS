describe('The Update Help Section Page', function () {
  beforeEach(function () {
    cy.login(Cypress.env('admin.username'), Cypress.env('admin.password'));
  });

  it('successfully update help section', function () {
    cy.visit('help/start', {
      failOnStatusCode: false,
    });

    cy.get('.help-categories > ul > .ng-star-inserted > a').click();
    cy.get('.article-list > .actions > :nth-child(1)').click();
    cy.get('[data-cy="text"]').type(' updated');
    cy.get('[data-cy="ok"]').click();

    cy.contains('Success');
  });

  afterEach(function () {
    cy.checkA11yWithLogging(undefined, undefined);
  });
});

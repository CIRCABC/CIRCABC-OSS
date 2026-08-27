describe('The Delete Help Article Page', function () {
  beforeEach(function () {
    cy.login(Cypress.env('admin.username'), Cypress.env('admin.password'));
  });

  it('successfully Delete Help Article', function () {
    cy.visit('help/start').contains('Section1');
    cy.get('[data-cy="category-header"]').first().click();
    cy.get('[data-cy="article-item"]').first().click();
    cy.get('[data-cy="delete-article"]').click();
    cy.get('[data-cy="ok"]').click();
    cy.contains('Success');
  });
});
afterEach(function () {
  cy.checkA11yWithLogging(undefined, undefined);
});

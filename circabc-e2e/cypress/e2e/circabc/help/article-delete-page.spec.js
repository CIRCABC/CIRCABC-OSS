describe('The Delete Help Article Page', function () {
  beforeEach(function () {
    cy.login(Cypress.env('admin.username'), Cypress.env('admin.password'));
  });

  it('successfully Delete Help Article', function () {
    cy.visit('help/start').contains('Section1');
    cy.get('ul > .ng-star-inserted > a').click();
    cy.get('.article-list > ul > :nth-child(1) > a').click();
    cy.get('.actions > :nth-child(3)').click();
    cy.get('[data-cy="ok"]').click();
    cy.contains('Success');
  });
});
afterEach(function () {
  cy.checkA11yWithLogging(undefined, undefined);
});

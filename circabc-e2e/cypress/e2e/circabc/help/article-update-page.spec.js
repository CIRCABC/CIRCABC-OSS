describe('The Update Help Article Page', function () {
  beforeEach(function () {
    cy.login(Cypress.env('admin.username'), Cypress.env('admin.password'));
  });

  it('successfully Update Help Article', function () {
    cy.visit('help/start').contains('Section1');
    cy.get('[data-cy="category-header"]').first().click();
    cy.get('[data-cy="article-item"]').first().click();
    cy.get('[data-cy="edit-article"]').click();
    cy.get('[data-cy="text"]').type(' updated');
    cy.get(
      '.modal-content > form.ng-untouched > .field > .custom-select > .ng-untouched',
    ).select('es');
    cy.get('.ql-editor').type(' updated');
    cy.get('[data-cy="ok"]').click();
    cy.contains('Article updated');
  });
});
afterEach(function () {
  cy.checkA11yWithLogging(undefined, undefined);
});

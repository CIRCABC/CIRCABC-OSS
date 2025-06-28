describe('The Upload Logo Page', function () {
  beforeEach(function () {
    cy.login(
      Cypress.env('category.admin.username'),
      Cypress.env('category.admin.password'),
    );
  });
  it('successfully upload file', function () {
    cy.visit('explore', {
      failOnStatusCode: false,
    });
    cy.get('.header-category').click();
    cy.get('.header-category').click();
    cy.get('.sub-menu > .ng-star-inserted').click();
    cy.get('.header > :nth-child(3) > .tab').click();
    cy.get('cbc-category-customisation.ng-star-inserted > .cta').click();
    const fileName = 'cypress/fixtures/files/logoCategory.png';
    cy.get('[data-cy="file-input"]').selectFile(fileName, { force: true });
    cy.get('[data-cy="ok"]').click();
    cy.contains('Success');
  });

  afterEach(function () {
    cy.checkA11yWithLogging(undefined, undefined);
  });
});

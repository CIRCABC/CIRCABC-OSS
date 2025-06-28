describe('Delete Logo Page', function () {
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
    cy.get(
      ':nth-child(2) > :nth-child(3) > cbc-inline-delete > .question > .ng-star-inserted',
    ).click();
    cy.get('#confirmDeletePerm').click();
    cy.contains('Success');
  });

  afterEach(function () {
    cy.checkA11yWithLogging(undefined, undefined);
  });
});

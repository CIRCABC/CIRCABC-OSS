describe('The Delete Topic Page', function () {
  beforeEach(function () {
    cy.login(
      Cypress.env('interest.group.admin.username'),
      Cypress.env('interest.group.admin.password'),
    );
  });
  it('successfully delete topic', function () {
    cy.visit('me/roles', {
      failOnStatusCode: false,
    });
    cy.contains(Cypress.env('interest.group.title')).click();
    cy.get('[data-cy=forums]').should('be.visible').click();
    cy.contains('Topic1').trigger('mouseover');
    cy.contains('Delete').click();
    cy.get('.cta').contains('Delete').click();

    cy.contains('Success');
    cy.contains('Close').click();
  });

  afterEach(function () {
    cy.checkA11yWithLogging(undefined, undefined);
  });
});

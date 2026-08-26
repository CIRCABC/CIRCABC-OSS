describe('The Create Forum Page', function () {
  beforeEach(function () {
    cy.login(
      Cypress.env('interest.group.admin.username'),
      Cypress.env('interest.group.admin.password'),
    );
  });
  it('successfully create forum', function () {
    cy.visit('me/roles', {
      failOnStatusCode: false,
    });
    cy.contains(Cypress.env('interest.group.title')).click();
    cy.get('[data-cy=forums]').should('be.visible').click();
    cy.get('[data-cy=add]').click();
    cy.get('[data-cy=create-forum]').click();
    cy.get('[data-cy="text"]').type('Forum1');
    cy.get('[data-cy="textarea"]').type('Forum1 Description');
    cy.get('[data-cy="ok"]').click();
  });

  afterEach(function () {
    cy.checkA11yWithLogging(undefined, undefined);
  });
});

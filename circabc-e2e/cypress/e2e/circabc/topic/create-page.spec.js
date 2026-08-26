describe('The Create Topic Page', function () {
  beforeEach(function () {
    cy.login(
      Cypress.env('interest.group.admin.username'),
      Cypress.env('interest.group.admin.password'),
    );
  });
  it('successfully create topic', function () {
    cy.visit('me/roles', {
      failOnStatusCode: false,
    });
    cy.contains(Cypress.env('interest.group.title')).click();
    cy.get('[data-cy=forums]').should('be.visible').click();
    cy.get('[data-cy=add]').click();
    cy.get('[data-cy=create-topic]').click();
    cy.get('[data-cy="text"]').type('Topic1');
    cy.get('[data-cy="textarea"]').type('Topic1 Description');
    cy.get('[data-cy="ok"]').click();
  });

  afterEach(function () {
    cy.checkA11yWithLogging(undefined, undefined);
  });
});

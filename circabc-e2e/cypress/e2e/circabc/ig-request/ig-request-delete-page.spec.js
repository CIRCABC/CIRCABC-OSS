describe('The Request Delete IG Page', function () {
  beforeEach(function () {
    cy.login(
      Cypress.env('interest.group.admin.username'),
      Cypress.env('interest.group.admin.password'),
    );
  });
  it('successfully Request Delete IG', function () {
    cy.wait(3000);
    cy.visit('me/roles', {
      failOnStatusCode: false,
    });
    cy.get('[data-cy="groups"] > .group-title', { timeout: 30000 }).first().click();
    cy.get('[data-cy="admin"]', { timeout: 15000 }).click();
    cy.get('.page-header > :nth-child(3) > .cta').click();
    cy.get('[data-cy="igReqDelete"]').click();
    cy.get('[data-cy="justification"]').type('Request Delete IG Page e2e ');
    cy.get('[data-cy="ok"]').click();
    cy.contains('Success');
  });
});

afterEach(function () {
  cy.checkA11yWithLogging(undefined, undefined);
});

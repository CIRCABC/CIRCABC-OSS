describe('The Request Delete IG Page', function () {
  beforeEach(function () {
    cy.login(
      Cypress.env('interest.group.admin.username'),
      Cypress.env('interest.group.admin.password'),
    );
  });
  it('successfully Request Delete IG', function () {
    cy.visit('me/roles', {
      failOnStatusCode: false,
    });

    cy.get(':nth-child(2) > .group-title').click();
    cy.get('[data-cy="admin"]').click();
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

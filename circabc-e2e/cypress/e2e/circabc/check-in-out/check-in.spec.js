describe('The Check IN file Page', function () {
  beforeEach(function () {
    cy.login(
      Cypress.env('interest.group.admin.username'),
      Cypress.env('interest.group.admin.password'),
    );
  });
  it('successfully Check OUT', function () {
    cy.visit('me/roles', {
      failOnStatusCode: false,
    });

    cy.get(':nth-child(2) > .group-title').click();
    cy.wait(500);
    cy.get('[data-cy="library"]').click();
    cy.wait(500);
    cy.get(
      ':nth-child(2) > .cell-file-name > .file-name > a.ng-star-inserted',
    ).click();

    cy.get('.checkin').click();
    cy.get('[data-cy="ok"]').click();

    cy.contains('Success');
  });
});
afterEach(function () {
  cy.checkA11yWithLogging(undefined, undefined);
});

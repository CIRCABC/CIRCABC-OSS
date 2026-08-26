describe('The Delete Profile Page', function () {
  beforeEach(function () {
    cy.login(
      Cypress.env('interest.group.admin.username'),
      Cypress.env('interest.group.admin.password'),
    );
  });
  it('successfully delete Profile', function () {
    cy.visit('me/roles', {
      failOnStatusCode: false,
    });
    cy.get(':nth-child(2) > .group-title').click();
    cy.wait(500);
    cy.get('[data-cy="members"]').click();
    cy.contains('Profiles').click();
    cy.get(
      ':nth-child(6) > .cell-name > .actions > .ng-star-inserted > a',
    ).click();
    cy.get('[data-cy="ok"]').click();
    cy.contains('Success');
  });
  afterEach(function () {
    cy.checkA11yWithLogging(undefined, undefined);
  });
});

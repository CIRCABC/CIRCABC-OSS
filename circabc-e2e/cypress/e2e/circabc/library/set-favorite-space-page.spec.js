describe('The Library Page', function () {
  beforeEach(function () {
    cy.login(
      Cypress.env('interest.group.admin.username'),
      Cypress.env('interest.group.admin.password'),
    );
  });
  it('successfully make favorite space', function () {
    cy.visit('me/roles', {
      failOnStatusCode: false,
    });
    cy.contains(Cypress.env('interest.group.title')).click();
    cy.get('[data-cy=library]').should('be.visible').click();
    cy.get(
      ':nth-child(2) > .cell-file-name > .file-name > cbc-favourite-switch > .favourite-container > a > .ng-star-inserted',
    ).click();
    cy.visit('me', {
      failOnStatusCode: false,
    });
    cy.contains('Space 1');
  });
});

afterEach(function () {
  cy.checkA11yWithLogging(undefined, undefined);
});

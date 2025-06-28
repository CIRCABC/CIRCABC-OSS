describe('The Copy Paste Page', function () {
  beforeEach(function () {
    cy.login(
      Cypress.env('interest.group.admin.username'),
      Cypress.env('interest.group.admin.password'),
    );
  });
  it('successfully copy, paste , paste link', function () {
    cy.visit('me/roles', {
      failOnStatusCode: false,
    });
    cy.contains(Cypress.env('interest.group.title')).click();
    cy.get('[data-cy=library]').should('be.visible').click();
    cy.get('.row > .cell-checkbox > input').click();
    cy.get('td.ng-star-inserted > .bulk-actions > :nth-child(4) > a').click();
    cy.get('.half-oval > a').click();
    cy.get('#copyItemFromClipboardId > .ng-star-inserted > img').click();
    cy.contains('Success');
    cy.get('#linkItemFromClipboardId > .ng-star-inserted > img').click();
    cy.contains('Success');
  });

  afterEach(function () {
    cy.checkA11yWithLogging(undefined, undefined);
  });
});

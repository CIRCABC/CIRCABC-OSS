describe('The Aprove Delete IG Page', function () {
  beforeEach(function () {
    cy.login(
      Cypress.env('category.admin.username'),
      Cypress.env('category.admin.password'),
    );
  });
  it('successfully aprove delete forum', function () {
    cy.visit('explore', {
      failOnStatusCode: false,
    });
    cy.get('.header-category').click();
    cy.get(':nth-child(1) > .link').click();
    cy.get('.sub-menu > .ng-star-inserted').click();
    cy.get(':nth-child(6) > .tab > .tab-text').click();
    cy.wait(1000);
    cy.get(
      '#mat-tab-group-0-label-1 > .mdc-tab__content > .mdc-tab__text-label',
    ).click();
    cy.get(
      ':nth-child(1) > .g-request > :nth-child(1) > .actions > [data-cy="approve"]',
    ).click();
    cy.wait(1000);
    cy.get('[data-cy="delete-button"]').click();
    cy.contains('Success');
  });
});

afterEach(function () {
  cy.checkA11yWithLogging(undefined, undefined);
});

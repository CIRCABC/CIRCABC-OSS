describe('The Aprove Create IG Page', function () {
  beforeEach(function () {
    cy.login(
      Cypress.env('category.admin.username'),
      Cypress.env('category.admin.password'),
    );
  });

  it('successfully aprove create IG', function () {
    cy.visit('explore', {
      failOnStatusCode: false,
    });

    cy.get('.header-category').click();
    cy.wait(3000);
    cy.get(':nth-child(1) > .link').click();
    cy.wait(3000);
    cy.get('.sub-menu > .ng-star-inserted').click();
    cy.wait(1000);
    cy.get(':nth-child(6) > .tab > .tab-text').click();
    cy.wait(1000);
    cy.get(
      ':nth-child(1) > .g-request > :nth-child(1) > .actions > [data-cy="approve"]',
    ).click();
    cy.wait(1000);
    cy.get('.ql-editor').type(
      "Ah, ha, ha, ha, stayin' alive, stayin' alive ..  stayin' aliiiiiiive",
    );
    cy.get('form.ng-touched > .actions > .cta').click();
    cy.contains('Success');
  });

  afterEach(function () {
    cy.checkA11yWithLogging(undefined, undefined);
  });
});

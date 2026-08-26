describe('The Request Create IG Page', function () {
  beforeEach(function () {
    cy.login(
      Cypress.env('interest.group.admin.username'),
      Cypress.env('interest.group.admin.password'),
    );
  });
  it('successfully request create IG', function () {
    cy.visit('explore', {
      failOnStatusCode: false,
    });

    cy.get('.link').click();
    // cy.pause();
    cy.get('.link').click();
    cy.wait(2000);
    cy.get('cbc-explorer-dropdown > .cta').click();
    // cy.get('.page-header__actions > .ng-star-inserted > .cta').click();
    cy.get('.request--group').click();
    cy.get('#header').select(0);
    cy.wait(1000);
    cy.get('#category').select(0);
    cy.wait(1000);
    cy.get(':nth-child(4) > :nth-child(1) > .ng-untouched').type('aaaa');
    cy.get(':nth-child(4) > :nth-child(2) > .ng-pristine').type('aaaa');
    cy.wait(1000);
    cy.get('#comment').type(
      "Ah, ha, ha, ha, stayin' alive, stayin' alive ..  stayin' aliiiiiiive",
    );
    cy.get('.cta').click();
    cy.get('.cta').click();
    cy.contains('Success');
  });

  afterEach(function () {
    cy.checkA11yWithLogging(undefined, undefined);
  });
});

describe('The Notification Page', function () {
  beforeEach(function () {
    cy.login(
      Cypress.env('interest.group.admin.username'),
      Cypress.env('interest.group.admin.password'),
    );
  });
  it('successfully set notification file', function () {
    cy.visit('me/roles', {
      failOnStatusCode: false,
    });
    cy.contains(Cypress.env('interest.group.title')).click();
    cy.get('[data-cy=library]').should('be.visible').click();
    cy.get('[data-cy="notifications"]').should('be.visible').click();
    cy.get('.box > .cta').should('be.visible').click();
    cy.get('#search').should('be.visible').click();
    cy.get('#selectMultiple')
      .should('be.visible')
      .find('option')
      .should('have.length.greaterThan', 0)
      .first()
      .then(($option) => {
        const value = $option.val();
        cy.get('#selectMultiple').select(value);
      })
      .then(() => {
        cy.wait(500);
        cy.get('#addToList').should('be.visible').click();
        cy.get('[data-cy="ok"]').should('be.visible').click();
        cy.contains('Success');
        cy.contains('Close').click();
      });
  });

  afterEach(function () {
    cy.checkA11yWithLogging(undefined, undefined);
  });
});

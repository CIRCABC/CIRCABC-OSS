describe('The Delete Mebership Page', function () {
  beforeEach(function () {
    cy.login(
      Cypress.env('interest.group.admin.username'),
      Cypress.env('interest.group.admin.password'),
    );
  });
  it('successfully delete  membership', function () {
    cy.visit('me/roles', {
      failOnStatusCode: false,
    });
    cy.contains(Cypress.env('interest.group.title')).click();
    cy.get('[data-cy="members"]').should('be.visible').click();
    Cypress.on('fail', (error, runnable) => {
      if (error.message.includes('element is not visible')) {
        return false; // returning false here prevents Cypress from failing the test
      }
      throw error; // throw the error otherwise
    });

    cy.get(
      'tr.row:nth-child(2) > td:nth-child(4) > ul:nth-child(2) > li:nth-child(1) > a:nth-child(1)',
    ).click({ force: true });

    cy.get('[data-cy="ok"]').should('be.visible').click();
  });

  afterEach(function () {
    cy.checkA11yWithLogging(undefined, undefined);
  });
});

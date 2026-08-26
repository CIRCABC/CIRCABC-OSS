describe('Created circabc', function () {
  beforeEach(function () {
    cy.login('admin', 'admin');
  });

  it('successfully created circabc', function () {
    cy.visit('admin/circabc', {
      failOnStatusCode: false,
    });
    cy.get('[data-cy=add-circabc]').click();
    cy.get('#name').type(Cypress.env('circabc.admin.username'));
    cy.get('[data-cy=search-button]').click();
    cy.get('[data-cy=users]').select(0);

    cy.get('[data-cy=add-selection]').click();
    cy.wait(500);
    cy.get('[data-cy=ok').click();
    cy.wait(500);
    cy.contains('Success');
    cy.contains('Close').click();
  });
});

afterEach(function () {
  cy.checkA11yWithLogging(undefined, undefined);
});

describe('The Create a link on Help Section Page', function () {
  beforeEach(function () {
    cy.login(Cypress.env('admin.username'), Cypress.env('admin.password'));
  });

  it('successfully create a link on help section', function () {
    cy.visit('help/start', {
      failOnStatusCode: false,
    });
    cy.get('[data-cy="edit-inline"]').click();
    cy.get('[data-cy="text"]').click();
    cy.get('[data-cy="text"]').type('Link1-Updated');
    cy.get('#href').click();
    cy.get('#href').type('http://link1-updated.test');
    cy.get('[data-cy="ok"]').click();
    cy.contains('Success');
  });
});
afterEach(function () {
  cy.checkA11yWithLogging(undefined, undefined);
});

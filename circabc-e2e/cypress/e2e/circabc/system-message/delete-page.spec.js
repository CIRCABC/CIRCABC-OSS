describe('The Delete System Message Page', function () {
  beforeEach(function () {
    cy.login('admin', 'admin');
  });
  it('successfully delete system message', function () {
    cy.visit('support/system-message', {
      failOnStatusCode: false,
    });
    cy.get('[data-cy=delete-inline]').click();
    cy.get('#confirmDeletePerm').click();
    cy.contains('Success');
  });

  afterEach(function () {
    cy.checkA11yWithLogging(undefined, undefined);
  });
});

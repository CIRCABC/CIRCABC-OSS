describe('The Update System Message Page', function () {
  beforeEach(function () {
    cy.login('admin', 'admin');
  });
  it('successfully update system message', function () {
    cy.visit('support/system-message', {
      failOnStatusCode: false,
    });
    cy.get(
      ':nth-child(1) > cbc-template-renderer > .template > .actions > [data-cy="update-message"]',
    ).click();
    cy.wait(500);
    cy.get('.ql-editor').type(' Updated');
    cy.wait(500);
    cy.get('.cta').click();
    cy.contains('Success');
  });

  afterEach(function () {
    cy.checkA11yWithLogging(undefined, undefined);
  });
});

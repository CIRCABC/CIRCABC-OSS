describe('The Create System Message Page', function () {
  beforeEach(function () {
    cy.login('admin', 'admin');
  });
  it('successfully create system message', function () {
    cy.visit('support/system-message', {
      failOnStatusCode: false,
    });
    cy.get('.cta').click();
    cy.wait(500);
    cy.get('.ql-editor').type('My Message 1');
    cy.wait(500);
    cy.get('.cta').click();
    cy.contains('Success');
  });

  afterEach(function () {
    cy.checkA11yWithLogging(undefined, undefined);
  });
});

describe('The Download Page', function () {
  beforeEach(function () {
    cy.login(
      Cypress.env('interest.group.admin.username'),
      Cypress.env('interest.group.admin.password'),
    );
  });
  it('successfully download file', function () {
    cy.visit('me/roles', {
      failOnStatusCode: false,
    });
    cy.contains(Cypress.env('interest.group.title')).click();
    cy.get('[data-cy=library]').should('be.visible').click();
    cy.contains('CIRCABC_Leader_Guide.pdf').should('be.visible').click();
    cy.get('.download').should('be.visible').click();
  });

  it('downloaded file visible in user dashboard', function () {
    cy.visit('me', {
      failOnStatusCode: false,
    });
    cy.contains('Downloads').click();
    cy.contains('CIRCABC_Leader_Guide.pdf');
  });
  afterEach(function () {
    cy.checkA11yWithLogging(undefined, undefined);
  });
});

describe('The Home Page', function () {
  beforeEach(function () {
    cy.visit('/', {
      failOnStatusCode: false,
    });
  });
  it('successfully show home page', function () {
    cy.contains('CIRCABC');
    cy.url().should('include', 'welcome');
  });
  afterEach(function () {
    cy.checkA11yWithLogging(undefined, undefined);
  });
});

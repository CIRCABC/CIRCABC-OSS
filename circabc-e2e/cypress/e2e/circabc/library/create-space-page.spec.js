describe('The Create Space Page', function () {
  beforeEach(function () {
    cy.login(
      Cypress.env('interest.group.admin.username'),
      Cypress.env('interest.group.admin.password'),
    );
  });
  it('successfully create space', function () {
    cy.visit('me/roles', {
      failOnStatusCode: false,
    });
    cy.contains(Cypress.env('interest.group.title')).click();
    cy.get('[data-cy=library]').should('be.visible').click();
    cy.get('[data-cy=add]').click();
    cy.get('[data-cy=folder]').should('be.visible').click();
    cy.get('[data-cy=name]').type('Space 1');
    cy.get('[data-cy=text]').type('Space 1 Title');
    cy.get('.ql-editor').type('Space 1 Description');
    cy.get('[data-cy=ok]').click();
    cy.contains('Success');
  });
});

afterEach(function () {
  cy.checkA11yWithLogging(undefined, undefined);
});

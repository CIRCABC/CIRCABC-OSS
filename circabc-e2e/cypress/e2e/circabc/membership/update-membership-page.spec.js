describe('The Update Mebership Page', function () {
  beforeEach(function () {
    cy.login(
      Cypress.env('interest.group.admin.username'),
      Cypress.env('interest.group.admin.password'),
    );
  });
  it('successfully update  membership', function () {
    cy.visit('me/roles');
    cy.contains(Cypress.env('interest.group.title')).click();
    cy.get('[data-cy="members"]').should('be.visible').click();
    cy.get(':nth-child(2) > .cell-firstname > .file-name > a')
      .should('be.visible')
      .trigger('mouseover');
    cy.contains('Change profile').click();
    cy.get('#profileName').select('Access');
    cy.get('[data-cy="ok"]').should('be.visible').click();
  });

  afterEach(function () {
    cy.checkA11yWithLogging(undefined, undefined);
  });
});

describe('The Create Profile Page', function () {
  beforeEach(function () {
    cy.login(
      Cypress.env('interest.group.admin.username'),
      Cypress.env('interest.group.admin.password'),
    );
  });
  it('successfully create profile', function () {
    cy.visit('me/roles', {
      failOnStatusCode: false,
    });
    cy.get(':nth-child(2) > .group-title').click();
    cy.wait(500);
    cy.get('[data-cy="members"]').click();
    cy.contains('Profiles').click();
    cy.get('li > .cta').click();
    cy.get('[data-cy="text"]').type('MyProfile1');
    cy.get('.modal-footer > .buttons-group > .cta').click();
    cy.contains('Success');
  });

  afterEach(function () {
    cy.checkA11yWithLogging(undefined, undefined);
  });
});

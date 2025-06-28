describe('The Invite user Page', function () {
  beforeEach(function () {
    cy.login(
      Cypress.env('interest.group.admin.username'),
      Cypress.env('interest.group.admin.password'),
    );
  });
  it('successfully invite user and sending notification', function () {
    cy.visit('me/roles', {
      failOnStatusCode: false,
    });
    cy.contains(Cypress.env('interest.group.title')).click();
    cy.get('[data-cy="members"]').should('be.visible').click();
    //
    cy.get('[data-cy="actions"]').should('be.visible').click();
    cy.get('[data-cy="invite-member"]').should('be.visible').click();
    cy.get('[data-cy="search-user"]').type(Cypress.env('access.username'));
    cy.get('[data-cy="search-button"]').should('be.visible').click();
    cy.get('[data-cy=users]').select(
      Cypress.env('access.first.name') +
        ' ' +
        Cypress.env('access.last.name') +
        ' (' +
        Cypress.env('access.email') +
        ')',
    );
    cy.get('[data-cy=select]').should('be.visible').click();
    cy.wait(500);

    cy.get('[ id="cmn-toggle-1"]').click();

    cy.get('[data-cy="message"]').type(
      'When I find myself in times of trouble, Mother Mary comes to me Speaking words of wisdom, let it be',
    );

    cy.get('[ id="cmn-toggle-2"]').should('be.visible').click();

    cy.get('[data-cy=ok]').should('be.visible').click();
    cy.contains('Success');
  });

  afterEach(function () {
    cy.checkA11yWithLogging(undefined, undefined);
  });
});

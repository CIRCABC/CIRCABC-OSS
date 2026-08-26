describe('Accessibility', function () {
  beforeEach(function () {
    cy.login(
      Cypress.env('interest.group.admin.username'),
      Cypress.env('interest.group.admin.password'),
    );
  });

  it('home page', function () {
    cy.visit('/');

    cy.checkA11yWithLogging(undefined, undefined);
  });
  it('user dashboard', function () {
    cy.visit('/me');

    cy.checkA11yWithLogging(undefined, undefined);
  });
  it('user calendar', function () {
    cy.visit('/me/calendar');

    cy.checkA11yWithLogging(undefined, undefined);
  });
  it('user calendar day', function () {
    cy.visit('/me/calendar');
    cy.get('#view').select('day');

    cy.checkA11yWithLogging(undefined, undefined);
  });

  it('user calendar week', function () {
    cy.visit('/me/calendar');
    cy.get('#view').select('week');

    cy.checkA11yWithLogging(undefined, undefined);
  });

  it('user roles', function () {
    cy.visit('/me/roles');

    cy.checkA11yWithLogging(undefined, undefined);
  });

  it('user explore', function () {
    cy.visit('/explore');

    cy.checkA11yWithLogging(undefined, undefined);
    cy.get('.link').should('be.visible').click();
    cy.checkA11yWithLogging(undefined, undefined);
    cy.get('.link').should('be.visible').click();
    cy.checkA11yWithLogging(undefined, undefined);
  });
});

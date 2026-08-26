describe('The Upload Page', function () {
  beforeEach(function () {
    cy.login(
      Cypress.env('interest.group.admin.username'),
      Cypress.env('interest.group.admin.password'),
    );
  });
  it('successfully upload file', function () {
    cy.visit('me/roles', {
      failOnStatusCode: false,
    });
    cy.contains(Cypress.env('interest.group.title')).click();
    cy.get('[data-cy=library]').should('be.visible').click();
    cy.get('[data-cy=add]').click();
    cy.get('[data-cy=files]').click();

    const fileName = 'cypress/fixtures/files/CIRCABC_Leader_Guide.pdf';

    cy.get('[data-cy="file-input"]').selectFile(fileName, { force: true });

    cy.get('[data-cy=upload]').click();

    cy.get('[data-cy=finish]').click();
  });

  it('uploaded file visible in user dashboard', function () {
    cy.visit('me', {
      failOnStatusCode: false,
    });
    cy.contains('Uploads').click();
    cy.contains('CIRCABC_Leader_Guide.pdf');
  });

  afterEach(function () {
    cy.checkA11yWithLogging(undefined, undefined);
  });
});

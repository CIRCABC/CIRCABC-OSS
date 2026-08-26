describe('The Create Help Article Page', function () {
  beforeEach(function () {
    cy.login(Cypress.env('admin.username'), Cypress.env('admin.password'));
  });

  it('successfully Create Help Article', function () {
    cy.visit('help/start').contains('Section1');
    cy.get('ul > .ng-star-inserted > a').click();
    cy.get('.article-list > .actions > .cta').click();
    cy.get('[data-cy="text"]').type('Article1');
    cy.get('.field > .custom-select > .ng-valid').select('en');
    cy.get('.ql-editor').type('Space 1 Description');

    cy.get('[data-cy="ok"]').click();
    cy.contains('Article created');
  });

  afterEach(function () {
    cy.checkA11yWithLogging(undefined, undefined);
  });
});

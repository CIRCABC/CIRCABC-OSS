describe("Delete interest group", function() {
  beforeEach(function() {
    cy.login(
      Cypress.env("category.admin.username"),
      Cypress.env("category.admin.password")
    );
  });
  it("successfully deleted interest group ", function() {
    cy.visit("me/roles", {
      failOnStatusCode: false
    });
    cy.get("[data-cy=categories]>.group-title").click();
    cy.get("[data-cy=group-list]").click();
    cy.get('[data-cy=groups]>.group-title').click()
    cy.get("[data-cy=admin]").click();
    cy.get("[data-cy=delete-group]").click();
    cy.get("[data-cy=verify]").click();
    cy.get("[data-cy=delete-button]").click();
    cy.contains("Success");
    cy.contains("Close").click();
  });
});

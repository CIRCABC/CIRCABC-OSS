describe("The delete file", function() {
  beforeEach(function() {
    cy.login(
      Cypress.env("interest.group.admin.username"),
      Cypress.env("interest.group.admin.password")
    );
  });
  it("successfully delete file", function() {
    cy.visit("me/roles", {
      failOnStatusCode: false
    });
    cy.contains(Cypress.env("interest.group.title")).click();
    cy.contains("Library").click();
    cy.get("[data-cy=select-all]").click();
    cy.get("[data-cy=delete-all]").click();
    cy.get("[data-cy=delete]").click();
    cy.contains("Success");
    cy.contains("Close").click();
  });
});

describe("The Login Page", function() {
  it("successfully login", function() {
    cy.visit("/", {
      failOnStatusCode: false
    });
    cy.get("[data-cy=connect]").click();
    cy.get("[data-cy=username]").type(Cypress.env("admin.username"));
    cy.get("[data-cy=password]").type(Cypress.env("admin.password"));
    cy.get("[data-cy=login]").click();
    cy.contains("Dashboard");
    cy.url().should("include", "/" + "me");
  });
});

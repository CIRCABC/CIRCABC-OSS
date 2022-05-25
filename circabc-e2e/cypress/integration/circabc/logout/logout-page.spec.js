describe("The Logout Page", function() {
  it("successfully logout", function() {
    cy.visit("/");
    cy.get(".cta").click();
    cy.get("#username").type(Cypress.env("admin.username"));
    cy.get("#password").type(Cypress.env("admin.password"));
    cy.get(".cta").click();
    cy.visit("login/logout", {
      failOnStatusCode: false
    });
    cy.contains("CIRCABC");
    cy.url().should("include", "/" + "welcome");
  });
});

describe("Create users", function () {
  beforeEach(function () {
    cy.login(
      Cypress.env("circabc.admin.username"),
      Cypress.env("circabc.admin.password")
    );
  });

  it("successfully created 5 users", function () {
    for (var i = 1; i < 6; i++) {
      cy.visit("/me/roles", {
        failOnStatusCode: false,
      });
      cy.get("[data-cy=create-user]").click();
      cy.get("[data-cy=username]").type(Cypress.env("user" + i + ".username"));
      cy.get("[data-cy=firstname]").type(
        Cypress.env("user" + i + ".first.name")
      );
      cy.get("[data-cy=lastname]").type(Cypress.env("user" + i + ".last.name"));
      cy.get("[data-cy=email]").type(Cypress.env("user" + i + ".email"));
      cy.get("[data-cy=phone]").type(Cypress.env("user" + i + ".phone"));
      cy.get("[data-cy=postalAddress]").type(
        Cypress.env("user" + i + ".postalAddress")
      );
      cy.get("[data-cy=password]").type(Cypress.env("user" + i + ".password"));
      cy.get("[data-cy=passwordVerify]").type(
        Cypress.env("user" + i + ".password")
      );

      cy.get("[data-cy=create]").click();
      cy.contains("Success");
    }
  });
});

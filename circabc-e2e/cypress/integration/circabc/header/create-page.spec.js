describe("The Create Header Page", function() {
  beforeEach(function() {
    cy.login(Cypress.env("admin.username"), Cypress.env("admin.password"));
  });


  afterEach(function () {
    cy.wait(5000);
    cy.setPassword(
      Cypress.env("circabc.admin.username"),
      Cypress.env("circabc.admin.password")
    );
  });

  it("successfully login", function() {
    cy.visit("/" + "admin/headers", {
      failOnStatusCode: false
    });
    cy.get("[data-cy=add-header]").click();
    cy.get("[data-cy=name]").type(Cypress.env("header.name"));
    cy.get("[data-cy=description]")
      .get("[data-cy=text]")
      .type(Cypress.env("header.description"));
    cy.get("[data-cy=ok").click();
    cy.contains("Success");
    cy.contains("Close").click();
  });
});

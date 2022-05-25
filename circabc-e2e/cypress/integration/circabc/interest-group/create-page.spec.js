describe("Create interest group", function () {
  beforeEach(function () {
    cy.login(
      Cypress.env("category.admin.username"),
      Cypress.env("category.admin.password")
    );
  });
  afterEach(function () {
    cy.wait(5000);
    cy.setPassword(
      Cypress.env("interest.group.admin.username"),
      Cypress.env("interest.group.admin.password")
    );
  });
  it("successfully created interest group ", function () {
    cy.visit("me/roles", {
      failOnStatusCode: false,
    });
    cy.get(".group-title>a").click();
    cy.get("[data-cy=add-interest-group]").click();
    cy.get("[data-cy=name]").type(Cypress.env("interest.group.name"));

    cy.get("[data-cy=title]>.flexContainer>.flexWrap>[data-cy=text]").type(
      Cypress.env("interest.group.title")
    );

    cy.get("[data-cy=textarea]")
      .first()
      .type(Cypress.env("interest.group.description"));
    cy.get("[data-cy=textarea]")
      .last()
      .type(Cypress.env("interest.group.contact"));

    cy.get("[data-cy=ok]").click();
    cy.get("[data-cy=search]").type(
      Cypress.env("interest.group.admin.username")
    );
    cy.get("[data-cy=search-button]").click();
    cy.get("[data-cy=users]").select(
      Cypress.env("interest.group.admin.first.name") +
        " " +
        Cypress.env("interest.group.admin.last.name") +
        " (" +
        Cypress.env("interest.group.admin.email") +
        ")"
    );
    cy.get("[data-cy=select]").click();
    cy.get("[data-cy=ok]").click();
    cy.get("[data-cy=ok]").click();
    cy.contains("Success");
    cy.contains("Close").click();
  });
});

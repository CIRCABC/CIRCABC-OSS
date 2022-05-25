
// private interest group with leadership

for (var i = 1; i < 6; i++) {
describe("Create interest group", function () {
  beforeEach(function () {
    cy.login(
      Cypress.env("user" + i + ".username"),
      Cypress.env("user" + i + ".password")
    );
  });
  it("successfully created interest group ", function () {
    cy.visit("me/roles", {
      failOnStatusCode: false
    });
    cy.get(".group-title>a").click();
    cy.get(".page-header__actions>.cta").click();
    cy.get("[data-cy=name]").type(Cypress.env("interest.group.name"+" - "+Cypress.env("user" + i + ".username")));
    cy.get("[data-cy=title]>.field>[data-cy=text]").type(
      Cypress.env("interest.group.title"+" - "+Cypress.env("user" + i + ".username"))
    );

    cy.get(
      "[data-cy=description]>.field>.editor-container>[data-cy=textarea]>.p-editor-container>.p-editor-content>.ql-editor"
    ).type(Cypress.env("interest.group.description"+" - "+Cypress.env("user" + i + ".username")));
    cy.get(
      "[data-cy=contact]>.field>.editor-container>[data-cy=textarea]>.p-editor-container>.p-editor-content>.ql-editor"
    ).type(Cypress.env("interest.group.contact"+" - "+Cypress.env("user" + i + ".username")));
    cy.get("[data-cy=ok]").click();
    
      cy.get("[data-cy=search]").type(Cypress.env("user" + i + ".username"));
      cy.get("[data-cy=search-button]").click();
      cy.get("[data-cy=users]").select(
        Cypress.env("user" + i + ".first.name") +
        " " +
        Cypress.env("user" + i + ".last.name") +
        " (" +
        Cypress.env("user" + i + ".email") +
        ")"
      );
      cy.get("[data-cy=select]").click();
      cy.get("[data-cy=ok]").click();
    cy.get("[data-cy=ok]").click();
  });
});
}




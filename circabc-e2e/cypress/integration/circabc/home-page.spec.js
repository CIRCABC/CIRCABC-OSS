describe("The Home Page", function() {
  beforeEach(function() {
    cy.visit("/", {
      failOnStatusCode: false
    });
  });
  it("successfully show home page", function() {
    cy.contains("CIRCABC");
    cy.url().should("include", "welcome");
  });
  it("successfully accept cookies", function() {
    cy.contains("This site uses cookies");
    cy.get('[data-cy="accept-cookies"]').click();
  });
});

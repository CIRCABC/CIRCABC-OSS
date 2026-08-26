describe("Lock Interest Group", function () {
  // Lock from Category Admin perspective (Administração da Categoria)
  describe("Category Admin locks IG", function () {
    beforeEach(function () {
      cy.login(
        Cypress.env("category.admin.username"),
        Cypress.env("category.admin.password"),
      );
    });

    it("successfully lock/unlock IG from category admin with a message", function () {
      cy.visit("me/roles", { failOnStatusCode: false });
      cy.get(".group-title>a").click();
      cy.get('[data-cy="group-list"]').click();
      cy.get('[aria-label="Lock the Interest Group"]').should("be.visible");
      // Click the lock icon in the category groups list
      cy.get('[data-cy="lock"]').first().click();

      // Lock dialog should appear
      cy.contains("Lock the interest group").should("be.visible");
      cy.contains(
        "When locking the interest group, only IG leaders and category administrators will be able to access it.",
      ).should("be.visible");

      // Type a lock message in the rich text editor
      cy.get(".ql-editor").type("This group is temporarily unavailable.");

      // Leave Read-only as No (default) and click Lock
      cy.get('[data-cy="confirmlock"]').first().click();
      cy.wait(500);
      cy.contains("Success");

      cy.get('[data-cy="unlock"]').first().click();
      cy.contains("Success");
    });

    afterEach(function () {
      cy.checkA11yWithLogging(undefined, undefined);
    });
  });

  // Lock from IG Leader perspective (Administração do IG)
  describe("IG Leader locks IG from group admin", function () {
    beforeEach(function () {
      cy.login(
        Cypress.env("interest.group.admin.username"),
        Cypress.env("interest.group.admin.password"),
      );
    });

    it("successfully locks IG with read-only mode enabled", function () {
      cy.visit("me/roles", { failOnStatusCode: false });
      cy.contains(Cypress.env("interest.group.title")).click();
      cy.get("[data-cy=admin]").click();

      // Open the Actions dropdown
      cy.contains("Actions").click();
      cy.get("[data-cy=igLockGroup]").click();
      cy.wait(300);

      cy.contains(
        "When locking the interest group, only IG leaders and category administrators will be able to access it.",
      ).should("be.visible");

      // Type a lock message in the rich text editor
      cy.get('[data-cy="messagelock"]').type(
        "This group is temporarily unavailable.",
      );

      // Enable Read-only toggle
      // mat-slide-toggle renders an internal <button role="switch">, click that directly
      cy.get('[data-cy="readonly"]').find("button").click();
      cy.contains("Yes").should("be.visible");

      // Leave Read-only as No (default) and click Lock
      cy.get('[data-cy="confirmlock"]').first().click();

      cy.contains("successfully locked the interest group").should(
        "be.visible",
      );
    });

    it("lock icon appears next to IG name in menu after locking", function () {
      cy.visit("me/roles", { failOnStatusCode: false });
      cy.contains(Cypress.env("interest.group.title")).click();

      // After locking, leader should see lock icon in the navigator
      cy.get(".ig-lock-icon").should("be.visible");
    });

    afterEach(function () {
      cy.checkA11yWithLogging(undefined, undefined);
    });
  });
});

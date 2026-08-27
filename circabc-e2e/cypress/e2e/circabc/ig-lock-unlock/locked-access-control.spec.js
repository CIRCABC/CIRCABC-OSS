describe("Locked IG Access Control", function () {
  // Regular member is redirected to locked page
  describe("Regular member access to locked IG", function () {
    beforeEach(function () {
      // First, ensure the IG is locked — lock it as category admin
      cy.login(
        Cypress.env("category.admin.username"),
        Cypress.env("category.admin.password"),
      );
      cy.visit("me/roles", { failOnStatusCode: false });
      cy.wait(500);
      cy.get(".group-title>a").click();
      cy.get('[data-cy="group-list"]').click();
      cy.wait(500);
      // Lock if not already locked (lock icon present means it's unlocked)
      cy.get("body").then(($body) => {
        if ($body.find('[aria-label="Lock the Interest Group"]').length > 0) {
          cy.get('[aria-label="Lock the Interest Group"]').first().click();
          cy.contains("Lock the interest group").should("be.visible");
          cy.get('[data-cy="confirmlock"]').first().click();
          cy.contains("successfully locked the interest group").should(
            "be.visible",
          );
        }
      });
      cy.visit("login/logout", { failOnStatusCode: false });
    });

    it("member sees locked page when accessing a locked IG", function () {
      // Login as a regular member (contributor)
      cy.wait(500);
      cy.login(
        Cypress.env("reviewer.username"),
        Cypress.env("reviewer.password"),
      );
      cy.visit("me/roles", { failOnStatusCode: false });
      cy.wait(500);
      cy.contains(Cypress.env("interest.group.title")).click();
      cy.wait(500);
      // Should be redirected to the locked page
      cy.url().should("include", "/locked");
    });

    it("locked page displays a lock icon", function () {
      cy.wait(500);
      cy.login(
        Cypress.env("reviewer.username"),
        Cypress.env("reviewer.password"),
      );
      cy.visit("me/roles", { failOnStatusCode: false });
      cy.wait(500);
      cy.contains(Cypress.env("interest.group.title")).click();

      cy.url().should("include", "/locked");
      cy.get('img[src*="icon-lock"]').should("be.visible");
    });

    afterEach(function () {
      cy.checkA11yWithLogging(undefined, undefined);
    });
  });

  // IG Leader can still access a locked IG
  describe("IG Leader access to locked IG", function () {
    beforeEach(function () {
      cy.wait(500);
      cy.login(
        Cypress.env("interest.group.admin.username"),
        Cypress.env("interest.group.admin.password"),
      );
    });
    it("IG leader can access locked IG without being redirected", function () {
      cy.visit("me/roles", { failOnStatusCode: false });
      cy.wait(500);
      cy.contains(Cypress.env("interest.group.title")).click();

      // Leader should NOT be redirected to /locked
      cy.url().should("not.include", "/locked");
      cy.get("[data-cy=library]").should("be.visible");
    });

    it("IG leader sees lock icon in the group navigator when IG is locked", function () {
      cy.visit("me/roles", { failOnStatusCode: false });
      cy.wait(500);
      cy.contains(Cypress.env("interest.group.title")).click();

      cy.get(".ig-lock-icon").should("be.visible");
    });

    afterEach(function () {
      cy.checkA11yWithLogging(undefined, undefined);
    });
  });

  // Read-only mode: write buttons hidden for leader
  describe("Read-only mode restricts write actions", function () {
    before(function () {
      cy.wait(500);
      // Lock with read-only enabled as IG leader
      cy.login(
        Cypress.env("interest.group.admin.username"),
        Cypress.env("interest.group.admin.password"),
      );
      cy.visit("me/roles", { failOnStatusCode: false });
      cy.wait(500);

      cy.contains(Cypress.env("interest.group.title")).click();
      cy.get("[data-cy=admin]").click();
      cy.wait(300);
      // Actions dropdown to unlock
      cy.contains("Actions").click();
      cy.get("[data-cy=igUnlockGroup]").click();
      cy.wait(300);

      // Actions dropdown to lock with readonly
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

      cy.wait(500);

      // cy.contains(Cypress.env("interest.group.title")).click();
      cy.get("[data-cy=admin]").click();
      cy.contains("Actions").click();
      // Lock with read-only if the lock option is visible (IG might already be locked)
      cy.get("body").then(($body) => {
        if ($body.find("[data-cy=igLockGroup]").length > 0) {
          cy.get("[data-cy=igLockGroup]").click();
          cy.contains("Lock the interest group").should("be.visible");
          cy.get("#read-only-toggle").click();
          cy.contains("Lock").click();
          cy.contains("successfully locked the interest group").should(
            "be.visible",
          );
        }
      });
    });

    it("library Add button is hidden in read-only mode for the IG leader", function () {
      cy.wait(500);
      cy.login(
        Cypress.env("interest.group.admin.username"),
        Cypress.env("interest.group.admin.password"),
      );
      cy.visit("me/roles", { failOnStatusCode: false });
      cy.contains(Cypress.env("interest.group.title")).click();
      cy.get("[data-cy=library]").should("be.visible").click();

      // The Add/upload button should not be visible in read-only mode
      cy.get("[data-cy=add]").should("not.exist");
    });

    it("unlock option remains available in read-only mode for IG leader", function () {
      cy.wait(500);
      cy.login(
        Cypress.env("interest.group.admin.username"),
        Cypress.env("interest.group.admin.password"),
      );
      cy.visit("me/roles", { failOnStatusCode: false });
      cy.wait(500);
      cy.contains(Cypress.env("interest.group.title")).click();
      cy.get("[data-cy=admin]").click();

      // Unlock option should still be visible even in read-only mode
      cy.contains("Actions").click();
      cy.get("[data-cy=igUnlockGroup]").should("be.visible");
    });

    after(function () {
      cy.wait(500);
      // Clean up — unlock the IG after read-only tests
      cy.login(
        Cypress.env("interest.group.admin.username"),
        Cypress.env("interest.group.admin.password"),
      );
      cy.visit("me/roles", { failOnStatusCode: false });
      cy.contains(Cypress.env("interest.group.title")).click();
      cy.get("[data-cy=admin]").click();
      cy.contains("Actions").click();
      cy.get("[data-cy=igUnlockGroup]").click();
      cy.contains("successfully unlocked the interest group").should(
        "be.visible",
      );
      // Navigate back to roles and re-enter the IG to get a fresh page state,
      // then verify the lock icon is gone after the unlock has fully taken effect.
      cy.visit("me/roles", { failOnStatusCode: false });
      cy.contains(Cypress.env("interest.group.title")).click();
      cy.get(".ig-lock-icon").should("not.exist");
    });

    afterEach(function () {
      cy.checkA11yWithLogging(undefined, undefined);
    });
  });
});

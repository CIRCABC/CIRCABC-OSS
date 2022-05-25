describe("Create users", function () {
  beforeEach(function () {
    cy.login(
      Cypress.env("circabc.admin.username"),
      Cypress.env("circabc.admin.password")
    );
  });

  it("successfully created category admin", function () {
    cy.visit("/me/roles", {
      failOnStatusCode: false,
    });
    cy.get("[data-cy=create-user]").click();
    cy.get("[data-cy=username]").type(Cypress.env("category.admin.username"));
    cy.get("[data-cy=firstname]").type(
      Cypress.env("category.admin.first.name")
    );
    cy.get("[data-cy=lastname]").type(Cypress.env("category.admin.last.name"));
    cy.get("[data-cy=email]").type(Cypress.env("category.admin.email"));
    cy.get("[data-cy=phone]").type(Cypress.env("category.admin.phone"));
    cy.get("[data-cy=postalAddress]").type(
      Cypress.env("category.admin.postalAddress")
    );
    cy.get("[data-cy=password]").type(Cypress.env("category.admin.password"));
    cy.get("[data-cy=passwordVerify]").type(
      Cypress.env("category.admin.password")
    );

    cy.get("[data-cy=create]").click();
  });

  it("successfully created interest group admin", function () {
    cy.visit("/me/roles", {
      failOnStatusCode: false,
    });
    cy.get("[data-cy=create-user]").click();
    cy.get("[data-cy=username]").type(
      Cypress.env("interest.group.admin.username")
    );
    cy.get("[data-cy=firstname]").type(
      Cypress.env("interest.group.admin.first.name")
    );
    cy.get("[data-cy=lastname]").type(
      Cypress.env("interest.group.admin.last.name")
    );
    cy.get("[data-cy=email]").type(Cypress.env("interest.group.admin.email"));
    cy.get("[data-cy=phone]").type(Cypress.env("interest.group.admin.phone"));
    cy.get("[data-cy=postalAddress]").type(
      Cypress.env("interest.group.admin.postalAddress")
    );
    cy.get("[data-cy=password]").type(
      Cypress.env("interest.group.admin.password")
    );
    cy.get("[data-cy=passwordVerify]").type(
      Cypress.env("interest.group.admin.password")
    );

    cy.get("[data-cy=create]").click();
    cy.contains("Success");
  });

  it("successfully created access user", function () {
    cy.visit("/me/roles", {
      failOnStatusCode: false,
    });
    cy.get("[data-cy=create-user]").click();
    cy.get("[data-cy=username]").type(Cypress.env("access.username"));
    cy.get("[data-cy=firstname]").type(Cypress.env("access.first.name"));
    cy.get("[data-cy=lastname]").type(Cypress.env("access.last.name"));
    cy.get("[data-cy=email]").type(Cypress.env("access.email"));
    cy.get("[data-cy=phone]").type(Cypress.env("access.phone"));
    cy.get("[data-cy=postalAddress]").type(Cypress.env("access.postalAddress"));
    cy.get("[data-cy=password]").type(Cypress.env("access.password"));
    cy.get("[data-cy=passwordVerify]").type(Cypress.env("access.password"));

    cy.get("[data-cy=create]").click();
    cy.contains("Success");
  });

  it("successfully created author user", function () {
    cy.visit("/me/roles", {
      failOnStatusCode: false,
    });
    cy.get("[data-cy=create-user]").click();
    cy.get("[data-cy=username]").type(Cypress.env("author.username"));
    cy.get("[data-cy=firstname]").type(Cypress.env("author.first.name"));
    cy.get("[data-cy=lastname]").type(Cypress.env("author.last.name"));
    cy.get("[data-cy=email]").type(Cypress.env("author.email"));
    cy.get("[data-cy=phone]").type(Cypress.env("author.phone"));
    cy.get("[data-cy=postalAddress]").type(Cypress.env("author.postalAddress"));
    cy.get("[data-cy=password]").type(Cypress.env("author.password"));
    cy.get("[data-cy=passwordVerify]").type(Cypress.env("author.password"));

    cy.get("[data-cy=create]").click();
    cy.contains("Success");
  });

  it("successfully created contributor user", function () {
    cy.visit("/me/roles", {
      failOnStatusCode: false,
    });
    cy.get("[data-cy=create-user]").click();
    cy.get("[data-cy=username]").type(Cypress.env("contributor.username"));
    cy.get("[data-cy=firstname]").type(Cypress.env("contributor.first.name"));
    cy.get("[data-cy=lastname]").type(Cypress.env("contributor.last.name"));
    cy.get("[data-cy=email]").type(Cypress.env("contributor.email"));
    cy.get("[data-cy=phone]").type(Cypress.env("contributor.phone"));
    cy.get("[data-cy=postalAddress]").type(
      Cypress.env("contributor.postalAddress")
    );
    cy.get("[data-cy=password]").type(Cypress.env("contributor.password"));
    cy.get("[data-cy=passwordVerify]").type(
      Cypress.env("contributor.password")
    );

    cy.get("[data-cy=create]").click();
    cy.contains("Success");
  });

  it("successfully created reviewer user", function () {
    cy.visit("/me/roles", {
      failOnStatusCode: false,
    });
    cy.get("[data-cy=create-user]").click();
    cy.get("[data-cy=username]").type(Cypress.env("reviewer.username"));
    cy.get("[data-cy=firstname]").type(Cypress.env("reviewer.first.name"));
    cy.get("[data-cy=lastname]").type(Cypress.env("reviewer.last.name"));
    cy.get("[data-cy=email]").type(Cypress.env("reviewer.email"));
    cy.get("[data-cy=phone]").type(Cypress.env("reviewer.phone"));
    cy.get("[data-cy=postalAddress]").type(
      Cypress.env("reviewer.postalAddress")
    );
    cy.get("[data-cy=password]").type(Cypress.env("reviewer.password"));
    cy.get("[data-cy=passwordVerify]").type(Cypress.env("reviewer.password"));

    cy.get("[data-cy=create]").click();
    cy.contains("Success");
  });

  it("successfully created secretary user", function () {
    cy.visit("/me/roles", {
      failOnStatusCode: false,
    });
    cy.get("[data-cy=create-user]").click();
    cy.get("[data-cy=username]").type(Cypress.env("secretary.username"));
    cy.get("[data-cy=firstname]").type(Cypress.env("secretary.first.name"));
    cy.get("[data-cy=lastname]").type(Cypress.env("secretary.last.name"));
    cy.get("[data-cy=email]").type(Cypress.env("secretary.email"));
    cy.get("[data-cy=phone]").type(Cypress.env("secretary.phone"));
    cy.get("[data-cy=postalAddress]").type(
      Cypress.env("secretary.postalAddress")
    );
    cy.get("[data-cy=password]").type(Cypress.env("secretary.password"));
    cy.get("[data-cy=passwordVerify]").type(Cypress.env("secretary.password"));

    cy.get("[data-cy=create]").click();
    cy.contains("Success");
  });
});

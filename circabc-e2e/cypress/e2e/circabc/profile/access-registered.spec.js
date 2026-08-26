describe("The Update Profile Page", function () {
  beforeEach(function () {
    cy.login(
      Cypress.env("interest.group.admin.username"),
      Cypress.env("interest.group.admin.password")
    );
  });
  it("Give access to guest and verify synchronization on registered profile ", function () {
    cy.visit("me/roles", {
      failOnStatusCode: false,
    });
    cy.get(":nth-child(2) > .group-title").click();
    cy.wait(500);
    cy.get('[data-cy="members"]').click();
    cy.contains("Profiles").click();

    // giving access to guest profile
    cy.get(":nth-child(2) > .cell-name > .actions > :nth-child(2) > a").click();

    cy.get('input[matSliderThumb][formControlName="information"]')
      .invoke("val", "1")
      .trigger("input")
      .trigger("change");

    cy.get('input[matSliderThumb][formControlName="library"]')
      .invoke("val", "1")
      .trigger("input")
      .trigger("change");

    cy.get('input[matSliderThumb][formControlName="events"]')
      .invoke("val", "1")
      .trigger("input")
      .trigger("change");

    cy.get('input[matSliderThumb][formControlName="newsgroups"]')
      .invoke("val", "1")
      .trigger("input")
      .trigger("change");

    cy.get(".modal-footer > .buttons-group > .cta").click();
    cy.contains("Success");

    cy.visit("me/roles", {
      failOnStatusCode: false,
    });
    cy.get(":nth-child(2) > .group-title").click();

    cy.get('[data-cy="members"]').click();
    cy.contains("Profiles").click();

    // testing the registered profile sychronization
    cy.get(":nth-child(3) > .cell-name > .actions > :nth-child(2) > a").click();

    cy.get('input[matSliderThumb][formControlName="information"]').should(
      "have.value",
      "1"
    );

    cy.get('input[matSliderThumb][formControlName="library"]').should(
      "have.value",
      "1"
    );

    cy.get('input[matSliderThumb][formControlName="events"]').should(
      "have.value",
      "1"
    );

    cy.get('input[matSliderThumb][formControlName="newsgroups"]').should(
      "have.value",
      "1"
    );
  });

  it("Cut access to registered and and verify synchronization on guest profile ", function () {
    cy.visit("me/roles", {
      failOnStatusCode: false,
    });
    cy.get(":nth-child(2) > .group-title").click();
    cy.wait(500);
    cy.get('[data-cy="members"]').click();
    cy.contains("Profiles").click();

    cy.get(":nth-child(3) > .cell-name > .actions > :nth-child(2) > a").click();

    // revoking access for registered users
    cy.get('input[matSliderThumb][formControlName="information"]')
      .invoke("val", "0")
      .trigger("input")
      .trigger("change");

    cy.get('input[matSliderThumb][formControlName="library"]')
      .invoke("val", "0")
      .trigger("input")
      .trigger("change");

    cy.get('input[matSliderThumb][formControlName="events"]')
      .invoke("val", "0")
      .trigger("input")
      .trigger("change");

    cy.get('input[matSliderThumb][formControlName="newsgroups"]')
      .invoke("val", "0")
      .trigger("input")
      .trigger("change");

    cy.get(".modal-footer > .buttons-group > .cta").click();
    cy.contains("Success");
    cy.get('[style="float: right;"] > a').click();

    cy.visit("me/roles", {
      failOnStatusCode: false,
    });
    cy.get(":nth-child(2) > .group-title").click();

    cy.get('[data-cy="members"]').click();
    cy.contains("Profiles").click();

    // testing the registered profile sychronization
    cy.get(":nth-child(2) > .cell-name > .actions > :nth-child(2) > a").click();

    cy.get('input[matSliderThumb][formControlName="information"]').should(
      "have.value",
      "0"
    );

    cy.get('input[matSliderThumb][formControlName="library"]').should(
      "have.value",
      "0"
    );

    cy.get('input[matSliderThumb][formControlName="events"]').should(
      "have.value",
      "0"
    );
    cy.get('input[matSliderThumb][formControlName="newsgroups"]').should(
      "have.value",
      "0"
    );

    cy.get(".modal-footer > .buttons-group > .button").click();
  });

  afterEach(function () {
    cy.checkA11yWithLogging(undefined, undefined);
  });
});

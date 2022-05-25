describe("The Upload Page", function () {
  beforeEach(function () {
    cy.login(
      Cypress.env("interest.group.admin.username"),
      Cypress.env("interest.group.admin.password")
    );
  });
  it("successfully upload file", function () {
    cy.visit("me/roles", {
      failOnStatusCode: false,
    });
    cy.contains(Cypress.env("interest.group.title")).click();
    cy.contains("Library").click();
    cy.get("[data-cy=add]").click();
    cy.get("[data-cy=files]").click();

    const fileName = "files/CIRCABC_Leader_Guide.pdf";
    // const fileName = "files/test.txt";

    cy.fixture(fileName, "binary")
      .then(Cypress.Blob.binaryStringToBlob)
      .then((fileContent) => {
        cy.get('[data-cy="file-input"]').attachFile({
          fileContent,
          filePath: fileName,
          encoding: "utf-8",
          mimeType: "application/pdf",
        });
      });

    cy.get("[data-cy=upload]").click();

    cy.get("[data-cy=finish]").click();
  });
});

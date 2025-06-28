describe('IG Leader, User and Admin Permission Test', function () {
  const testFileName = 'CIRCABC_Leader_Guide.pdf';
  
  it('IG leader invites user to interest group', function () {
    // Login as the IG leader
    cy.login(
      Cypress.env('interest.group.admin.username'),
      Cypress.env('interest.group.admin.password'),
    );
    
    // Navigate to the interest group
    cy.visit('me/roles', {
      failOnStatusCode: false,
    });
    cy.contains(Cypress.env('interest.group.title')).click();
    
    // Go to members section
    cy.get('[data-cy="members"]').should('be.visible').click();
    
    // Click on actions and invite member
    cy.get('[data-cy="actions"]').should('be.visible').click();
    cy.get('[data-cy="invite-member"]').should('be.visible').click();
      // Search for the user to invite
    cy.get('[data-cy="search-user"]').type(Cypress.env('author.username'));
    cy.get('[data-cy="search-button"]').should('be.visible').click();
    cy.get('[data-cy=users]').select(
      Cypress.env('author.first.name') +
        ' ' +
        Cypress.env('author.last.name') +
        ' (' +
        Cypress.env('author.email') +
        ')',
    );
    cy.get('[data-cy=select]').should('be.visible').click();
    cy.wait(500);

    // Add a message and send notification
    cy.get('[ id="cmn-toggle-1"]').click();
    cy.get('[data-cy="message"]').type(
      'You have been invited to the interest group. Please upload a document.'
    );
    cy.get('[ id="cmn-toggle-2"]').should('be.visible').click();

    // Complete the invitation
    cy.get('[data-cy=ok]').should('be.visible').click();
    cy.contains('Success');

    // Logout
    cy.visit('login/logout', { failOnStatusCode: false });
  });
  it('Invited user uploads file and cuts inheritance', function () {
    // Login as the invited user
    cy.login(
      Cypress.env('author.username'),
      Cypress.env('author.password'),
    );
    
    // Navigate to the interest group
    cy.visit('me/roles', {
      failOnStatusCode: false,
    });
    cy.contains(Cypress.env('interest.group.title')).click();
    
    // Go to library section
    cy.get('[data-cy=library]').should('be.visible').click();
    
    // Upload a file
    cy.get('[data-cy=add]').click();
    cy.get('[data-cy=files]').click();

    const fileName = 'cypress/fixtures/files/CIRCABC_Leader_Guide.pdf';
    cy.get('[data-cy="file-input"]').selectFile(fileName, { force: true });
    cy.get('[data-cy=upload]').click();
    cy.get('[data-cy=finish]').click();
    
    // Verify the file was uploaded
    cy.contains(testFileName).should('be.visible');
    
    // Click on the file to see details
    cy.contains(testFileName).click();
    
    // Navigate to permissions tab
    cy.get('[data-cy="permissions"]').should('be.visible').click();
    
    // Cut inheritance
    cy.get('.nonBlockLabel').should('be.visible').click();

    
    // Wait for success message
    cy.contains('Success').should('be.visible');
    
    // Logout
    cy.visit('login/logout', { failOnStatusCode: false });
  });

  it('IG admin can still view file details after inheritance cut', function () {
    // Login as IG admin
    cy.login(
      Cypress.env('interest.group.admin.username'),
      Cypress.env('interest.group.admin.password'),
    );
    
    // Navigate to the interest group
    cy.visit('me/roles', {
      failOnStatusCode: false,
    });
    cy.contains(Cypress.env('interest.group.title')).click();
    
    // Go to library section
    cy.get('[data-cy=library]').should('be.visible').click();
    
    // Verify file is visible
    cy.contains(testFileName).should('be.visible');
    
    // Click on the file to see details
    cy.contains(testFileName).click();
    
   
    
    // Logout
    cy.visit('login/logout', { failOnStatusCode: false });
  });

  afterEach(function () {
    cy.checkA11yWithLogging(undefined, undefined);
  });
});
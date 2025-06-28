describe('User Contributor Control Test', function () {
  const testFileName = 'CIRCABC_Leader_Guide.pdf';
    before(function () {
    // First login as IG leader/admin and invite the contributor user
    cy.login(
      Cypress.env('interest.group.admin.username'),
      Cypress.env('interest.group.admin.password'),
    );
    
    // Go to the interest group
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
    cy.get('[data-cy="search-user"]').type(Cypress.env('contributor.username'));
    cy.get('[data-cy="search-button"]').should('be.visible').click();
    cy.get('[data-cy=users]').select(
      Cypress.env('contributor.first.name') +
        ' ' +
        Cypress.env('contributor.last.name') +
        ' (' +
        Cypress.env('contributor.email') +
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
  });  it('Invited user can upload a file', function () {
    // Login as the invited user
    cy.login(
      Cypress.env('contributor.username'),
      Cypress.env('contributor.password'),
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
    
    // Go to details page of the uploaded file
    cy.contains(testFileName).click();
    
    // Store the URL of the details page for later use
    cy.url().then((url) => {
      // Store the details page URL in Cypress env variable
      Cypress.env('fileDetailsUrl', url+'/details');
      cy.log('File details URL stored: ' + url);
    });
    
    // Logout
    cy.visit('login/logout', { failOnStatusCode: false });
  });  it('Contributor user can unsubscribe from interest group', function () {
    // Login as the contributor user
    cy.login(
      Cypress.env('contributor.username'),
      Cypress.env('contributor.password'),
    );
    
    // Navigate to the roles/memberships page
    cy.visit('me/roles', {
      failOnStatusCode: false,
    });
    
    cy.get('.profile > a').should('be.visible').click();
    cy.get('[data-cy="ok"]').should('be.visible').click();
    
    // Verify the interest group is no longer in the user's roles
    cy.visit('me/roles', {
      failOnStatusCode: false,
    });
    cy.contains(Cypress.env('interest.group.title')).should('not.exist');
    
    // Logout
    cy.visit('login/logout', { failOnStatusCode: false });
  });  it('Removed user cannot access document details', function () {
    // Login as the removed user
    cy.login(
      Cypress.env('contributor.username'),
      Cypress.env('contributor.password'),
    );
    
    // Attempt to navigate to the interest group
    cy.visit('me/roles', {
      failOnStatusCode: false,
    });
    
    // Assert that the user cannot see the interest group in their roles
    cy.contains(Cypress.env('interest.group.title')).should('not.exist');
    
    // Try to directly access the library (this should fail or redirect)
    cy.visit('group/' + Cypress.env('interest.group.name') + '/library', {
      failOnStatusCode: false,
    });
    
    // Assert the user cannot see the document they uploaded
    cy.contains(testFileName).should('not.exist');
    
    // Try to directly access the details page URL that we stored earlier
    cy.log('Attempting to access file details URL: ' + Cypress.env('fileDetailsUrl'));
    cy.visit(Cypress.env('fileDetailsUrl'), {
      failOnStatusCode: false,
    });
    
    // Verify the user can't access the file details (either redirected or access denied)
    cy.contains(testFileName).should('not.exist');
    
    // Additional check: verify they get access denied or are redirected appropriately
    cy.url().should('not.include', 'details');
  });

  afterEach(function () {
    cy.checkA11yWithLogging(undefined, undefined);
  });
});
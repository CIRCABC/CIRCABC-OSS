// ***********************************************
// This example commands.js shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --
// Cypress.Commands.add("login", (email, password) => { ... })
//
//
// -- This is a child command --
// Cypress.Commands.add("drag", { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add("dismiss", { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This is will overwrite an existing command --
// Cypress.Commands.overwrite("visit", (originalFn, url, options) => { ... })
import 'cypress-file-upload';

Cypress.Commands.add('setPassword', (username, password) => {
  cy.request({
    method: 'POST',
    url:
      `http://${Cypress.env('host')}:${Cypress.env('port')}${Cypress.env('path')}/service/api/person/changepassword/${username}`,
    body: {
      newpw: password,
    },
    headers: { Authorization: 'Basic ' + btoa('admin:admin') },
  });
});

Cypress.Commands.add('login', (username, password) => {
  // accept cookies
  window.localStorage.setItem('allow-cookies', 'yes');

  cy.request({
    method: 'POST',
    url:    `http://${Cypress.env('host')}:${Cypress.env('port')}${Cypress.env('path')}/service/api/login?guest=true`,
    body: {
      username: username,
      password: password,
    },
  }).then((resp) => {
    window.sessionStorage.setItem('ticket', resp.body.data.ticket);

    cy.request({
      method: 'GET',
      url:
        `http://${Cypress.env('host')}:${Cypress.env('port')}${Cypress.env('path')}/service/circabc/users/${username}`,
      headers: { Authorization: 'Basic ' + btoa(resp.body.data.ticket) },
    }).then((resp) => {
      let user = resp.body;
      window.sessionStorage.setItem('user.userId', user.userId);
      window.sessionStorage.setItem('user.email', user.email);
      window.sessionStorage.setItem('user.avatar', user.avatar);
      window.sessionStorage.setItem('user.firstname', user.firstname);
      window.sessionStorage.setItem('user.lastname', user.lastname);
      window.sessionStorage.setItem('user.phone', user.phone);
      window.sessionStorage.setItem(
        'user.contentFilterLang',
        user.contentFilterLang
      );
      window.sessionStorage.setItem('user.uiLang', user.uiLang);
      if (user.visibility) {
        window.sessionStorage.setItem(
          'user.visibility',
          user.visibility.toString()
        );
      }
      window.sessionStorage.setItem(
        'user.properties',
        JSON.stringify(user.properties)
      );
    });
  });
});

Cypress.Commands.add('logout', () => {
  let ticket = window.sessionStorage.getItem('ticket');
  if (ticket !== null) {
    cy.request({
      method: 'DELETE',
      url: '../service/api/login/ticket/' + ticket + '?format=json',
      headers: { Authorization: 'Basic ' + btoa(ticket) },
    }).then((resp) => {
      if (resp) {
        window.sessionStorage.removeItem('ticket');
        window.sessionStorage.removeItem('user.userId');
        window.sessionStorage.removeItem('user.email');
        window.sessionStorage.removeItem('user.avatar');
        window.sessionStorage.removeItem('user.first.name');
        window.sessionStorage.removeItem('user.last.name');
        window.sessionStorage.removeItem('user.phone');
        window.sessionStorage.removeItem('user.contentFilterLang');
        window.sessionStorage.removeItem('user.uiLang');
        window.sessionStorage.removeItem('user.visibility');
        window.sessionStorage.removeItem('user.properties');
      }
    });
  }
});

import 'cypress-axe';

const logA11yViolations = (violations) => {
  cy.task('log', `${violations.length} accessibility violation(s) detected.`);
  violations.forEach(({ id, impact, description, nodes }) => {
    cy.task('log', `[${impact}] ${id}: ${description}`);
    nodes.forEach(({ html }) => {
      cy.task('log', `Affected Node: ${html}`);
    });
  });
  cy.wait(500);
};

// Add violationCallback as a custom command
// @ts-ignore
Cypress.Commands.add('checkA11yWithLogging', (context, options) => {

  if (Cypress.env('timeout') > 0 ) {
    cy.wait(Cypress.env('timeout'));
  }
  if (!Cypress.env('runA11y')) {
    return;
  }
  cy.injectAxe();
  if (Cypress.env('logA11yFailures')) {
    // @ts-ignore
    cy.checkA11y(
      context,
      options,
      logA11yViolations,
      Cypress.env('skipA11yFailures')
    );
  } else {
    // @ts-ignore
    cy.checkA11y(context, options, undefined, Cypress.env('skipA11yFailures'));
  }
  
});

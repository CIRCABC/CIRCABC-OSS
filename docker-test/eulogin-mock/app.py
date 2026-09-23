"""
EU Login (ECAS) CAS mock for CircaBC — local development only.

CircaBC's backend uses the CLASSIC CAS protocol via
/alfresco/service/circabc/eulogin:

  1. The backend redirects the browser to:
        {CAS_BASE_URL}/login?service=<serviceUrl>
  2. This mock shows a login form. On submit it redirects back to <serviceUrl>
     with a one-time service ticket:  <serviceUrl>?ticket=ST-xxxx (&route=...)
  3. The backend validates the ticket by calling:
        {CAS_BASE_URL}/laxValidate?service=<serviceUrl>&ticket=ST-xxxx&userDetails=true
     and expects a CAS 2.0 XML response with EU Login user attributes as direct
     children of <cas:authenticationSuccess> (cas:email, cas:firstName, ...).

Runs on https://0.0.0.0:7002 with a self-signed certificate generated at build
time (no private key is committed).

Test users are defined in USERS below. DEV ONLY — do not use in production.
"""

import json
import ssl
import uuid
from urllib.parse import urlencode, urlsplit, urlunsplit, parse_qsl
from xml.sax.saxutils import escape

from flask import Flask, request, redirect, Response, render_template_string

app = Flask(__name__)

# ── Test users (dev only) ──
# Keyed by username; each entry mirrors the EU Login attributes CircaBC reads.
USERS = {
    "admin":       {"password": "admin",     "email": "admin@example.org",         "firstName": "Admin",  "lastName": "User",    "domain": "eu.europa.ec"},
    "bournja":     {"password": "Admin123",  "email": "jason.bourne@example.org",  "firstName": "Jason",  "lastName": "Bourne",  "domain": "eu.europa.ec"},
    "chucknorris": {"password": "Qwerty098", "email": "chuck.norris@example.org",  "firstName": "Chuck",  "lastName": "Norris",  "domain": "eu.europa.ec"},
    "smithja":     {"password": "Test1234",  "email": "jane.smith@example.org",    "firstName": "Jane",   "lastName": "Smith",   "domain": "eu.europa.ec"},
    "garciam":     {"password": "Test1234",  "email": "maria.garcia@example.org",  "firstName": "Maria",  "lastName": "Garcia",  "domain": "external"},
}

# One-time service tickets: ticket -> username
TICKETS = {}

LOGIN_PAGE = """
<!doctype html>
<html lang="en"><head><meta charset="utf-8"><title>EU Login (mock)</title>
<style>
 body{font-family:sans-serif;background:#f4f6f9;display:flex;justify-content:center;padding-top:60px}
 .box{background:#fff;border:1px solid #ddd;border-radius:8px;padding:32px;width:360px;box-shadow:0 2px 8px rgba(0,0,0,.08)}
 h1{font-size:18px;color:#004494;margin-top:0}
 label{display:block;margin:12px 0 4px;font-size:13px}
 input,select{width:100%;padding:8px;box-sizing:border-box;border:1px solid #ccc;border-radius:4px;background:#fff}
 button{margin-top:18px;width:100%;padding:10px;background:#004494;color:#fff;border:0;border-radius:4px;cursor:pointer;font-size:14px}
 .hint{margin-top:16px;font-size:12px;color:#666}
 .err{color:#b00;font-size:13px;margin-top:8px}
</style></head>
<body><div class="box">
 <h1>EU Login (mock)</h1>
 {% if error %}<div class="err">{{ error }}</div>{% endif %}
 <form method="post">
  <input type="hidden" name="service" value="{{ service }}">

  <label for="userselect">Test user</label>
  <select id="userselect">
   <option value="">— choose a user —</option>
   {% for uid in usernames %}<option value="{{ uid }}">{{ uid }}</option>{% endfor %}
  </select>

  <label for="username">Username</label>
  <input id="username" name="username" autocomplete="off" autofocus>
  <label for="password">Password</label>
  <input id="password" name="password" type="password" autocomplete="off">
  <button type="submit">Sign in</button>
 </form>
 <div class="hint">Dev mock — pick a user to auto-fill the password, or type your own.</div>
</div>
<script>
 // Map of username -> password, injected by the server (dev mock only).
 var CREDS = {{ creds_json | safe }};
 var sel = document.getElementById('userselect');
 var u = document.getElementById('username');
 var p = document.getElementById('password');
 sel.addEventListener('change', function () {
   var name = sel.value;
   if (name) { u.value = name; p.value = CREDS[name] || ''; }
 });
</script>
</body></html>
"""


def _append_ticket(service_url: str, ticket: str) -> str:
    """Append ?ticket=... to the service URL, preserving existing query params."""
    parts = urlsplit(service_url)
    query = dict(parse_qsl(parts.query, keep_blank_values=True))
    query["ticket"] = ticket
    return urlunsplit(
        (parts.scheme, parts.netloc, parts.path, urlencode(query), parts.fragment)
    )


def _render_login(service, error=None, status=200):
    """Render the login page with the dropdown of test users and their passwords."""
    creds = {name: u["password"] for name, u in USERS.items()}
    html = render_template_string(
        LOGIN_PAGE,
        service=service,
        error=error,
        usernames=list(USERS.keys()),
        creds_json=json.dumps(creds),
    )
    return (html, status) if status != 200 else html


@app.route("/cas/login", methods=["GET", "POST"])
def login():
    service = request.values.get("service", "")
    if request.method == "GET":
        return _render_login(service)

    username = request.form.get("username", "").strip()
    password = request.form.get("password", "")
    user = USERS.get(username)
    if not user or user["password"] != password:
        return _render_login(service, error="Invalid username or password", status=401)

    ticket = "ST-" + uuid.uuid4().hex
    TICKETS[ticket] = username
    if not service:
        return f"Authenticated as {username}. No service URL supplied.", 200
    return redirect(_append_ticket(service, ticket), code=302)


@app.route("/cas/laxValidate")
@app.route("/cas/serviceValidate")
def lax_validate():
    ticket = request.args.get("ticket", "")
    username = TICKETS.pop(ticket, None)  # single-use

    if not username:
        xml = (
            '<cas:serviceResponse xmlns:cas="http://www.yale.edu/tp/cas">'
            '<cas:authenticationFailure code="INVALID_TICKET">'
            "ticket not recognized</cas:authenticationFailure>"
            "</cas:serviceResponse>"
        )
        return Response(xml, mimetype="application/xml")

    u = USERS[username]
    # EU Login/ECAS returns attributes as direct children of authenticationSuccess.
    xml = (
        '<cas:serviceResponse xmlns:cas="http://www.yale.edu/tp/cas">'
        "<cas:authenticationSuccess>"
        f"<cas:user>{escape(username)}</cas:user>"
        f"<cas:email>{escape(u['email'])}</cas:email>"
        f"<cas:firstName>{escape(u['firstName'])}</cas:firstName>"
        f"<cas:lastName>{escape(u['lastName'])}</cas:lastName>"
        f"<cas:domain>{escape(u['domain'])}</cas:domain>"
        f"<cas:moniker>{escape(username)}</cas:moniker>"
        "</cas:authenticationSuccess>"
        "</cas:serviceResponse>"
    )
    return Response(xml, mimetype="application/xml")


@app.route("/cas")
@app.route("/cas/")
def index():
    return "EU Login CAS mock is running. Endpoints: /cas/login, /cas/laxValidate", 200


if __name__ == "__main__":
    context = ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)
    context.load_cert_chain("/app/cert.pem", "/app/key.pem")
    app.run(host="0.0.0.0", port=7002, ssl_context=context, debug=False)

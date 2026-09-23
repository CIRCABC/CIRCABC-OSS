"""Generate self-signed certificate for the EU Login mockup server."""
from cryptography import x509
from cryptography.x509.oid import NameOID
from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import rsa
import datetime

key = rsa.generate_private_key(public_exponent=65537, key_size=2048)

subject = issuer = x509.Name([
    x509.NameAttribute(NameOID.COUNTRY_NAME, "BE"),
    x509.NameAttribute(NameOID.STATE_OR_PROVINCE_NAME, "Brussels"),
    x509.NameAttribute(NameOID.ORGANIZATION_NAME, "European Commission"),
    x509.NameAttribute(NameOID.ORGANIZATIONAL_UNIT_NAME, "DIGIT"),
    x509.NameAttribute(NameOID.COMMON_NAME, "EU Login Mock-Up Certificate Authority (Dev Only)"),
])

cert = (
    x509.CertificateBuilder()
    .subject_name(subject)
    .issuer_name(issuer)
    .public_key(key.public_key())
    .serial_number(x509.random_serial_number())
    .not_valid_before(datetime.datetime.utcnow())
    .not_valid_after(datetime.datetime.utcnow() + datetime.timedelta(days=3650))
    .add_extension(x509.SubjectAlternativeName(
        [x509.DNSName("eulogin"), x509.DNSName("localhost")]
        + [
            (x509.IPAddress(__import__("ipaddress").ip_address(s))
             if s and s[0].isdigit() else x509.DNSName(s))
            for s in __import__("os").environ.get("EXTRA_SANS", "").split(",")
            if s.strip()
        ]
    ), critical=False)
    .sign(key, hashes.SHA256())
)

with open("/app/cert.pem", "wb") as f:
    f.write(cert.public_bytes(serialization.Encoding.PEM))

with open("/app/key.pem", "wb") as f:
    f.write(key.private_bytes(
        serialization.Encoding.PEM,
        serialization.PrivateFormat.TraditionalOpenSSL,
        serialization.NoEncryption(),
    ))

# Also write to certs/ for the Java truststore import
import os
os.makedirs("/app/certs", exist_ok=True)
with open("/app/certs/eulogin.crt", "wb") as f:
    f.write(cert.public_bytes(serialization.Encoding.PEM))

print("✅ Generated self-signed certificate for EU Login mockup")

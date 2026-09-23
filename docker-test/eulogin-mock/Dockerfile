FROM python:3.11-slim

RUN pip install --no-cache-dir flask cryptography PyJWT requests

WORKDIR /app
COPY app.py .
COPY generate_certs.py .

# Optional extra Subject Alternative Names (comma-separated, DNS or IP) for the
# self-signed cert — useful when the mock must be reached at a host IP.
ARG EXTRA_SANS=""
ENV EXTRA_SANS=${EXTRA_SANS}

# Always generate a fresh self-signed certificate at build time.
# No private key is committed to the repository (dev-only, throwaway cert).
RUN python generate_certs.py

EXPOSE 7002

CMD ["python", "app.py"]

#!/bin/bash

set -e

NVD_API_KEY="${1:-}"
VAULT_ADDR="${VAULT_ADDR:-http://localhost:8200}"
VAULT_TOKEN="${VAULT_TOKEN:-dev-token}"

echo "Setting up Vault..."
echo "Vault address: $VAULT_ADDR"
echo "Using NVD API key: ${NVD_API_KEY:0:8}..."

if ! curl -s "$VAULT_ADDR/v1/sys/health" > /dev/null 2>&1; then
    echo "Error: Vault is not running at $VAULT_ADDR"
    echo "Please start Vault first: docker compose up -d vault"
    exit 1
fi

export VAULT_ADDR
export VAULT_TOKEN

if ! vault secrets list | grep -q "secret/"; then
    echo "Enabling KV v2 secrets engine..."
    vault secrets enable -version=2 -path=secret kv
fi

echo "Storing NVD API key in Vault..."
vault kv put secret/insecure-bank \
    nvd.api.key="$NVD_API_KEY" \
    || vault kv put secret/insecure-bank \
        api-key="$NVD_API_KEY"

echo "Verifying stored key..."
STORED_KEY=$(vault kv get -field=nvd.api.key secret/insecure-bank 2>/dev/null || \
             vault kv get -field=api-key secret/insecure-bank 2>/dev/null || \
             echo "")

if [ -n "$STORED_KEY" ]; then
    echo "✓ Successfully stored NVD API key in Vault"
    echo "  Key length: ${#STORED_KEY} characters"
    echo "  First 8 chars: ${STORED_KEY:0:8}..."
else
    echo "⚠ Warning: Could not verify stored key"
fi

echo ""
echo "Vault setup complete!"
echo ""
echo "To use Vault in GitHub Actions, add these secrets:"
echo "  VAULT_URI: $VAULT_ADDR"
echo "  VAULT_TOKEN: $VAULT_TOKEN"
echo ""
echo "For production, use a proper Vault server and token with limited permissions."

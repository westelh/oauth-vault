#!/bin/bash
export VAULT_ADDR="http://127.0.0.1:8200"

vault secrets enable  -version=2 kv
vault kv put -mount=kv secrets/oauth-vault @config-data.json
vault policy write oauth-vault config-read-policy.tf
vault policy write oauth-vault-oidc-client policy-oidc-client.tf

vault write auth/kubernetes/role/colima policies=oauth-vault,oauth-vault-oidc-client

vault write identity/oidc/client/oauth-vault \
  redirect_uris="http://127.0.0.1:8080" \
  assignments="allow_all"

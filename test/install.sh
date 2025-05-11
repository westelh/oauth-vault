#!/bin/bash
VAULT_ADDR="http://127.0.0.1:8200"

vault secrets enable -address=$VAULT_ADDR -version=2 kv
vault kv put -address=$VAULT_ADDR -mount=kv secrets/oauth-vault @config-data.json
vault policy write -address=$VAULT_ADDR oauth-vault config-read-policy.tf
vault write auth/kubernetes/role/colima -address=$VAULT_ADDR policies=oauth-vault


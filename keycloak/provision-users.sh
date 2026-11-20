#!/bin/sh
set -eu

kcadm=/opt/keycloak/bin/kcadm.sh
realm=helpdesk-realm

"$kcadm" config credentials \
  --server http://keycloak:8080 \
  --realm master \
  --user "$KEYCLOAK_ADMIN" \
  --password "$KEYCLOAK_ADMIN_PASSWORD"

ensure_user() {
  username=$1
  email=$2
  first_name=$3
  last_name=$4
  role=$5
  password=$6

  if "$kcadm" get users -r "$realm" -q "username=$username" --fields username --format csv --noquotes |
    grep -Fxq "$username"; then
    echo "Keycloak user '$username' already exists"
  else
    "$kcadm" create users -r "$realm" \
      -s "username=$username" \
      -s "email=$email" \
      -s "firstName=$first_name" \
      -s "lastName=$last_name" \
      -s enabled=true \
      -s emailVerified=true
    "$kcadm" set-password -r "$realm" \
      --username "$username" \
      --new-password "$password"
    echo "Created Keycloak user '$username'"
  fi

  user_id=$("$kcadm" get users -r "$realm" -q "username=$username" --fields id --format csv --noquotes | tail -n 1)
  if [ -z "$user_id" ]; then
    echo "Could not resolve Keycloak user '$username'" >&2
    exit 1
  fi
  "$kcadm" update "users/$user_id" -r "$realm" \
    -s "email=$email" \
    -s "firstName=$first_name" \
    -s "lastName=$last_name" \
    -s emailVerified=true

  if ! "$kcadm" get-roles -r "$realm" --uusername "$username" --fields name --format csv --noquotes |
    grep -Fxq "$role"; then
    "$kcadm" add-roles -r "$realm" --uusername "$username" --rolename "$role"
  fi
}

ensure_user admin admin@helpdesk.local System Administrator ADMIN "$HELPDESK_ADMIN_PASSWORD"
ensure_user agent1 agent1@helpdesk.local Helpdesk Agent AGENT "$HELPDESK_AGENT_PASSWORD"
ensure_user user1 user1@helpdesk.local Helpdesk User USER "$HELPDESK_USER_PASSWORD"

ensure_ui_role_mapper() {
  client_id=$("$kcadm" get clients -r "$realm" -q clientId=helpdesk-ui-client --fields id --format csv --noquotes |
    tail -n 1)
  if [ -z "$client_id" ]; then
    echo "Could not resolve the helpdesk UI client in Keycloak" >&2
    exit 1
  fi

  mapper_path="clients/$client_id/protocol-mappers/models"
  if "$kcadm" get "$mapper_path" -r "$realm" --fields name --format csv --noquotes |
    grep -Fxq "realm roles in ID token"; then
    return
  fi

  "$kcadm" create "$mapper_path" -r "$realm" \
    -s "name=realm roles in ID token" \
    -s protocol=openid-connect \
    -s protocolMapper=oidc-usermodel-realm-role-mapper \
    -s consentRequired=false \
    -s 'config."user.attribute"=realmRoles' \
    -s 'config."introspection.token.claim"=true' \
    -s 'config."access.token.claim"=true' \
    -s 'config."claim.name"=realm_access.roles' \
    -s 'config."jsonType.label"=String' \
    -s 'config.multivalued=true' \
    -s 'config."id.token.claim"=true' \
    -s 'config."userinfo.token.claim"=true'
}

ensure_ui_role_mapper

#!/bin/sh
set -eu

context="${MOSHI_CONTEXT_PATH:-ROOT}"
case "$context" in
    ''|.|..|*[!A-Za-z0-9_-]*)
        printf 'Invalid MOSHI_CONTEXT_PATH: %s\n' "$context" >&2
        exit 1
        ;;
esac

source_war="${CATALINA_HOME}/webapps/ROOT.war"
target_war="${CATALINA_HOME}/webapps/${context}.war"
if [ "$context" != "ROOT" ] && [ -f "$source_war" ]; then
    mv "$source_war" "$target_war"
fi

exec "${CATALINA_HOME}/bin/catalina.sh" run

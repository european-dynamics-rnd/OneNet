#!/bin/bash

if ! keytool -import -trustcacerts -keystore /opt/java/openjdk/lib/security/cacerts -storepass changeit -noprompt -alias dataAppCert -file /cert/execution_core_container.cer; then
    echo "---"
fi
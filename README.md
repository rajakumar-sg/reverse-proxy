# reverse-proxy
Jetty Reverse Proxy example


## Generate server keystore jks

keytool -genkey -v -keystore my-release-key.keystore -alias alias_name -keyalg RSA -keysize 2048 -validity 10000

## Generate personal cert
keytool -genkey -alias my-name \
    -keystore cert.pfx \
    -storetype pkcs12 \
    -keyalg RSA \
    -storepass secret123 \
    -validity 730 \
    -keysize 2048 
#!/bin/bash

mkdir -p dist
cd dist

rm -f als-node-client.min.js

echo 'SHACLValidator = require("amf-shacl-node")' > als-node-client.min.js
echo 'Ajv = require("ajv")' >> als-node-client.min.js
cat ../../target/artifact/als-node-client.js >> als-node-client.min.js
sed  -i".bk" -e "s,../../src/main/resources/wasm_exec.js,./wasm_exec.js," als-node-client.min.js
sed  -i".bk" -e "s,\"als-server/js/node-package/main.wasm\",__dirname + \"/main.wasm\"," als-node-client.min.js
rm -f als-node-client.min.js.bk
chmod a+x als-node-client.min.js

cd ..
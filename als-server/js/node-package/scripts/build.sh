#!/bin/bash

mkdir -p lib
cd lib

rm -f als-server.js

echo 'SHACLValidator = require("amf-shacl-node")' > als-server.js
echo 'Ajv = require("ajv")' >> als-server.js
cat ../../target/artifact/als-server.js >> als-server.js
sed  -i".bk" -e "s,../../src/main/resources/wasm_exec.js,../wasm_exec.js," als-server.js
rm -f als-server.js.bk
chmod a+x als-server.js

rm -f als-server.min.js

echo 'SHACLValidator = require("amf-shacl-node")' > als-server.min.js
echo 'Ajv = require("ajv")' >> als-server.min.js
cat ../../target/artifact/als-server.min.js >> als-server.min.js
sed  -i".bk" -e "s,../../src/main/resources/wasm_exec.js,../wasm_exec.js," als-server.min.js
rm -f als-server.min.js.bk
chmod a+x als-server.min.js

cd ..

npm run build:dist

exit $?

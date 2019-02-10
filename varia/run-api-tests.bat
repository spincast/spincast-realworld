
newman run Conduit.postman_collection.json --insecure --delay-request 500 --global-var "APIURL=https://localhost:12345/api" --global-var "USERNAME=toto" --global-var "EMAIL=toto@example.com" --global-var "PASSWORD=12341234"

import * as http from "http";

export function start() {
    http.createServer(function (req, res) {
        res.write('Online!');
        res.end();
    }).listen(8080);
}
/**
 * Usage node indexKnowledgeBase.js whole_knowledge_base_path.txt
 */

var redis = require("redis");
var LineByLineReader = require('line-by-line');
var lr = new LineByLineReader(process.argv[2]);
var i = 0;

client = redis.createClient();

client.select(5, function() {
    lr.on('error', function (err) {
        console.error("Error: " + err);
    });

    lr.on('line', function (line) {
        lr.pause();
        if (i++ % 10000 === 0) {
            console.log("Processing line " + i + ": " + (line.match(/wiki_title="(.*?)"/)[1]) + " ... " + (line.match(/id="(.*?)"/)[1]));
        }
        client.set(line.match(/wiki_title="(.*?)"/)[1], line.match(/id="(.*?)"/)[1]);
        lr.resume();
//        // pause emitting of lines...
//
//        // ...do your asynchronous line processing..
//        setTimeout(function () {
//
//            // ...and continue emitting lines.
//            lr.resume();
//        }, 100);
    });

    lr.on('end', function () {
        console.log("All lines read");
    });

//    client.set(row.fromPage, row.toPage);
});
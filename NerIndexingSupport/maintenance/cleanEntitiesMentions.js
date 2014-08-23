var csv = require('ya-csv');
var fs = require('fs');
var redis = require("redis");

var client = redis.createClient();

var reader = csv.createCsvFileReader(__dirname + '/../csv/encsv/entities_mentions.csv', {
    'separator': ',',
    'quote': '"',
    'escape': '"',
    'nestedQuotes': true
});
var writer = csv.createCsvStreamWriter(fs.createWriteStream(__dirname + '/../csv/encsv/entities_mentions_cleaned.csv'));
var index = 0;

reader.addListener('data', function(csvRow) {
    var pageTitle = unescape(csvRow[0].replace("http://en.wikipedia.org/wiki/", ""));

//    reader.pause();
    
    client.get(pageTitle, function(err, reply) {
        index++;
//        index--;
        if (err) {
            throw err;
        }
        
        if ((index % 100000) == 0) {
            console.log("Checking page " + index + " result " + reply);
        }
        
        if (reply) {
            writer.writeRecord(csvRow);
//            console.log("Page " + index + " not in database ... " + pageTitle);
        }
    });
        
});

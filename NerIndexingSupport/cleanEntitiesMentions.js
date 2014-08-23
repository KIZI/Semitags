var csv = require('csv');
var fs = require('fs');
var redis = require("redis");

var client = redis.createClient();


csv().from(fs.createReadStream(__dirname + '/../csv/encsv/entities_mentions.csv'), {
    delimiter : ',',
    escape: '"',
    relax: true
})
.to.stream(fs.createWriteStream(__dirname + '/../csv/encsv/entities_mentions_cleaned_2.csv'))
.transform( function(csvRow, index, callback){

    var pageTitle = csvRow[0].replace("http://en.wikipedia.org/wiki/", "");
    
    client.get(pageTitle, function(err, reply) {
        if (err) {
            throw err;
        }
        
        if ((index % 100000) == 0) {
            console.log("Checking page " + index + " result " + reply);
        }
        
        if (reply) {
            callback(null, csvRow);
        } else {
            callback();
        }
    });    
    
}, {parallel: 100})
.on('parse_error', function(row){ 
    console.log("Parsing error %j", row); 
    return row.split(',') ;
})
.on('close', function(count) {
    // when writing to a file, use the 'close' event
    // the 'end' event may fire before the file has been written
    console.log('Read all lines: ' + count);
    process.exit(0);
}).on('error', function(error) {
    console.log("ERROR: " + error.message);
    process.exit(1);
});

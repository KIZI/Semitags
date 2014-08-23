var csv = require('csv');
var fs = require('fs');
var redis = require("redis");

var client = redis.createClient();
var maxRedirects = 0;

function removeRedirect(csvRow, index, prevRedirects, callback) {
    var pageTitle = csvRow[0].replace("http://en.wikipedia.org/wiki/", "");
    
    client.get(pageTitle, function(err, reply) {
        if (err) {
            throw err;
        }
        
        if ((index % 100000) === 0) {
            console.log("Checking redirects for page " + index + " result " + reply);
            console.log("Max redirects so far: " + maxRedirects);
        }
        
        if (reply && (prevRedirects.indexOf(reply) < 0)) {
            if (prevRedirects.length > maxRedirects) {
                maxRedirects = prevRedirects.length;
            }
//            console.log("Found Redirect " + csvRow[0] + " to " + reply);
            csvRow[0] = "http://en.wikipedia.org/wiki/" + reply;
            prevRedirects.push(reply);
            removeRedirect(csvRow, index, prevRedirects, callback);
        } else {
            callback(null, csvRow);
        }
    });    
}


client.select(1, function() {
    csv().from(fs.createReadStream(__dirname + '/../csv/encsv/entities_mentions_cleaned.csv'), {
        delimiter : ',',
        escape: '"',
        relax: true
    })
    .to.stream(fs.createWriteStream(__dirname + '/../csv/encsv/entities_mentions_no_redirects.csv'))
    .transform( function(csvRow, index, callback){
    
            removeRedirect(csvRow, index, [], callback);
        
    }, {parallel: 100})
    .on('parse_error', function(row){ 
        console.log("Parsing error %j", row); 
        return row.split(',') ;
    })
    .on('close', function(count) {
        // when writing to a file, use the 'close' event
        // the 'end' event may fire before the file has been written
        console.log("Max redirects: " + maxRedirects);
        console.log('Read all lines: ' + count);
        process.exit(0);
    }).on('error', function(error) {
        console.log("ERROR: " + error.message);
        process.exit(1);
    });
});

var csv = require('csv');
var fs = require('fs');
var redis = require("redis");
var normalizer = require(__dirname + "/../utils/normalizer.js");

var client = redis.createClient();

var lang = process.argv[2];

csv().from(fs.createReadStream(__dirname + '/../../csv/' + lang + 'csv/entities_mentions_no_redirects.csv'), {
    delimiter : ',',
    escape: '"',
    relax: true
})
.to.stream(fs.createWriteStream(__dirname + '/../../csv/' + lang + 'csv/normalizedSurfaceForms.csv'))
.transform( function(csvRow, index, callback){
    csvRow[1] = normalizer.normalize(csvRow[1]).trim();
    
    if (csvRow[1]) {
        callback(null, csvRow);
    } else {
        callback();
    }
    
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

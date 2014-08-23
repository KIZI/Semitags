var mysql = require('mysql');
var csv = require('csv');
var fs = require('fs');

var connection = mysql.createConnection({
    host : 'localhost',
    user : 'ner',
    password : 'ner',
    database: 'ner'
});


csv().from(fs.createReadStream(__dirname + '/../csv/encsv/entities_mentions.csv'), {
    delimiter : ',',
    escape : '"'
})
.to.stream(fs.createWriteStream(__dirname + '/../csv/encsv/entities_mentions_cleaned.csv'))
.transform( function(csvRow, index, callback){

    var pageTitle = csvRow[0].replace("http://en.wikipedia.org/wiki/", "");
    
    connection.query('SELECT * FROM page WHERE page_title LIKE ?', [ pageTitle ], function(err, rows, fields) {
        if (err) {
            throw err;
        }

        if (rows.length <= 0) {
            console.log("Page " + index + " not in database ... " + pageTitle);
            callback();
        } else {
            if (rows[0].page_is_redirect) {
                console.log("Page " + index + " is redirect ... " + pageTitle);
            }
            callback(null, csvRow);
        }
    });    
    
}, {parallel: 5})
.on('close', function(count) {
    // when writing to a file, use the 'close' event
    // the 'end' event may fire before the file has been written
    console.log('Read all lines: ' + count);
}).on('error', function(error) {
    console.log("ERROR: " + error.message);
});

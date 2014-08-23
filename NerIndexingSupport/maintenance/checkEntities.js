var mysql = require('mysql');
var csv = require('csv');

var connection = mysql.createConnection({
    host : 'localhost',
    user : 'ner',
    password : 'ner',
    database: 'ner'
});

csv().from.path(__dirname + '/../csv/ennodups/entities.csv', {
    delimiter : ',',
    escape : '"'
}).on('record', function(csvRow, index) {
    var pageTitle = csvRow[0].replace("http://en.wikipedia.org/wiki/", "");
    
    connection.query('SELECT * FROM page WHERE page_title LIKE ?', [ pageTitle ], function(err, rows, fields) {
        if (err) {
            throw err;
        }

        if (rows.length <= 0) {
            console.log("Page not in database ... " + pageTitle);
        } else if (rows[0].page_is_redirect) {
            console.log("Page is redirect ... " + pageTitle);
        }
    });
})
.on('close', function(count) {
    // when writing to a file, use the 'close' event
    // the 'end' event may fire before the file has been written
    console.log('Read all lines: ' + count);
}).on('error', function(error) {
    console.log("ERROR: " + error.message);
});

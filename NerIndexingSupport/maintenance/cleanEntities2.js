var mysql = require('mysql');
var csv = require('csv');
var fs = require('fs');

var pool = mysql.createPool({
    host : 'localhost',
    user : 'ner',
    password : 'ner',
    database: 'ner',
    connectionLimit: 15
});

var reader = fs.createReadStream(__dirname + '/../csv/encsv/entities.csv');
var waiting = 0;


csv()
.from.stream(reader, {
    delimiter : ',',
    escape : '"'
})
.to.stream(fs.createWriteStream(__dirname + '/../csv/encsv/entities_cleaned.csv'))
.transform( function(csvRow, index, callback){
    waiting++;
    reader.pause();

    var pageTitle = csvRow[0].replace("http://en.wikipedia.org/wiki/", "");
    
    pool.getConnection(function(err, connection) {
        if (err) {
            throw err;
        }
        connection.query('SELECT * FROM page WHERE page_title LIKE ?', [ pageTitle ], function(err, rows, fields) {
            waiting--;
            if (err) {
                throw err;
            }
            
            console.log("Waiting " + waiting);
            reader.resume();
            if (waiting <= 1) {
                console.log("Waiting " + waiting);
            }
            
            if (rows.length <= 0) {
                console.log("Page " + index + " not in database ... " + pageTitle);
                connection.release();
                callback();
            } else {
                if (rows[0].page_is_redirect) {
                    console.log("Page " + index + " is redirect ... " + pageTitle);
                }
                connection.release();
                callback(null, csvRow);
            }
        });    
    });
    
}, {parallel: 50})
.on('close', function(count) {
    // when writing to a file, use the 'close' event
    // the 'end' event may fire before the file has been written
    console.log('Read all lines: ' + count);
}).on('error', function(error) {
    console.log("ERROR: " + error.message);
});

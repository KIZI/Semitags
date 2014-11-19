var mysql = require('mysql');
var csv = require('ya-csv');
var fs = require('fs');

var pool = mysql.createPool({
    host : 'localhost',
    user : 'ner',
    password : 'ner',
    database: 'ner',
    connectionLimit: 15
});

var connection = mysql.createConnection({
    host : 'localhost',
    user : 'ner',
    password : 'ner',
    database: 'ner'
});


var reader = csv.createCsvFileReader(__dirname + '/../csv/encsv/entities.csv', {
    'separator': ',',
    'quote': '"',
    'escape': '"',
    'nestedQuotes': true       
});
var writer = csv.createCsvStreamWriter(fs.createWriteStream(__dirname + '/../csv/encsv/entities_cleaned.csv'));
var index = 0;

var underscores = /_/g;
var percents = /\%/g;

reader.addListener('data', function(csvRow) {
    index++;
    var pageTitle = unescape(csvRow[0].replace("http://en.wikipedia.org/wiki/", ""))
                    .replace(underscores, "\\_").replace(percents, "\\%");
    reader.pause();
    
    pool.getConnection(function(err, connection) {
        if (err) {
            throw err;
        }
        
        connection.query('SELECT SQL_NO_CACHE * FROM page WHERE page_title LIKE ?', [ pageTitle ], function(err, rows, fields) {
            index--;
            if (err) {
                throw err;
            }
    
            if (index <= 0) {
                console.log("Resuming reading...");
                reader.resume();
            }
            
            if (rows.length <= 0) {
                console.log("Page " + index + " not in database ... " + pageTitle);
            } else {
                if (rows[0].page_is_redirect) {
                    console.log("Page " + index + " is redirect ... " + pageTitle);
                }
//                csvRow[0] = pageTitle;
                writer.writeRecord(csvRow);
            }
            
            connection.release();
        });  
    });
});

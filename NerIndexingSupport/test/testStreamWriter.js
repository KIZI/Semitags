var mysql = require('mysql');
var csv = require('ya-csv');
var fs = require('fs');

var connection = mysql.createConnection({
    host : 'localhost',
    user : 'ner',
    password : 'ner',
    database: 'ner'
});


var reader = csv.createCsvFileReader(__dirname + '/../csv/encsv/entities.csv', {
    'separator': ',',
    'quote': '"',
    'escape': '"'       
});
var writer = csv.createCsvStreamWriter(fs.createWriteStream(__dirname + '/../csv/encsv/entities_cleaned.csv'));
var index = 0;

reader.addListener('data', function(csvRow) {
    index++;
    var pageTitle = csvRow[0].replace("http://en.wikipedia.org/wiki/", "");
    writer.writeRecord([ pageTitle ]);
});

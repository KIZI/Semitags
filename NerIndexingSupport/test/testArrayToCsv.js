var csv = require('csv');
var fs = require('fs');

var outputRows = [];
outputRows.push([ "countOfEntities", "countOfParagraphs" ]);
outputRows.push([ 1, 5 ]);

csv().from.array(outputRows)
.to.stream(fs.createWriteStream(__dirname + '/testing.csv'));
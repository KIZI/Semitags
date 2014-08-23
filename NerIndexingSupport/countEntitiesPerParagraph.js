var csv = require('csv');
var fs = require('fs');

var paragraphCounts = {};
var lastParagraph = null;
var entitiesInParagraph = [];
var paragraph = null;
var entity = null;
var prevIndex = 0; 


function countParagraph() {
    if (lastParagraph) {
        if (paragraphCounts[entitiesInParagraph.length]) {
            paragraphCounts[entitiesInParagraph.length]++;
        } else {
            paragraphCounts[entitiesInParagraph.length] = 1;
        }
    }
}

csv().from(fs.createReadStream(__dirname + '/../csv/encsv/cleanedSorted/sortByParagraphs.csv'), {
    delimiter : ',',
    escape: '"',
    relax: true
})
.on('record', function(csvRow,index){
    paragraph = csvRow[2];
    entity = csvRow[0];
    
    if (prevIndex > index) {
        console.log("Error: SPLIT ROWS");
    }
    prevIndex = index;
    
    if (((index % 100000) === 0) && (index > 0)) {
        console.log("Processed " + index + " lines. Paragraph counts length " + Object.keys(paragraphCounts).length);
    }
    
    if (lastParagraph == paragraph) {
        if (entitiesInParagraph.indexOf(entity) < 0) {
            entitiesInParagraph.push(entity);
        }
    } else {
        countParagraph(paragraph, entity);
        entitiesInParagraph = [];
        entitiesInParagraph.push(entity);
    }
    
    lastParagraph = paragraph;
})
.on('parse_error', function(row){ 
    console.log("Parsing error %j", row); 
    return row.split(',') ;
})
.on('end', function(count) {
    console.log('Read all lines: ' + count);
    countParagraph();
    
    var outputRows = [];
    outputRows.push(["countOfEntities", "countOfParagraphs"]);
    
    
    for (var i in paragraphCounts) {
        outputRows.push([i, paragraphCounts[i]]);
    }
    
    console.log("Writig results to csv");
    
    csv().from.array(outputRows)
    .to.stream(fs.createWriteStream(__dirname + '/../csv/encsv/cleanedSorted/countEntitiesPerParagraph.csv'))
    .on('close', function(count) {
        console.log("Written paragraph counts " + (count - 1));
        process.exit(0);
    });
}).on('error', function(error) {
    console.log("ERROR: " + error.message);
    process.exit(1);
});

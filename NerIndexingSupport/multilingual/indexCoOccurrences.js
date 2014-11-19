/**
 * nohup node indexCoOccurrences.js > indexCoOccurrences.out 2>&1&
 */
var csv = require('csv');
var fs = require('fs');
var redis = require("redis");
var async = require('async');

client = redis.createClient();

client.on("error", function (err) {
    console.log("Redis error " + err);
});

var coEntities = [];
var lastParagraph = null;
var surfaceForm = null;
var paragraph = null;
var entity = null;
var prevIndex = 0; 

var SEPARATOR = "#$#";


function saveCoEntities() {
    var coIndex = [];
    if (coEntities.length < 100) {
        if (lastParagraph) {
            for (var i in coEntities) {
                for(var j in coEntities) {
                    if (coEntities[i] < coEntities[j]) {
                        var id = coEntities[i] + SEPARATOR + coEntities[j];
                        if (coIndex.indexOf(id) < 0) {
                            coIndex.push(id);
                        }
                    }
                }
            }
        }
        
        for (var i in coIndex) {
    //        console.log("Indexing cooccurrence " + coIndex[i]);
            client.incr(coIndex[i]);
        }
    } else {
        console.warn("Too many cooccurring entities to index: " + coEntities.length);
    }
}

//client.select(2, function() {
//    csv().from(fs.createReadStream(__dirname + '/../csv/encsv/cleanedSorted/sortBySurfaceForms.csv'), {
client.select(4, function() {
        csv().from(fs.createReadStream(__dirname + '/../csv/encsv/cleanedSorted/sortByParagraphs.csv'), {
        delimiter : ',',
        escape: '"',
        relax: true
    })
    .on('record', function(csvRow,index){
        surfaceForm = csvRow[1];
        entity = csvRow[0].replace("http://en.wikipedia.org/wiki/", "");
        paragraph = csvRow[2];
        
        if (prevIndex > index) {
            console.log("Error: SPLIT ROWS");
        }
        prevIndex = index;
        
        if (((index % 1000) === 0) && (index > 0)) {
            console.log("Processed " + index + " lines. Current entity: " + entity + " Actual coEntities length " + coEntities.length);
        }
        
        if (lastParagraph == paragraph) {
            coEntities.push(entity);
        } else {
            saveCoEntities();
            coEntities = [];
            coEntities.push(entity);
        }
        
        lastParagraph = paragraph;
    })
    .on('parse_error', function(row){ 
        console.log("Parsing error %j", row); 
        return row.split(',') ;
    })
    .on('end', function(count) {
        console.log('Read all lines: ' + count);
        saveCoEntities();
        
        console.log("Saved last surface form... END");
    }).on('error', function(error) {
        console.log("ERROR: " + error.message);
        process.exit(1);
    });
});

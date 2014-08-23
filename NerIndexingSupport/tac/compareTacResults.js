/**
 * node compareTacResults.js ./links_and_queries/tac_2013_kbp_english_entity_linking_evaluation_KB_links.tab ./links_and_queries/results/res140517.tab
 * 
 */

var csv = require('csv');
var fs = require('fs');

var goldenStandard = process.argv[2];
var resultFile = process.argv[3];

var goldenStandardQueries = {};

var tp = 0;
var tn = 0;
var fp = 0;
var fn = 0;

var correct = 0;
var total = 0;

function evaluateResult() {
    csv().from(fs.createReadStream(resultFile), {
        delimiter : '\t',
        escape: '"',
        relax: true
    })
    .on('record', function(csvRow, index){
        var query = goldenStandardQueries[csvRow[0]];
        total++;
        
        if (query.entity == csvRow[1]) {
            console.log("Correct query " + csvRow[0] + ": " + query.entity + " = " + csvRow[1]);
            tp++;
            correct++;
        } else if ((query.entity.indexOf("NIL") === 0) && (csvRow[1].indexOf("NIL") === 0)) {
            tn++;
            correct++;
            console.log("Correct query " + csvRow[0] + ": " + query.entity + " = " + csvRow[1]);

//            if (query.entityType == csvRow[2]) {
//                console.log("Correct query " + csvRow[0] + ": " + query.entity + " = " + csvRow[1] + " ... type " + query.entityType + " = " + csvRow[2]);
//            } else {
//                console.log("Incorrect query " + csvRow[0] + ": " + query.entity + " = " + csvRow[1] + " ... type " + query.entityType + " != " + csvRow[2]);
//            }
        } else {
            if ((query.entity.indexOf("NIL") === 0) && (csvRow[1].indexOf("NIL") !== 0)) {
                console.log("Incorrect query FALSE FOUND " + csvRow[0] + ": " + query.entity + " != " + csvRow[1]);
                fp++;
            } else if ((query.entity.indexOf("NIL") !== 0) && (csvRow[1].indexOf("NIL") === 0)) {
                console.log("Incorrect query NOT FOUND " + csvRow[0] + ": " + query.entity + " != " + csvRow[1]);
                fn++;
            } else {
                console.error("Error: Undefined state");
            }
        }
        
        goldenStandardQueries[csvRow[0]] = {entity: csvRow[1], entityType: csvRow[2] };
    })
    .on('parse_error', function(row){ 
        console.log("Parsing error %j", row); 
        return row.split('\t') ;
    })
    .on('end', function(count) {
        console.log('All queries evaluated: ' + count);
        console.log("tp: " + tp + " tn: " + tn + " fp: " + fp + " fn: " + fn);
        console.log("Precision: " + (tp / (tp +fp)));
        console.log("Recall: " + (tp / (tp + fn)));
        console.log("B3 Precision: " + (correct / total));
    }).on('error', function(error) {
        console.log("ERROR: " + error.message);
        process.exit(1);
    });
}


csv().from(fs.createReadStream(goldenStandard), {
    delimiter : '\t',
    escape: '"',
    relax: true
})
.on('record', function(csvRow, index){
    goldenStandardQueries[csvRow[0]] = {entity: csvRow[1], entityType: csvRow[2] };
})
.on('parse_error', function(row){ 
    console.log("Parsing error %j", row); 
    return row.split('\t') ;
})
.on('end', function(count) {
    console.log('Read all lines: ' + count);
    evaluateResult();
}).on('error', function(error) {
    console.log("ERROR: " + error.message);
    process.exit(1);
});
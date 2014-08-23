/**
 * node build3dCandidateBenchmark.js ./3dcandidate_benchmark
 * 
 * nohup node build3dCandidateBenchmark.js ./3dcandidate_benchmark > build3dCandidateBenchmark.out 2>&1&
 */

var fs = require('fs');
var csv = require('csv');

var targetBenchmarkDir = process.argv[2];

var sfCandidates = {};

var repeatingParagraphs = 0;
var nonRepeatingParagraphs = 0;

var alreadyBenchmarked = {};

function produceSoftBenchmark(paragraphs) {
    console.log("===============================");
    console.log("Writing benchamrk files");
    console.log(Object.keys(paragraphs).length);
    console.log("===============================");
    console.log("===============================");
    csv().from(fs.createReadStream(__dirname + '/../../csv/encsv/paragraph.csv'), {
        delimiter : ',',
        escape: '"',
        relax: true
    })
    .on('record', function(csvRow,index){
        var paragraphId = csvRow[0];
        var paragraphText = csvRow[1];
        
        if (paragraphs[paragraphId] && !alreadyBenchmarked[paragraphs[paragraphId].surfaceForm + "##$##" + paragraphs[paragraphId].entity]) {
            fs.writeFileSync(targetBenchmarkDir + "/paragraph__" + paragraphId, paragraphText);
            fs.writeFileSync(targetBenchmarkDir + "/golden_standard__" + paragraphId, paragraphs[paragraphId].surfaceForm + "\n" + paragraphs[paragraphId].entity);
            alreadyBenchmarked[paragraphs[paragraphId].surfaceForm + "##$##" + paragraphs[paragraphId].entity] = 1;
        }

    })
    .on('parse_error', function(row){ 
        console.log("Parsing error %j", row); 
        return row.split(',') ;
    })
    .on('end', function(count) {
        console.log('Read all lines: ' + count);
        console.log("Repeating paragraphs " + repeatingParagraphs);
        console.log("Non repeating paragraphs " + nonRepeatingParagraphs);
    }).on('error', function(error) {
        console.log("ERROR: " + error.message);
        process.exit(1);
    });    
}

var processedParagrahs = 0;

function collectSoftParagraphs(surfaceFormMap) {
    console.log("Collecting benchmark paragraphs");
    var paragraphs = {};
    
    csv().from(fs.createReadStream(__dirname + '/../../csv/encsv/cleanedSorted/sortBySurfaceForms.csv'), {
        delimiter : ',',
        escape: '"',
        relax: true
    })
    .on('record', function(csvRow,index){
        var surfaceForm = csvRow[1];
        var entity = csvRow[0].replace("http://en.wikipedia.org/wiki/", "");
        var paragraph = csvRow[2];
        
        if (surfaceFormMap[surfaceForm] == entity) {
            if (paragraphs[paragraph]) {
                console.log("Repeating paragraph " + paragraph);
                repeatingParagraphs++;
            } else {
                nonRepeatingParagraphs++;
            }
            paragraphs[paragraph] = {};
            paragraphs[paragraph].surfaceForm = surfaceForm;
            paragraphs[paragraph].entity = entity;
        }
        
        if (((++processedParagrahs) % 50000) == 0) {
            console.log("Processed mentions: " + processedParagrahs);
        }
    })
    .on('parse_error', function(row){ 
        console.log("Parsing error %j", row); 
        return row.split(',') ;
    })
    .on('end', function(count) {
        console.log("Paragraphs collected.");
        console.log(paragraphs);
        produceSoftBenchmark(paragraphs);
    }).on('error', function(error) {
        console.log("ERROR: " + error.message);
        process.exit(1);
    });    
}

csv().from(fs.createReadStream(__dirname + '/../../csv/encsv/hardSurfaceForms3Candidates.csv'), {
//csv().from(fs.createReadStream(__dirname + '/test/hardSurfaceForm.csv'), {
    delimiter : ',',
    escape: '"',
    relax: true
})
.on('record', function(csvRow,index){
    if ((index > 0) && ((index % 135) == 0)) {
        var surfaceForm = csvRow[0];
        var candidates = csvRow[1];
        var candidatesArr = candidates.split(";");
        
        var candidatesCounts = [];
        for (var i in candidatesArr) {
            if (candidatesArr[i]) {
                var candidate = candidatesArr[i].split(" ");
                if(candidate[1]) {
                    candidatesCounts.push({candidate: candidate[0], occurrences: candidate[1].match(/\[(.*)\]/)[1]});
                }
            }
        }
        
        candidatesCounts = candidatesCounts.sort(function(candidateA, candidateB) {
            var a = parseInt(candidateA.occurrences);
            var b = parseInt(candidateB.occurrences);
            
            if (a < b) return 1;
            else if (a == b) return 0;
            else return -1;
        });        
        
        if(candidatesCounts[2]) {
            sfCandidates[surfaceForm] = candidatesCounts[2].candidate;
        }
    }
})
.on('parse_error', function(row){ 
    console.log("Parsing error %j", row); 
    return row.split(',') ;
})
.on('end', function(count) {
    
    collectSoftParagraphs(sfCandidates);
    
    
}).on('error', function(error) {
    console.log("ERROR: " + error.message);
    process.exit(1);
});
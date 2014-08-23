/**
 * node buildHardBenchmark.js ./wiki_benchmark ./soft_benchmark
 * 
 * nohup node buildSoftBenchmark.js ./soft_benchmark > buildSoftBenchmark.out 2>&1&
 */

var fs = require('fs');
var csv = require('csv');

//var outputDir = process.argv[2];
var targetBenchmarkDir = process.argv[2];
var surfaceFormsScores = [];

var alreadyBenchmarked = {};

var repeatingParagraphs = 0;
var nonRepeatingParagraphs = 0;
var writtenBenchParagraph = 0;
var alreadyBenchmarkedParagraph = 0;

function produceSoftBenchmark(paragraphs) {
    console.log("===============================");
    console.log("Writing benchamrk files");
    console.log(paragraphs);
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
//            if (paragraphId == "344210#32")
//                console.log("PARAGRAPH!! 344210#32 %j", paragraphs[paragraphId]);
            fs.writeFileSync(targetBenchmarkDir + "/paragraph__" + paragraphId + "__" + paragraphs[paragraphId].score, paragraphText);
            fs.writeFileSync(targetBenchmarkDir + "/golden_standard__" + paragraphId, paragraphs[paragraphId].surfaceForm + "\n" + paragraphs[paragraphId].entity);
            alreadyBenchmarked[paragraphs[paragraphId].surfaceForm + "##$##" + paragraphs[paragraphId].entity] = 1;
            writtenBenchParagraph++;
        } else if (paragraphs[paragraphId]) {
            alreadyBenchmarkedParagraph++;
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
        console.log("Written paragraphs " + writtenBenchParagraph);
        console.log("Already benchmarked paragraphs " + alreadyBenchmarkedParagraph);
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
        
        if ((paragraph == "344210#32")){ //|| (entity == "Great_North_Road_(Great_Britain)")) {
            console.log("PARAGRAPH 344210#32!!");
            console.log("csvRow %j", csvRow);
        }
        
        if (surfaceFormMap[surfaceForm + "###" + entity]) {
            if (paragraphs[paragraph]) {
                console.log("Repeating paragraph " + paragraph);
                repeatingParagraphs++;
            } else {
                nonRepeatingParagraphs++;
            }
            paragraphs[paragraph] = JSON.parse(JSON.stringify(surfaceFormMap[surfaceForm + "###" + entity]));
            paragraphs[paragraph].entity = entity;
            if ((paragraph == "344210#32")){ //|| (entity == "Great_North_Road_(Great_Britain)")) {
                console.log("PARAGRAPH 344210#32!! Storing...");
                console.log(paragraphs[paragraph]);
//                console.log(paragraphs);
//                console.log("===$$===");
            }
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

csv().from(fs.createReadStream(__dirname + '/../../csv/encsv/hardSurfaceForms.csv'), {
//csv().from(fs.createReadStream(__dirname + '/test/hardSurfaceForm.csv'), {
    delimiter : ',',
    escape: '"',
    relax: true
})
.on('record', function(csvRow,index){
    if (index > 0) {
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
        
//        console.log(candidatesCounts);
        
        surfaceFormsScores.push({
            surfaceForm: surfaceForm, 
            score: parseFloat(candidatesCounts[0].occurrences) / parseFloat(candidatesCounts[1].occurrences),
            candidateA: candidatesCounts[0].candidate,
            candidateB: candidatesCounts[1].candidate
        });
    }

})
.on('parse_error', function(row){ 
    console.log("Parsing error %j", row); 
    return row.split(',') ;
})
.on('end', function(count) {
    console.log('Read all lines: ' + count);
    surfaceFormsScores.sort(function(sfA, sfB) {
        if (sfA.score > sfB.score) return 1;
        else if (sfA.score == sfB.score) return 0;
        else return -1;
    });
    
    surfaceFormsScores = surfaceFormsScores.slice(0, 199);
    console.log("Prepared surface forms");
    console.log(surfaceFormsScores);
    var surfaceFormMap = {};
    for (var i in surfaceFormsScores) {
        surfaceFormMap[surfaceFormsScores[i].surfaceForm + "###" + surfaceFormsScores[i].candidateA] = surfaceFormsScores[i];
        surfaceFormMap[surfaceFormsScores[i].surfaceForm + "###" + surfaceFormsScores[i].candidateB] = surfaceFormsScores[i];
        surfaceFormMap[surfaceFormsScores[i].surfaceForm + "#A#" + surfaceFormsScores[i].candidateA] = surfaceFormsScores[i];
        surfaceFormMap[surfaceFormsScores[i].surfaceForm + "#B#" + surfaceFormsScores[i].candidateB] = surfaceFormsScores[i];
    }
    
//    console.log(surfaceFormMap);
    
    
    collectSoftParagraphs(surfaceFormMap);
    
    
}).on('error', function(error) {
    console.log("ERROR: " + error.message);
    process.exit(1);
});
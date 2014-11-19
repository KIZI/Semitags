/**
 * nohup node candidateIndexHistogram.js > candidateIndexHistogram.out 2>&1& 
 */

var csv = require('csv');
var fs = require('fs');
var redis = require("redis");

client = redis.createClient();

client.on("error", function (err) {
    console.log("Redis error " + err);
});

var lastSurfaceForm = null;
var surfaceForm = null;
var entity = null;
var prevIndex = 0; 
var percentages = {};
var readLines = 0;
var readAll = false;
var finalizeStarted = false;
    
function loadSurfaceForm(sf) {
    client.hgetall(sf, function(err, obj) {
        var maxCount = 0;
        var sum = 0;
        
        for (var i in obj) {
            var count = parseInt(obj[i]);
            if (count > maxCount) {
                maxCount = count;
            }
            
            sum += count;
        }
        
//        console.log(maxCount + " / " + sum + " ~ " + Math.round(100 * maxCount / sum) + "%");
        
        var percentage = Math.round(100 * maxCount / sum);
        if (percentages[percentage]) {
            percentages[percentage]++;
        } else {
            percentages[percentage] = 1;
        }
        readLines--;

        if (readLines < 0) {
            console.log("ERROR: processed more lines than read " + readLines);
        }
        
        if (readAll && (readLines === 0)) {
            finalize();
        }
        
    });
}

function finalize() {
    if (!finalizeStarted) {
        finalizeStarted = true;
        var outputRows = [];
        outputRows.push(["percentage", "countOfSurfaceForms"]);
        
        for (var i in percentages) {
            outputRows.push([i, percentages[i]]);
        }
        
        console.log("Writing results to csv");
        
        csv().from.array(outputRows)
        .to.stream(fs.createWriteStream(__dirname + '/../csv/encsv/normalizedCandidateIndexHistogram.csv'))
        .on('close', function(count) {
            console.log("Written percenteges " + (count - 1));
            process.exit(0);
        });
    }
}

//client.select(2, function() {
//    csv().from(fs.createReadStream(__dirname + '/../csv/encsv/cleanedSorted/sortBySurfaceForms.csv'), {
client.select(3, function() {
    csv().from(fs.createReadStream(__dirname + '/../csv/encsv/cleanedSorted/sortByNormalizedSurfaceForms.csv'), {
        delimiter : ',',
        escape: '"',
        relax: true
    })
    .on('record', function(csvRow,index){
        surfaceForm = csvRow[1];
        entity = csvRow[0].replace("http://en.wikipedia.org/wiki/", "");
        
        if (prevIndex > index) {
            console.log("Error: SPLIT ROWS");
        }
        prevIndex = index;
        
        if (((index % 100000) === 0) && (index > 0)) {
            console.log("Processed " + index + " lines. Current entity: " + entity + " Current surfaceForm: " + surfaceForm);
        }
        
        if (lastSurfaceForm != surfaceForm) {
            readLines++;
            loadSurfaceForm(surfaceForm);
        }
        
        lastSurfaceForm = surfaceForm;
    })
    .on('parse_error', function(row){ 
        console.log("Parsing error %j", row); 
        return row.split(',') ;
    })
    .on('end', function(count) {
        console.log('Read all lines: ' + count);
        readAll = true;
        console.log("Remaining lines " + readLines);
        if (readLines === 0) {
            finalize();
        }
    }).on('error', function(error) {
        console.log("ERROR: " + error.message);
        process.exit(1);
    });
});

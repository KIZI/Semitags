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
var hardSurfaceForms = [];
var readLines = 0;
var readAll = false;
var finalizeStarted = false;
    
function loadSurfaceForm(sf) {
    client.hgetall(sf, function(err, obj) {
//        console.log("=================================");
//        console.log(obj);
//        console.log("=================================");
        if (Object.keys(obj).length > 2) {
            hardSurfaceForms.push({surfaceForm: sf, candidates: obj});
            if (hardSurfaceForms.length % 1000 === 0) {
                console.log("Hard surface form " + sf + " total count " + hardSurfaceForms.length);
            }
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
        outputRows.push(["surfaceForm", "candidates"]);
        
        for (var i in hardSurfaceForms) {
            var candidates = hardSurfaceForms[i].candidates;
            var candidatesStr = "";
            
            for (var c in candidates) {
                candidatesStr += c + " [" + candidates[c] + "]" + ";";
            }
            outputRows.push([hardSurfaceForms[i].surfaceForm, candidatesStr]);
        }
        
        console.log("Writing results to csv");
        
        csv().from.array(outputRows)
        .to.stream(fs.createWriteStream(__dirname + '/../csv/encsv/hardSurfaceForms3Candidates.csv'))
        .on('close', function(count) {
            console.log("Written surface forms  " + (count - 1));
            process.exit(0);
        });
    }
}

client.select(2, function() {
    csv().from(fs.createReadStream(__dirname + '/../csv/encsv/cleanedSorted/sortBySurfaceForms.csv'), {
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

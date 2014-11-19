/**
 * nohup node buildCandidateIndex.js > buildCandidateIndex.out 2>&1&
 */
var csv = require('csv');
var fs = require('fs');
var redis = require("redis"),

client = redis.createClient();

client.on("error", function (err) {
    console.log("Redis error " + err);
});

var candidates = {};
var lastSurfaceForm = null;
var surfaceForm = null;
var entity = null;
var prevIndex = 0; 

var lang = process.argv[2];

if (!lang) {
    console.log("Usage: node 07buildCandidateIndex.js nl");
}

var redisDb = 10 // de

if (lang == "nl") {
    redisDb = 9;
}

console.log("Filling redis db " + redisDb + " for language " + lang);

function saveSurfaceForm() {
    if (lastSurfaceForm) {
        client.hmset(lastSurfaceForm, candidates);
    }
}

//client.select(2, function() {
//    csv().from(fs.createReadStream(__dirname + '/../csv/encsv/cleanedSorted/sortBySurfaceForms.csv'), {
client.select(redisDb, function() {
        csv().from(fs.createReadStream(__dirname + '/../../csv/' + lang + 'csv/sortByNormalizedSurfaceForm.csv'), {
        delimiter : ',',
        escape: '"',
        relax: true
    })
    .on('record', function(csvRow,index){
        surfaceForm = csvRow[1];
        entity = csvRow[0].replace("http://" + lang + ".wikipedia.org/wiki/", "");
        
        if (prevIndex > index) {
            console.log("Error: SPLIT ROWS");
        }
        prevIndex = index;
        
        if (((index % 100000) === 0) && (index > 0)) {
            console.log("Processed " + index + " lines. Current entity: " + entity + " Actual candidates length " + Object.keys(candidates).length);
        }
        
        if (lastSurfaceForm == surfaceForm) {
            if (candidates[entity]) {
                candidates[entity]++;
            } else {
                candidates[entity] = 1;
            }
        } else {
            saveSurfaceForm();
            candidates = {};
            candidates[entity] = 1;
        }
        
        lastSurfaceForm = surfaceForm;
    })
    .on('parse_error', function(row){ 
        console.log("Parsing error %j", row); 
        return row.split(',') ;
    })
    .on('end', function(count) {
        console.log('Read all lines: ' + count);
        saveSurfaceForm();
        
        console.log("Saved last surface form... END");
    }).on('error', function(error) {
        console.log("ERROR: " + error.message);
        process.exit(1);
    });
});

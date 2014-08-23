/**
 * soft_benchmark
 * node cleanSoftBenchmark.js ./soft_benchmark/
 * node cleanSoftBenchmark.js ./3dcandidate_benchmark/
 */

var fs = require('fs');
var csv = require('csv');
var async = require('async');
var request = require('request');

var TEXT_FILE_PREFIX = "paragraph__";
var RECOGNIZE_URL = "http://127.0.0.1:8081/recognize";

var correctSum = 0;
var incorrectSum = 0;
var totalSum = 0;
var incorrectExamples = [];

var processedFiles = 0;

var benchmarkDir = process.argv[2];



function parseGoldenStandard(goldenStandard) {
    var entity = {};
    var goldenArray = goldenStandard.split("\n");
    entity.name = goldenArray[0];
    entity.link = "http://www.wikipedia.org/wiki/" + goldenArray[1];
    
    return entity;
}

var correct = 0;
var total = 0;


fs.readdir(benchmarkDir, function (error, files) {
    async.eachSeries(files, function(fileName, callback) {
//        console.log("Processed files " + processedFiles++);
        if (fileName.indexOf(TEXT_FILE_PREFIX) == 0) {
//            console.log("Openning: " + benchmarkDir + fileName);
            var paragraph = fs.readFileSync(benchmarkDir + fileName, {encoding: "utf8"});
            var nameArray = fileName.split("__");
            var goldenStandard = fs.readFileSync(benchmarkDir + "golden_standard__" + nameArray[1], {encoding: "utf8"}); 
//            console.log("Evaluating paragraph " + fileName);
            goldenEntity = parseGoldenStandard(goldenStandard);
            
            total++;
            if (paragraph.indexOf(goldenEntity.name) < 0) {
                console.log("Error: " + fileName);
                console.log(paragraph);
                console.log(goldenEntity.name);
                console.log("-------------------------");
//                callback();
                fs.unlink(benchmarkDir + "golden_standard__" + nameArray[1], function() {
                    fs.unlink(benchmarkDir + fileName, callback);
                });
            } else {
                correct++;
                callback();
            }
        } else {
//            console.log("Skipping " + fileName);
            callback();
        }
    }, function() {
        console.log("Finished ... correct " + correct + " total " + total);
    });
});
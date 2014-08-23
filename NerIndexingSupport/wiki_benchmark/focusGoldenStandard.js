/**
 * 
 * node focusGoldenStandard ./soft_res/golden_standard.tab ./soft_res/spotlight.tab ./soft_res/golden_spotlight.tab
 */

var fs = require('fs');
var csv = require('csv');
var async = require('async');
var request = require('request');

var goldenStandardPath = process.argv[2];
var focusFilePath = process.argv[3];
var newGoldenStandardPath = process.argv[4];



function parseGoldenStandard(goldenStandard) {
    var entity = {};
    var goldenArray = goldenStandard.split("\n");
    entity.name = goldenArray[0];
    entity.link = "http://www.wikipedia.org/wiki/" + goldenArray[1];
    
    return entity;
}

var correct = 0;
var total = 0;

var focusFile = fs.readFileSync(focusFilePath, {encoding: "utf8"});

var focusLines = focusFile.split("\n");

var keepRecords = {};

for (var i in focusLines) {
    var focusLine = focusLines[i].split("\t");
    if (focusLine[1]) {
        if (focusLine[1].indexOf("NIL") < 0) {
           keepRecords[focusLine[0]] = true; 
        }
    }
}

var goldenStandard = fs.readFileSync(goldenStandardPath, {encoding: "utf8"});
var gsLines = goldenStandard.split("\n");

for (var i in gsLines) {
    if (keepRecords[gsLines[i].split("\t")[0]]) {
        console.log("Keeping " + gsLines[i].split("\t")[0]);
        fs.appendFileSync(newGoldenStandardPath, gsLines[i] + "\n");
    }
}

console.log("Finished");
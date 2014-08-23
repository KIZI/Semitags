/**
 * soft_benchmark
 * nohup node evaluateSoftBenchmarkSpotlight.js ./soft_benchmark/ ./soft_benchmark.spotlight.result > evaluateSoftBenchmarkSpotlight.out 2>&1&
 * 
 * 
 * 3d_candidate
 * nohup node evaluateSoftBenchmarkSpotlight.js ./3dcandidate_benchmark/ ./3dcandidate_benchmark.spotlight.result > evaluate3dCanodidateBenchmarkSpotlight.out 2>&1&
 */

var fs = require('fs');
var csv = require('csv');
var async = require('async');
var request = require('request');


var TEXT_FILE_PREFIX = "paragraph__";
var SPOTLIGHT_URL = " http://spotlight.dbpedia.org/rest/annotate/";

var correctSum = 0;
var incorrectSum = 0;
var totalSum = 0;
var incorrectExamples = [];

var processedFiles = 0;

var benchmarkDir = process.argv[2];
var resultFile = process.argv[3];
var preprocess = process.argv[4];

function countStats(err) {
    if (err) {
        console.error(err);
    } else {
        console.log("Writing results...");
        var result = "correct: " + correctSum + "\nincorrect: " + incorrectSum + "\n total: " + totalSum + "\n";
        result += "precision: " + (correctSum / (correctSum + incorrectSum)) + "\n recall: " + (correctSum / totalSum) + "\n";
        for (var i in incorrectExamples) {
            result += incorrectExamples[i] + "\n";
        }
        fs.writeFileSync(resultFile, result);
        console.log("Finished.");
    }
}

function callRecService(paragraph, surfaceForm, callback) {
    console.log("Calling rec service");
    
    request.post(
            SPOTLIGHT_URL, 
            {headers: {"Accept": "application/json"}, form:{text: paragraph}}, 
            function (err, res, recEntities) {
                var recEntity = null;
                if (err) {
                    console.error("Error calling recognition service (probably network error) " + err + " %j", res);
                    callback();
                    return;
                }
                
                if (res.statusCode != 200) {
                    console.error("Error calling best match status code: " + res.statusCode + " %j", recEntities);
                    console.log(paragraph);
                    callback();
                    return;
                } else {
//                                console.log("RecEntities %j", recEntities);
                    var finalEntities  =[];
                    recEntities = JSON.parse(recEntities);
                    
//                    console.log("Rec entities: %j", recEntities);
                    
                    recEntities = recEntities["Resources"];
                    
                    for (var i in recEntities) {
                        if (recEntities[i]["@surfaceForm"]  == surfaceForm) {
                            console.log("Recognized entity " + recEntities[i]["@surfaceForm"] + " link: " + recEntities[i]["@URI"]);
                            recEntity = {name: recEntities[i]["@surfaceForm"], link: recEntities[i]["@URI"]};
                            finalEntities.push(recEntity);
                        }
                    }
                    
                }
                
                callback(finalEntities);
            }
    );
}

function parseGoldenStandard(goldenStandard) {
    var entity = {};
    var goldenArray = goldenStandard.split("\n");
    entity.name = goldenArray[0];
    entity.link = "http://www.wikipedia.org/wiki/" + goldenArray[1];
    
    return entity;
}

function isCorrect(paragraph, goldenStandard, callback) {
    var goldenEntity = parseGoldenStandard(goldenStandard);
    callRecService(paragraph, goldenEntity.name, function(entities) {
        if (!entities) {
            callback();
            return;
        }
        
        console.log(goldenEntity);
        for (var i in entities) {
            console.log("Found " + entities[i].name + " ... " + goldenEntity.name + " ~ " + (entities[i].name == goldenEntity.name));
            entities[i].link = entities[i]["link"].replace("http://dbpedia.org/resource/", "http://www.wikipedia.org/wiki/");
            if (entities[i].name == goldenEntity.name) {
                callback(entities[i].link == goldenEntity.link, entities[i].link);
                return;
            }
        }
        
        callback();
    });

}

fs.readdir(benchmarkDir, function (error, files) {
    async.eachSeries(files, function(fileName, callback) {
        console.log("Processed files " + processedFiles++);
        if (fileName.indexOf(TEXT_FILE_PREFIX) == 0) {
            var paragraph = fs.readFileSync(benchmarkDir + fileName, {encoding: "utf8"});
            var nameArray = fileName.split("__");
            var goldenStandard = fs.readFileSync(benchmarkDir + "golden_standard__" + nameArray[1], {encoding: "utf8"}); 
            console.log("Evaluating paragraph " + fileName);
            isCorrect(paragraph, goldenStandard, function(correct, disambiguatedLink) {
                totalSum++;
                console.log("Correct %j", correct);
                if ((correct !== undefined) && (correct !== null)) {
                    if (correct) {
                        console.log("Correct");
                        correctSum++;
                    } else {
                        console.log("Incorrect");
                        incorrectSum++;
                        incorrectExamples.push(fileName + " ... " + goldenStandard + "\nDisambiguatedLink: " + disambiguatedLink);
                    }
                } else {
                    console.log("Not found");
                }
                callback();
            });
        } else {
            console.log("Skipping " + fileName);
            callback();
        }
    }, countStats);
});
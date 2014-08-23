/**
 * Usage node readQueries.js queriesFile
 * 
 * Normalized
 * nohup node readQueries.js /Users/ivo/workspace/NerIndexingSupport/tac/links_and_queries/tac_2013_kbp_english_entity_linking_evaluation_queries.xml /Users/ivo/workspace/NerIndexingSupport/tac/links_and_queries/results/res140620.norm.tab norm > readQueriesNorm.out 2>&1&
 * 
 * Sum
 * nohup node readQueries.js /Users/ivo/workspace/NerIndexingSupport/tac/links_and_queries/tac_2013_kbp_english_entity_linking_evaluation_queries.xml /Users/ivo/workspace/NerIndexingSupport/tac/links_and_queries/results/res140620.sum.tab sum > readQueriesSum.out 2>&1&
 * nohup node readQueries.js /lmnas/nlp/lasi00/indexing/ner/tac/links_and_queries/tac_2013_kbp_english_entity_linking_evaluation_queries.xml /lmnas/nlp/lasi00/indexing/ner/tac/links_and_queries/results/res140702.sum.tab sum > readQueriesSum.out 2>&1&
 * 
 * Non-normalized
 * nohup node readQueries.js /Users/ivo/workspace/NerIndexingSupport/tac/links_and_queries/tac_2013_kbp_english_entity_linking_evaluation_queries.xml /Users/ivo/workspace/NerIndexingSupport/tac/links_and_queries/results/res140620.default.tab default > readQueriesDefault.out 2>&1&
 * 
 * --------------------------------------
 * 
 * Default
 * nohup node readQueries.js /lmnas/nlp/lasi00/indexing/ner/tac/links_and_queries/tac_2013_kbp_english_entity_linking_evaluation_queries.xml /lmnas/nlp/lasi00/indexing/ner/tac/links_and_queries/results/res140702.default.tab default > readQueriesDefault.out 2>&1&
 * nohup node readQueries.js /lmnas/nlp/lasi00/indexing/ner/tac/links_and_queries/tac_2013_kbp_english_entity_linking_evaluation_queries.xml /lmnas/nlp/lasi00/indexing/ner/tac/links_and_queries/results/res140702.norm.tab norm > readQueriesNorm.out 2>&1&
 * nohup node readQueries.js /lmnas/nlp/lasi00/indexing/ner/tac/links_and_queries/tac_2013_kbp_english_entity_linking_evaluation_queries.xml /lmnas/nlp/lasi00/indexing/ner/tac/links_and_queries/results/res140702.sum.tab sum > readQueriesSum.out 2>&1&
 * nohup node readQueries.js /lmnas/nlp/lasi00/indexing/ner/tac/links_and_queries/tac_2013_kbp_english_entity_linking_evaluation_queries.xml /lmnas/nlp/lasi00/indexing/ner/tac/links_and_queries/results/res140723.max.tab max > readQueriesMax.out 2>&1&
 * 
 */

//var BEST_MATCH_URL = "http://nlp.vse.cz:8081/best-match";
//var RECOGNIZE_URL = "http://nlp.vse.cz:8081/recognize";
var BEST_MATCH_URL = "http://127.0.0.1:8081/best-match";
var RECOGNIZE_URL = "http://127.0.0.1:8081/recognize";
//var CORPUS_DIR = "/Volumes/WD-DATA/NER_experiments/TAC2014/corpus_extracted/";
//var CORPUS_DIR = "/Users/ivo/workspace/NerIndexingSupport/tac/corpus_extracted/";
var CORPUS_DIR = "/lmnas/nlp/lasi00/indexing/ner/tac/corpus_extracted/";

var TYPES_MAPPING = {
        "PERSON": "PER",
        "LOCATION": "LOC",
        "ORGANIZATION": "ORG",
        "MISC": "GPE"
};

var fs = require('fs');
var xml2js = require('xml2js');
var request = require('request');
var async = require('async');
var redis = require("redis");

//var client = request.newClient('http://nlp.vse.cz:8081/');
var redisClient = redis.createClient();
var parser = new xml2js.Parser();

var results = "";
var nils = 1;

var preprocess = process.argv[4];


redisClient.select(5, function() {
    fs.readFile(process.argv[2], function(err, data) {
        parser.parseString(data, function (err, result) {
            var queries = result.kbpentlink.query;
            
            var queriesCount = queries.length;
            var queryNr = 0;
            var processedQueries = 0;
            var lastTime = new Date();
            
            async.eachSeries(queries, function(query, callback) {
                queryNr++;
                var docid = query.docid[0];
                fs.readFile(CORPUS_DIR + docid, {encoding: 'utf-8'}, function(err, fileContent) {
                    if (err) { callback(err); return; }
                    console.log("Processing query " + queryNr + " of " + queriesCount + " entitiy: " + query.name[0]);
                    console.log("Reading file " + docid);
                
                    request.post(
                            RECOGNIZE_URL, 
                            {form:{preprocess: preprocess, text: fileContent, queryEntity: query.name[0]}}, 
                            function (err, res, recEntities) {
                                if (err) {
                                    console.error("Error calling recognition service (probably network error) " + err + " %j", res);
                                    callback();
                                    return;
                                }
                                
                                if (res.statusCode != 200) {
                                    console.error("Error calling best match status code: " + res.statusCode + " %j", recEntities);
                                    callback();
                                    return;
                                }
                                
                                var recEntity = null;
                                console.log("Query name " + query.name[0]);
//                                console.log("RecEntities %j", recEntities);
                                recEntities = JSON.parse(recEntities);
                                for (var i in recEntities) {
//                                    console.log(recEntities[i].name);
                                    if (recEntities[i].name == query.name[0]) {
                                        console.log("Recognized entity " + recEntities[i].name + " link: " + recEntities[i].link);
                                        recEntity = recEntities[i];
                                    }
                                }
                                
                                if (recEntity == null) {
                                    recEntity = { link: null, type: "MISC" };
                                }
                                var link = (recEntity.link == null) ? null : recEntity.link.replace("http://www.wikipedia.org/wiki/", "");
                                redisClient.get(link, function(error, tacId) {
                                    if (error) {
                                        callback(error);
                                    }
                                    
                                    if (tacId === null) {
                                        tacId ="NIL";
                                        for (var i = nils.toString().length; i < 4; i++) {
                                            tacId += "0";
                                        }
                                        tacId += nils++;
                                    }
                                    console.log(recEntity.type);
                                    var mappedType = (TYPES_MAPPING[recEntity.type]) ? TYPES_MAPPING[recEntity.type] : "GPE";
                                    results += query.$.id + "\t" + tacId + "\t" + mappedType + "\tN\tN\tN\n";
    //                                console.log(query.$.id);
    //                                console.log(query.name[0]);
    //                                console.log(body.link);
    //                                console.log(tacId);
                                    console.log(query.$.id + "\t" + tacId + "\t" + mappedType + "\tN\tN\tN\n");
                                    console.log("Time per query " + ((new Date()).getTime() - lastTime.getTime()) / 1000);
                                    lastTime = new Date();
                                    console.log("Processed queries " + (++processedQueries));
                                    callback();
                                });
                            }
                    );
                });
            }, function(err) {
                if (err) {
                    console.error("Error: " + err);
                } else {
                    console.log("All queries processed");
                    fs.writeFile(process.argv[3], results, function (err) {
                        if (err) console.error("Error writing file " + err);
                        console.log('Results Saved!');
                    });
                }
            });
        });
    });
});
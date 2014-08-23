/**
 * node checkQueries.js /Volumes/WD-DATA/NER_experiments/TAC2014/corpus_extracted/ /Volumes/WD-DATA/NER_experiments/TAC2014/links_and_queries/tac_2013_kbp_english_entity_linking_evaluation_queries.xml
 */

var fs = require('fs');
var async = require('async');
var xml2js = require('xml2js');

var parser = new xml2js.Parser();

var targetDir = process.argv[2];
var queriesFile = process.argv[3];

fs.readFile(queriesFile, function(err, data) {
    parser.parseString(data, function (err, result) {
        var queries = result.kbpentlink.query;
        var docIds = [];
        console.log("Indexing docIds");
        for (var i in queries) {
            docIds.push(queries[i].docid[0]);
        }
        
        async.eachSeries(docIds, function(docId, callback) {
            try {
                fs.readFileSync(targetDir + docId);
            } catch (ex) {
                console.log(docId + " does not exist");
            }
            
            callback();
        });
    });
});
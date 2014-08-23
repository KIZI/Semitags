/**
 * node --max-old-space-size=8192 --nouse-idle-notification extractDocs.js /Volumes/WD-DATA/NER_experiments/TAC2014/source_corpus/ /Volumes/WD-DATA/NER_experiments/TAC2014/corpus_extracted/ /Volumes/WD-DATA/NER_experiments/TAC2014/links_and_queries/tac_2013_kbp_english_entity_linking_evaluation_queries.xml
 */

var fs = require('fs');
var async = require('async');
var xml2js = require('xml2js');

var parser = new xml2js.Parser();

var docsDir = process.argv[2];
var targetDir = process.argv[3];
var queriesFile = process.argv[4];

function extractStrings(object, str) {
    if (!object) {
        return '';
    } else if (typeof object === 'string') {
        return object;
    } else {
        for (var i in object) {
            str += extractStrings(object[i], str);
        }
    }
    
    return str;
}

fs.readFile(queriesFile, function(err, data) {
    parser.parseString(data, function (err, result) {
        var queries = result.kbpentlink.query;
        var docIds = [];
        console.log("Indexing docIds");
        for (var i in queries) {
            docIds.push(queries[i].docid[0]);
        }
        var createdFiles = 0;

        console.log("docIds indexied " + docIds.length);
        fs.readdir(docsDir, function(err, files) {
            console.log("Processing files in " + docsDir + " " + files.length);
            async.eachSeries(files, function(file, callback) {
                if (file.indexOf("bolt-eng") >= 0) {
                    console.log("Reading file " + file);
                    fs.readFile(docsDir + file, function(err, data) {
                        if (err) {
                            console.error(err);
                            callback(err);
                            return;
                        }
                        console.log("Created files " + createdFiles);
                        console.log("Processing file " + file);
//                        var matches = data.toString().match(/<DOC id[\s\S]*?DOC>/g);
                        var splitString = "<doc id";
                        var matches = data.toString().split(splitString);
                        if ((matches == null) || (matches.length <= 1)) {
                            splitString = "<DOC id";
                            matches = data.toString().split(splitString);
                        }
                        console.log("Matched docs " + matches.length);

                        async.each(matches, function(match, matchCallback) {
                            match = splitString + match;
                            parser.parseString(match, function (err, result) {
                                if (!result || !result.DOC) {
                                    matchCallback();
                                    return;
                                }
                                if (docIds.indexOf(result.DOC.$.id) >= 0) {
                                    var str = '';
                                    fs.writeFile(targetDir + result.DOC.$.id, extractStrings(result.DOC.TEXT, str), function(err) {
                                        console.log("Created file " + result.DOC.$.id);
                                        createdFiles++;
                                        matchCallback(err);
                                    });
                                } else {
                                    matchCallback(err);
                                }
                            });
                        }, callback);
                    });
                } else {
                    console.log("Skipping " + file + "...");
                    callback();
                }
            });
        });
    });
});
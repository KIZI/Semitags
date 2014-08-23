/**
 * node extractDocsEngNG.js /Volumes/WD-DATA/NER_experiments/TAC2014/source_corpus/ /Volumes/WD-DATA/NER_experiments/TAC2014/corpus_extracted/ /Volumes/WD-DATA/NER_experiments/TAC2014/links_and_queries/tac_2013_kbp_english_entity_linking_evaluation_queries.xml
 */

var fs = require('fs');
var async = require('async');
var xml2js = require('xml2js');
var split = require('split');
var html_strip = require('htmlstrip-native');

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
                if (file.indexOf("eng-WL") >= 0) {
                    console.log("Reading file " + file);
                    console.log("Created files " + createdFiles);
                    var splitString = "<DOC>";
                    
                    fs.createReadStream(docsDir + file)
                    .pipe(split(splitString))
                    .on('data', function (match) {
                        match = splitString + match;
                        var idMatch = match.match(/<DOCID>(.*)<\/DOCID>/);
                        if (idMatch) {
                            var id = idMatch[1].trim();
                            match = html_strip.html_strip(match);
                            if (docIds.indexOf(id) >= 0) {
                                fs.writeFile(targetDir + id, match, function(err) {
                                    console.log("Created file " + id);
                                    createdFiles++;
                                });
                            }
                        }
                    })
                    .on('end', function() {
                        callback();
                    });
                } else {
                    console.log("Skipping " + file + "...");
                    callback();
                }
            });
        });
    });
});
/**
 * nohu node --stack-size=65500 rest.js &
 */

var restify = require("restify");
var request = require("request");
var xml2js = require("xml2js");
var redis = require("redis");
var async = require("async");
var normalizer = require(__dirname + "/utils/normalizer.js");

//--------------------------

var SERVER_PORT    =  "8081";
var SEMITAGS_NER_URL = "http://localhost:8080/SemiTags/rest/v1/ner";
var REDIS_PORT = 6379;
var REDIS_HOST = "localhost";
var REDIS_DATABASE_ORIGINAL = 2;
var REDIS_DATABASE_NORMALIZED = {
        en: 3,
        nl: 9,
        de: 10
};
var REDIS_DATABASE_COOC = {
        en: 6,
        nl: 11,
        de: 12
}
var SEPARATOR  = "#$#";

var TYPE_MAP = {
        "LOCATION" : "location",
        "I-LOC" : "location",
        "B-LOC" : "location",
        "MISCELLANEUS" : "miscellaneous",
        "I-MISC" : "miscellaneous",
        "B-MISC" : "miscellaneous",
        "ORGANIZATION" : "organization",
        "I-ORG" : "organization",
        "B-ORG" : "organization",
        "PERSON" : "person",
        "I-PER" : "person",
        "B-PER" : "person"
}
//var SERVER_IP_ADDR = "127.0.0.1";
//var SERVER_PORT    =  "8181";
//var SEMITAGS_NER_URL = "http://localhost:9090/SemiTags/rest/v1/ner";
//var REDIS_PORT = 6379;
//var REDIS_HOST = "nlp.vse.cz";
//var REDIS_DATABASE = 2;

// --------------------------

function getBestCandidate(candidates) {
    var bestCount = 0;
    var bestCandidate = null;
    var sumCount = 0;
    
//    console.log("Candidates %j", candidates);
    for (var i in candidates) {
        var count = parseInt(candidates[i]);
        sumCount += count;
        if (count > bestCount) {
            bestCount = count;
            bestCandidate = i;
        }
    }
    
    return { "bestCandidate": bestCandidate, score: (bestCount / sumCount) };
}

function entitiesIterator(entity, callback) {
    console.log("Processing entity %j", entity);
    redisClient.select(REDIS_DATABASE_ORIGINAL, function() {
        redisClient.hgetall(entity.name[0], function(err, candidates) {
            processCandidates(err, candidates, entity, entity.name[0], callback);
        });
    });
}

function entitiesIteratorNormalized(entity, lang, callback) {
    console.log("Processing entity %j", entity);
    redisClient.select(REDIS_DATABASE_NORMALIZED[lang], function() {
        redisClient.hgetall(normalizer.normalize(entity.name[0]), function(err, candidates) {
            processCandidates(err, candidates, entity, entity.name[0], callback);
        });
    });
}

function processCandidates(err, candidates, entity, originalEntityName, callback) {
    console.log("Getting best candidate for " + entity.name);
    var bestCandidateRes =  getBestCandidate(candidates);
    var bestCandidate = null;
    var score = null;
    if (bestCandidateRes.bestCandidate) {
        bestCandidate = bestCandidateRes.bestCandidate;
        score = bestCandidateRes.score;
    }
    
    
    callback(err, 
            { 
                name: originalEntityName, 
                start: entity.start[0],
                type: entity.type[0],
                link: bestCandidate,
                score: score
            }
    );
}

// Sum
function coOccurrenceScore(entities, lang, coocCallback) {
    var entCandidates = {};
    
    async.eachSeries(entities, function(entity, callback) {
        redisClient.select(REDIS_DATABASE_NORMALIZED[lang], function() {
            redisClient.hgetall(normalizer.normalize(entity.name[0]), function(err, candidates) {
                if (!candidates) {
                    candidates = {};
                }
                entCandidates[entity.name[0]] = {};
                entCandidates[entity.name[0]].candidates = candidates;
                entCandidates[entity.name[0]].entity = entity;
                callback();
            });
        });    
        
    }, function(err) {
        console.log("All candidates for all recognized entities: %j", entCandidates);
        for (var i in entCandidates) {
            for (var j in entCandidates[i].candidates) {
                entCandidates[i].candidates[j] = {
                    sfScore: parseInt(entCandidates[i].candidates[j]),
                    coocScore: 0
                };
            }
        }
        var coocMap = {};
        
        redisClientCoOc.select(REDIS_DATABASE_COOC[lang], function() {
            async.eachSeries(Object.keys(entCandidates), function(i, callback1) {
                console.log("Scoring surface form cooccurrences " + i);
                async.eachSeries(Object.keys(entCandidates[i].candidates), function(candidate1, callback3) {
                    if (entCandidates[i].candidates[candidate1].sfScore > 3) {
                        redisClientCoOc.hgetall(candidate1, function(err, coocCand) {
                            if (coocCand) {
                                for (var iCooc in coocCand) {
                                    if (coocMap[iCooc]) {
                                        coocMap[iCooc].cnt += parseInt(coocCand[iCooc]);
                                        coocMap[iCooc].sfs[i] = true;
                                    } else {
                                        coocMap[iCooc] = {
                                            cnt: parseInt(coocCand[iCooc]),
                                            sfs:{}
                                        };
                                        coocMap[iCooc].sfs[i] = true;
                                    }
                                }
                            }
                            callback3();
                        });
                    } else {
                        callback3();
                    }
                }, callback1);
            }, function(err) {
                
                for (var i in entCandidates) {
                    console.log("Scoring surface form " + i);
                    for (var candidate1 in entCandidates[i].candidates) {
                        if (coocMap[candidate1] && (!coocMap[candidate1].sfs[i] || (Object.keys(coocMap[candidate1].sfs).length > 1))) {
                            entCandidates[i].candidates[candidate1].coocScore += coocMap[candidate1].cnt;
                        }
                    }
                }
                
                coocCallback(err, entCandidates);
            });
        });
    });
}

function coOccurrenceMaxScore(entities, lang, coocCallback) {
    var entCandidates = {};
    
    async.eachSeries(entities, function(entity, callback) {
        redisClient.select(REDIS_DATABASE_NORMALIZED[lang], function() {
            redisClient.hgetall(normalizer.normalize(entity.name[0]), function(err, candidates) {
                if (!candidates) {
                    candidates = {};
                }
                entCandidates[entity.name[0]] = {};
                entCandidates[entity.name[0]].candidates = candidates;
                entCandidates[entity.name[0]].entity = entity;
                callback();
            });
        });    
        
    }, function(err) {
//        console.log("All candidates for all recognized entities: %j", entCandidates);
        for (var i in entCandidates) {
            for (var j in entCandidates[i].candidates) {
                entCandidates[i].candidates[j] = {
                        sfScore: parseInt(entCandidates[i].candidates[j]),
                        coocScore: 0
                };
            }
        }

        var coocMap = {};
        
        redisClientCoOc.select(REDIS_DATABASE_COOC[lang], function() {
            async.eachSeries(Object.keys(entCandidates), function(i, callback1) {
                console.log("Scoring surface form cooccurrences " + i);
                async.eachSeries(Object.keys(entCandidates[i].candidates), function(candidate1, callback3) {
                    if (entCandidates[i].candidates[candidate1].sfScore > 3) {
                        redisClientCoOc.hgetall(candidate1, function(err, coocCand) {
                            if (coocCand) {
                                for (var iCooc in coocCand) {
                                    if (!coocMap[iCooc]) coocMap[iCooc] = {};
                                    if (coocMap[iCooc][i]) {
                                        if (coocMap[iCooc][i].cnt < parseInt(coocCand[iCooc])) {
                                            coocMap[iCooc][i].cnt = parseInt(coocCand[iCooc]);
                                            coocMap[iCooc][i].cooc = candidate1;
                                        }
                                    } else {
                                        coocMap[iCooc][i] = {
                                            cnt: parseInt(coocCand[iCooc]),
                                            cooc: candidate1
                                        };
                                    }
                                }
                            }
                            callback3();
                        });
                    } else {
                        callback3();
                    }
                }, callback1);
            }, function(err) {
                
                for (var i in entCandidates) {
                    console.log("Scoring surface form " + i);
                    for (var candidate1 in entCandidates[i].candidates) {
                        if (coocMap[candidate1]) {
                            for (var j in coocMap[candidate1]) {
                                if (i != j) {
                                    if (entCandidates[i].candidates[candidate1].coocScore < coocMap[candidate1][j].cnt) {
//                                console.log("Assigning to candidate " + i + " ... " + candidate1 + "= " + coocMap[i][candidate1].cnt);
                                        entCandidates[i].candidates[candidate1].coocScore = coocMap[candidate1][j].cnt;
                                    }
                                    if (entCandidates[j].candidates[coocMap[candidate1][j].cooc].coocScore < coocMap[candidate1][j].cnt) {
//                                console.log("Assigning to candidate " + coocMap[i][candidate1].sf + " ... " + coocMap[i][candidate1].cooc + "= " + coocMap[i][candidate1].cnt);
                                        entCandidates[j].candidates[coocMap[candidate1][j].cooc].coocScore = coocMap[candidate1][j].cnt;
                                    }
                                }
                            }
                        }
                    }
                }
                
                coocCallback(err, entCandidates);
            });
        });        
    });
}


function selectBestCandidateBy(metric, scoredCandidates) {
    var maxScore = 0;
    var bestCandidate = null;
    var totalScore = 0;
    
    for (candidate in scoredCandidates) {
        if (scoredCandidates[candidate][metric] > maxScore) {
            maxScore = scoredCandidates[candidate][metric];
            bestCandidate = candidate;
        }
        totalScore += scoredCandidates[candidate][metric];
    }    

    console.log("Metric: " + metric + " ... " + bestCandidate + " max score: " + maxScore + " total score: " + totalScore);
    return { candidate: bestCandidate, score: (maxScore / totalScore), metric: metric };
}


function disambiguate(entities, preprocess, lang, callback) {
    var resEntities = [];
    if (preprocess == "sum") {
        coOccurrenceScore(entities, lang, function(err, scoredCandidates) {
            for (var sf in scoredCandidates) {
                var bestCandidate = selectBestCandidateBy("coocScore", scoredCandidates[sf].candidates);
                if (!bestCandidate.candidate) {
                    bestCandidate = selectBestCandidateBy("sfScore", scoredCandidates[sf].candidates);
                }
                
                resEntities.push({ 
                    name: sf, 
                    start: scoredCandidates[sf].entity.start[0],
                    type: scoredCandidates[sf].entity.type[0],
                    link: bestCandidate.candidate,
                    score: bestCandidate.score,
                    metric: bestCandidate.metric
                });
            }
            
//           console.log("scoredCandidates %j", scoredCandidates);
           callback(null, resEntities);
        });
    } else if (preprocess == "max") {
        coOccurrenceMaxScore(entities, lang, function(err, scoredCandidates) {
            for (var sf in scoredCandidates) {
                var bestCandidate = selectBestCandidateBy("coocScore", scoredCandidates[sf].candidates);
                if (!bestCandidate.candidate) {
                    bestCandidate = selectBestCandidateBy("sfScore", scoredCandidates[sf].candidates);
                }
                
                resEntities.push({ 
                    name: sf, 
                    start: scoredCandidates[sf].entity.start[0],
                    type: scoredCandidates[sf].entity.type[0],
                    link: bestCandidate.candidate,
                    score: bestCandidate.score,
                    metric: bestCandidate.metric
                });
            }
            
//           console.log("scoredCandidates %j", scoredCandidates);
           callback(null, resEntities);
        });        
    } else {
        if (preprocess == "norm") {
            
            async.concat(entities, function(entity, callback) {
                entitiesIteratorNormalized(entity, lang, callback);
            }, callback);
        } else {
            async.concat(entities, entitiesIterator, callback);
        }
    }
    
}


function recognize(req, res, next) {
    req.connection.setTimeout(86400 * 1000);
    res.connection.setTimeout(86400 * 1000); 
    console.log("Calling SemiTags with text: " + req.params.text);
    
    request.post(SEMITAGS_NER_URL, {form:{language: req.params.lang, text: req.params.text}}, function callback (err, httpResponse, body) {
        if (err) {
            console.error("SemiTags call failed:", err);
            res.send(500 , "SemiTags call failed");
            return next();
        }
    
        console.log("SemiTags returned " + body);
        xml2js.parseString(body,  function (err, semiTagsEntities) {
            if (err) {
                console.error("Failed parsing SemiTags response:", err);
                res.send(500 , "Failed parsing SemiTags response");
                return next();
            }
            
            var entities = semiTagsEntities.stanfordEntityRestables.stanfordEntityRestable;
            
            if (entities) {
                var containsQueryEntity = false;
                for (var i in entities) {
                    entities[i].type = [TYPE_MAP[entities[i].type]];
                    
                    if (entities[i].name[0] == req.params.queryEntity) {
                        containsQueryEntity = true;
                    }
                }
                if (!containsQueryEntity && req.params.queryEntity) {
                    entities.push({name: [req.params.queryEntity], type: [null], start:[null]});
                }
                
                disambiguate(entities, req.params.preprocess, req.params.lang, function(err, disambEntities){
                    res.send(200 , disambEntities);
                    return next();  
                });
            } else {
                res.send(200 , []);
                return next(); 
            }
        });
    });
}

function bestMatch(req, res, next) {
    console.log("Linking entity " + req.params.surfaceForm);
    if (REDIS_DATABASE_NORMALIZED[req.params.lang]) {
        redisClient.select(REDIS_DATABASE_NORMALIZED[req.params.lang], function() {
            redisClient.hgetall(normalizer.normalize(req.params.surfaceForm), function(err, candidates) {
                console.log("Getting best candidate for " + req.params.surfaceForm);
                var bestCandidateRes =  getBestCandidate(candidates);
                var bestCandidate = null;
                var score = null;
                if (bestCandidateRes.bestCandidate) {
                    bestCandidate = "http://" + req.params.lang + ".wikipedia.org/wiki/" + bestCandidateRes.bestCandidate;
                    score = bestCandidateRes.score;
                }
                
                res.send(200, { 
                    name: req.surfaceForm, 
                    link: bestCandidate,
                    socre: score
                });
                
                return next(); 
            });
        });
    } else {
        res.send(404, { 
            message: "Language not supported. Supported lang params en, de, nl."
        });
        
        return next(); 
    }
}

function timeout(req, res, next) {
    req.connection.setTimeout(86400 * 1000);
    res.connection.setTimeout(86400 * 1000); 
    setTimeout(function() {
        res.send(200, { 
            name: "success"
        });
        return next();
    }, 130 * 1000);
}

// --------------------------

var redisClient = redis.createClient(REDIS_PORT, REDIS_HOST);
var redisClientCoOc = redis.createClient(REDIS_PORT, REDIS_HOST);

redisClient.on("error", function (err) {
    console.error("Redis error " + err);
});


var server = restify.createServer({
    name : "NerService"
});
 
server.use(restify.queryParser());
server.use(restify.bodyParser());
server.use(restify.CORS());


server.post({path : "/recognize" , version: "0.0.1"}, recognize);
server.post({path : "/best-match" , version: "0.0.1"}, bestMatch);
server.post({path : "/timeout" , version: "0.0.1"}, timeout);

console.log("Connecting to Redis...");
console.log("Redis connected");
server.listen(SERVER_PORT, function(){
    console.log("%s listening at %s ", server.name , server.url);
});

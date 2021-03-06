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
var REDIS_DATABASE_NORMALIZED = 3;
var REDIS_DATABASE_COOC = 6;
var SEPARATOR  = "#$#";

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
    
    console.log("Candidates %j", candidates);
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

function entitiesIteratorNormalized(entity, callback) {
    console.log("Processing entity %j", entity);
    redisClient.select(REDIS_DATABASE_NORMALIZED, function() {
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
        bestCandidate = "http://www.wikipedia.org/wiki/" + bestCandidateRes.bestCandidate;
        score = bestCandidateRes.score;
    }
    
    callback(err, 
            { 
                name: originalEntityName, 
                start: entity.start[0],
                type: entity.type[0],
                link: bestCandidate,
                socre: score
            }
    );
}

// Sum
function coOccurrenceScore(entities, coocCallback) {
    var entCandidates = {};
    
    async.eachSeries(entities, function(entity, callback) {
        redisClient.select(REDIS_DATABASE_NORMALIZED, function() {
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
                    coocScore: 0,
                    cooc: {}
                };
            }
        }
        
        redisClientCoOc.select(REDIS_DATABASE_COOC, function() {
            async.eachSeries(Object.keys(entCandidates), function(i, callback1) {
                console.log("Scoring surface form " + i);
                async.eachSeries(Object.keys(entCandidates), function(j, callback2) {
                    if (i < j) {
                        console.log("... against surface form " + j);
//                        console.log("Scoring surface forms " + i + " and  " + j);
                        async.eachSeries(Object.keys(entCandidates[i].candidates), function(candidate1, callback3) {
                            redisClientCoOc.hgetall(candidate1, function(err, coocCand) {
                                if (coocCand) {
                                    entCandidates[i].candidates[candidate1].cooc = coocCand;
                                }
                                
                                for (var candidate2 in entCandidates[j].candidates) {
                                    
                                    if (entCandidates[i].candidates[candidate1].cooc[candidate2]) {
                                        var intCooc = parseInt(entCandidates[i].candidates[candidate1].cooc[candidate2]);
                                        console.log("FOUND cooc " + candidate1 + " " + candidate2 + " += " + intCooc);
                                        entCandidates[i].candidates[candidate1].coocScore += intCooc;
                                        entCandidates[j].candidates[candidate2].coocScore += intCooc;  
                                    }
                                }
                                callback3();
                            });
                        }, callback2);
                    } else {
                        callback2();
                    }
                }, callback1);
            }, function(err) {
                coocCallback(err, entCandidates);
            });
        });
    });
}

function coOccurrenceMaxScore(entities, coocCallback) {
    var entCandidates = {};
    
    async.eachSeries(entities, function(entity, callback) {
        redisClient.select(REDIS_DATABASE_NORMALIZED, function() {
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
                        coocScore: 0,
                        cooc: {}
                };
            }
        }
        
        redisClientCoOc.select(REDIS_DATABASE_COOC, function() {
            async.eachSeries(Object.keys(entCandidates), function(i, callback1) {
                console.log("Scoring surface form " + i);
                async.eachSeries(Object.keys(entCandidates), function(j, callback2) {
                    if (i < j) {
                        console.log("... against surface form - " + j);
//                        console.log("Scoring surface forms " + i + " and  " + j);
                        async.eachSeries(Object.keys(entCandidates[i].candidates), function(candidate1, callback3) {
                            redisClientCoOc.hgetall(candidate1, function(err, coocCand) {
                                if (coocCand) {
                                    entCandidates[i].candidates[candidate1].cooc = coocCand;
                                }
                                
                                for (var candidate2 in entCandidates[j].candidates) {
                                    for (var iCooc in entCandidates[i].candidates[candidate1].cooc) {
                                        if (iCooc == candidate2) {
                                            var intCooc = parseInt(entCandidates[i].candidates[candidate1].cooc[iCooc]);
                                            console.log("FOUND cooc " + candidate1 + " " + candidate2 + " += " + intCooc);
                                            if (intCooc > entCandidates[i].candidates[candidate1].coocScore) {
                                                entCandidates[i].candidates[candidate1].coocScore = intCooc;
                                            }
                                            if (intCooc > entCandidates[j].candidates[candidate2].coocScore) {
                                                entCandidates[j].candidates[candidate2].coocScore = intCooc;
                                            }
                                        }
                                    }
                                }
                                callback3();
                            });
                        }, callback2);
                    } else {
                        callback2();
                    }
                }, callback1);
            }, function(err) {
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


function disambiguate(entities, preprocess, callback) {
    var resEntities = [];
    if (preprocess == "sum") {
        coOccurrenceScore(entities, function(err, scoredCandidates) {
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
        coOccurrenceMaxScore(entities, function(err, scoredCandidates) {
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
        var entitiesItType = entitiesIterator;
        if (preprocess == "norm")
            entitiesItType = entitiesIteratorNormalized;
        
        async.concat(entities, entitiesItType, callback);
        
    }
    
}


function recognize(req, res, next) {
    req.connection.setTimeout(86400 * 1000);
    res.connection.setTimeout(86400 * 1000); 
    console.log("Calling SemiTags with text: " + req.params.text);
    
    request.post(SEMITAGS_NER_URL, {form:{language: "en", text: req.params.text}}, function callback (err, httpResponse, body) {
        if (err) {
            return console.error("SemiTags call failed:", err);
        }
    
        console.log("SemiTags returned " + body);
        xml2js.parseString(body,  function (err, semiTagsEntities) {
            if (err) {
                return console.error("Failed parsing SemiTags response:", err);
            }
            
            var entities = semiTagsEntities.stanfordEntityRestables.stanfordEntityRestable;
            
            if (entities) {
                var containsQueryEntity = false;
                for (var i in entities) {
                    if (entities[i].name[0] == req.params.queryEntity) {
                        containsQueryEntity = true;
                    }
                }
                if (!containsQueryEntity) {
                    entities.push({name: [req.params.queryEntity], type: [null], start:[null]});
                }
                
                disambiguate(entities, req.params.preprocess, function(err, disambEntities){
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
    redisClient.hgetall(req.params.surfaceForm, function(err, candidates) {
        console.log("Getting best candidate for " + req.params.surfaceForm);
        var bestCandidateRes =  getBestCandidate(candidates);
        var bestCandidate = null;
        var score = null;
        if (bestCandidateRes.bestCandidate) {
            bestCandidate = "http://www.wikipedia.org/wiki/" + bestCandidateRes.bestCandidate;
            score = bestCandidateRes.score;
        }
        
        res.send(200, { 
            name: req.surfaceForm, 
            link: bestCandidate,
            socre: score
        });
        
        return next(); 
    });
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

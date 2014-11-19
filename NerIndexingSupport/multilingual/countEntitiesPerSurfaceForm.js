/**
 * nohup node countEntitiesPerSurfaceForm.js > countEntitiesPerSurfaceForm.out 2>&1&
 */

var csv = require('csv');
var fs = require('fs');

var surfaceFormCounts = {};
var lastSurfaceForm = null;
var entitiesInSurfaceForm = [];
var surfaceForm = null;
var entity = null;
var prevIndex = 0; 


function countSurfaceForm() {
    if (lastSurfaceForm) {
        if (surfaceFormCounts[entitiesInSurfaceForm.length]) {
            surfaceFormCounts[entitiesInSurfaceForm.length]++;
        } else {
            surfaceFormCounts[entitiesInSurfaceForm.length] = 1;
        }
    }
}

//csv().from(fs.createReadStream(__dirname + '/../csv/encsv/cleanedSorted/sortBySurfaceForms.csv'), {
csv().from(fs.createReadStream(__dirname + '/../csv/encsv/cleanedSorted/sortByNormalizedSurfaceForms.csv'), {
    delimiter : ',',
    escape: '"',
    relax: true
})
.on('record', function(csvRow,index){
    surfaceForm = csvRow[1];
    entity = csvRow[0];
    
    if (prevIndex > index) {
        console.log("Error: SPLIT ROWS");
    }
    prevIndex = index;
    
    if (((index % 100000) === 0) && (index > 0)) {
        console.log("Processed " + index + " lines. SurfaceForm counts length " + Object.keys(surfaceFormCounts).length);
    }
    
    if (entitiesInSurfaceForm.length > 900) {
        console.log("TOO BIG SF: " + surfaceForm);
    }
    if (lastSurfaceForm == surfaceForm) {
        if (entitiesInSurfaceForm.indexOf(entity) < 0) {
            entitiesInSurfaceForm.push(entity);
        }
    } else {
        countSurfaceForm();
        entitiesInSurfaceForm = [];
        entitiesInSurfaceForm.push(entity);
    }
    
    lastSurfaceForm = surfaceForm;
})
.on('parse_error', function(row){ 
    console.log("Parsing error %j", row); 
    return row.split(',') ;
})
.on('end', function(count) {
    console.log('Read all lines: ' + count);
    countSurfaceForm();
    
    var outputRows = [];
    outputRows.push(["countOfEntities", "countOfSurfaceForms"]);
    
    
    for (var i in surfaceFormCounts) {
        outputRows.push([i, surfaceFormCounts[i]]);
    }
    
    console.log("Writig results to csv");
    
    csv().from.array(outputRows)
//    .to.stream(fs.createWriteStream(__dirname + '/../csv/encsv/cleanedSorted/countEntitiesPerSurfaceForm.csv'))
    .to.stream(fs.createWriteStream(__dirname + '/../csv/encsv/cleanedSorted/countEntitiesPerNormalizedSurfaceForm.csv'))
    .on('close', function(count) {
        console.log("Written surfaceForm counts " + (count - 1));
        process.exit(0);
    });
}).on('error', function(error) {
    console.log("ERROR: " + error.message);
    process.exit(1);
});

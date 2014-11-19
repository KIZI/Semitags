var csv = require('csv');
var fs = require('fs');

var entityCounts = {};
var lastEntity = null;
var surfaceFormsInEntity = [];
var surfaceForm = null;
var entity = null;
var prevIndex = 0; 


function countSurfaceForm() {
    if (lastEntity) {
        if (entityCounts[surfaceFormsInEntity.length]) {
            entityCounts[surfaceFormsInEntity.length]++;
        } else {
            entityCounts[surfaceFormsInEntity.length] = 1;
        }
    }
}

csv().from(fs.createReadStream(__dirname + '/../csv/encsv/cleanedSorted/sortByEntities.csv'), {
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
        console.log("Processed " + index + " lines. Entity counts length " + Object.keys(entityCounts).length);
    }
    
    if (surfaceFormsInEntity.length > 1000) {
        console.log("TOO BIG ENTITY: " + entity)
    }
    if (lastEntity == entity) {
        if (surfaceFormsInEntity.indexOf(surfaceForm) < 0) {
            surfaceFormsInEntity.push(surfaceForm);
        }
    } else {
        countSurfaceForm();
        surfaceFormsInEntity = [];
        surfaceFormsInEntity.push(surfaceForm);
    }
    
    lastEntity = entity;
})
.on('parse_error', function(row){ 
    console.log("Parsing error %j", row); 
    return row.split(',') ;
})
.on('end', function(count) {
    console.log('Read all lines: ' + count);
    countSurfaceForm();
    
    var outputRows = [];
    outputRows.push(["countOfSurfaceForms", "countOfEntities"]);
    
    
    for (var i in entityCounts) {
        outputRows.push([i, entityCounts[i]]);
    }
    
    console.log("Writig results to csv");
    
    csv().from.array(outputRows)
    .to.stream(fs.createWriteStream(__dirname + '/../csv/encsv/cleanedSorted/countSurfaceFormsPerEntity.csv'))
    .on('close', function(count) {
        console.log("Written surfaceForm counts " + (count - 1));
        process.exit(0);
    });
}).on('error', function(error) {
    console.log("ERROR: " + error.message);
    process.exit(1);
});

var csv = require('csv');
var fs = require('fs');

var surfaceFormCounts = {};
var lastSurfaceForm = null;
var entitiesInSurfaceForm = [];
var surfaceForm = null;
var entity = null;
var prevIndex = 0; 
var surfaceFormPopularity = 0;


function countSurfaceForm() {
    if (lastSurfaceForm) {
        if (surfaceFormCounts[entitiesInSurfaceForm.length]) {
            if (surfaceFormCounts[entitiesInSurfaceForm.length][surfaceFormPopularity]) {
                surfaceFormCounts[entitiesInSurfaceForm.length][surfaceFormPopularity]++;
            } else {
                surfaceFormCounts[entitiesInSurfaceForm.length][surfaceFormPopularity] = 1;
            }
        } else {
            surfaceFormCounts[entitiesInSurfaceForm.length] = {};
            surfaceFormCounts[entitiesInSurfaceForm.length][surfaceFormPopularity] = 1;
        }
    }
}

csv().from(fs.createReadStream(__dirname + '/../csv/encsv/sortBySurfaceForms.csv'), {
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
    
    if (lastSurfaceForm == surfaceForm) {
        surfaceFormPopularity++;
        if (entitiesInSurfaceForm.indexOf(entity) < 0) {
            entitiesInSurfaceForm.push(entity);
        }
    } else {
        countSurfaceForm();
        entitiesInSurfaceForm = [];
        entitiesInSurfaceForm.push(entity);
        surfaceFormPopularity = 1;
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
        for (var j in surfaceFormCounts[i]) {
            outputRows.push([i, j, surfaceFormCounts[i][j]]);
        }
    }
    
    console.log("Writig results to csv");
    
    csv().from.array(outputRows)
    .to.stream(fs.createWriteStream(__dirname + '/../csv/encsv/countEntitiesPerSurfaceFormPopularity.csv'))
    .on('close', function(count) {
        console.log("Written surfaceForm counts " + (count - 1));
        process.exit(0);
    });
}).on('error', function(error) {
    console.log("ERROR: " + error.message);
    process.exit(1);
});

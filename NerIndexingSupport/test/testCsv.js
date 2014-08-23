var csv = require('csv');

csv().from.string('#Welcome\n"1","2","3","4"\n"a","b","c","d"', {
    comment : '#'
}).transform(function(row, index, callback) {
    
    if (row[0] != '1') {
        setTimeout(function(){
            callback();
        }, 1000);
    } else {
        setTimeout(function(){
            callback(null, row);
        }, 1000);
    }
  }, {parallel: 1}
).to.array(function(data) {
    console.log(data);
});

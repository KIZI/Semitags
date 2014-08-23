$(document).ready(function() {
    $("#textform").submit(function(event){
        event.preventDefault();
        $.post("http://nlp.vse.cz:8081/recognize", $(this).serialize(), function(data) {
            var html = "<ul>";
            var annotatedText = $("#txtarea").val();
            var indices = [];
            for (var i in data) {
                if (data[i].link) {
                    if (data[i].link.indexOf("http://www.wikipedia.org/wiki/") != 0) {
                        data[i].link = "http://www.wikipedia.org/wiki/" + data[i].link;
                    }
                    
                    getAllIndicies(annotatedText, data[i].name, data[i], indices);
                    
//                    annotatedText = annotatedText.substring(0, pos) 
//                        + "<a href=\"http://wikipedia.org/wiki/" + data[i].link + "\" target=\"blank\">" + data[i].name + "</a>"
//                        + annotatedText.substring(pos + data[i].name.length, annotatedText.length);
//                    offset = pos + ("<a href=\"http://wikipedia.org/wiki/" + data[i].link + "\" target=\"blank\">" + data[i].name + "</a>").length - data[i].name.length;
                    
                    if (data[i].score)
                        html += "<li><a href=\"" + data[i].link + "\" target=\"blank\">" + data[i].name + "</a> [" + data[i].type + "] (confidence: " + data[i].score + ")</li>";
                    else 
                        html += "<li><a href=\"" + data[i].link + "\" target=\"blank\">" + data[i].name + "</a> [" + data[i].type + "]</li>";
                }
                else
                    html += "<li>" + data[i].name + "[" + data[i].type + "] </li>";
            }
            
            indices.sort(function(a, b) {
                return a.index - b.index;
            });
            
            var offset = 0;
            console.log(indices);
            console.log(annotatedText);
            for (var i in indices) {
                indices[i]
                var pos = indices[i].index;
                annotatedText = annotatedText.substring(0, pos + offset)
                + "<a href=\"" + indices[i].data.link + "\" target=\"blank\">" + indices[i].data.name + "</a>"
                + annotatedText.substring(pos + offset + indices[i].data.name.length, annotatedText.length + offset);
                offset = offset + ("<a href=\"" + indices[i].data.link + "\" target=\"blank\">" + indices[i].data.name + "</a>").length - indices[i].data.name.length;                
            }
            
            html += "</ul>";
            
            $("#textres").html(annotatedText.replace(/\n/g, "<br />"));
            $("#result").html(html);
        });
    });
});

function getAllIndicies(str, val, dataItem, indices) {
    var regex = new RegExp("[^a-zA-Z_0-9]" + val + "[^a-zA-Z_0-9]", "gi"), result;
    while ( (result = regex.exec(str)) ) {
        if (!containsIndex(indices, result.index + 1)) {
            indices.push({
                index: result.index + 1,
                length: dataItem.name.length,
                data: dataItem
            });
        }
    }
    
    return indices;
}

function containsIndex(indices, index) {
    for (var i in indices) {
        if ((indices[i].index <= index) && (index <= (indices[i].index + indices[i].length))) 
            return true;
    }
    
    return false;
}
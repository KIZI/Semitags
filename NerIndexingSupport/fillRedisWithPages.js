var mysql = require('mysql');
var redis = require("redis"),

client = redis.createClient();

var db_config = {
    host : 'localhost',
    user : 'ner',
    password : 'ner',
    database: 'ner'
};

var connection;

function handleDisconnect() {
    connection = mysql.createConnection(db_config); // Recreate the connection,
                                                    // since
                                                    // the old one cannot be reused.

    connection.connect(function(err) { // The server is either down
        if (err) { // or restarting (takes a while sometimes).
            console.log('error when connecting to db:', err);
            setTimeout(handleDisconnect, 2000); // We introduce a delay before
                                                // attempting to reconnect,
        } // to avoid a hot loop, and to allow our node script to
    }); // process asynchronous requests in the meantime.
    // If you're also serving http, display a 503 error.
    connection.on('error', function(err) {
        console.log('db error', err);
        if (err.code === 'PROTOCOL_CONNECTION_LOST') { // Connection to the
                                                        // MySQL server is
                                                        // usually
            handleDisconnect(); // lost due to either server restart, or a
        } else { // connnection idle timeout (the wait_timeout
            throw err; // server variable configures this)
        }
    });
}

handleDisconnect();

var query = connection.query('SELECT SQL_NO_CACHE * FROM page');

client.on("error", function (err) {
    console.log("Redis error " + err);
});

var results = 0;

query
    .on('error', function(err) {
        console.log("MySQL error: " + err);
    })
    .on('result', function(row) {
        results++;
        connection.pause();
        if ((results % 10000) === 0) {
            console.log("Processed " + results + " results");
        }
    
        client.set(row.page_title, row.page_is_redirect);
      
        connection.resume();
    })
    .on('end', function() {
        console.log("All lines read");
    });
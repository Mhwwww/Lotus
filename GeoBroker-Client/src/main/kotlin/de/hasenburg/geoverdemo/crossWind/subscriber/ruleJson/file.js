module.exports = (req, res) => {
    const fs = require("fs");

    const fileName = "saverule.json";
    const crosswindFileName = "aircraftType.json";

    fs.readFile(fileName, function (err, data) {
        if (!err) {
            // user input rules related
            let rules = JSON.parse(data);
            let inJson = JSON.parse(JSON.stringify(req.body));

            // crosswind user input related
            let crosswindThreshold = 0.0;
            let aircraftType = "";

            let windVelocity = "";
            let windAngle = "";
            let crosswind = 0.0;

            for (let i = 0; i < rules.length; i++) {
                if (rules[i]["topic"] == "aircraft type") {
                    aircraftType = rules[i]["constraints"];
                    console.log(aircraftType);
                } else if (rules[i]["topic"] == "crosswind") {
                    crosswindThreshold = rules[i]["constraints"];
                    console.log(crosswindThreshold);
                }

                if (i === rules.length - 1 && crosswindThreshold === 0.0) {
                    console.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    // Read crosswindThreshold from file asynchronously using a Promise
                    readCrosswindThresholdFromFile(crosswindFileName, aircraftType)
                        .then((threshold) => {
                            crosswindThreshold = threshold;
                            console.log("READ FROM FILE" + crosswindThreshold);

                            // Process windVelocity and windAngle
                            processWindData();
                        })
                        .catch((error) => {
                            console.log("Error reading crosswindThreshold from file:", error);
                            // Process windVelocity and windAngle
                            processWindData();
                        });
                } else {
                    // Process windVelocity and windAngle
                    processWindData();
                }
            }

            function processWindData() {
                windAngle = inJson["message"]["windDirection"];
                windVelocity = inJson["message"]["windVelocity"];

                console.log(windVelocity, windAngle, windVelocity && windAngle);

                if (windVelocity && windAngle) {
                    console.log(windAngle, windVelocity);
                    console.log(crosswind, crosswindThreshold);

                    crosswind = crosswindCalculator(windVelocity, windAngle);
                    if (crosswind >= crosswindThreshold) {
                        console.log("DO NOT LAND!!!");
                        res.write(JSON.stringify(inJson));
                    } else {
                        console.log("current crosswind is " + crosswind + "\nYOU CAN LAND");
                    }
                } else {
                    console.log("There is not enough information for crosswind");
                    res.write("There is not enough information for crosswind");
                }

                res.end();
            }
        }
    });
};

function readCrosswindThresholdFromFile(fileName, aircraftType) {
    const fs = require("fs");

    return new Promise((resolve, reject) => {
        fs.readFile(fileName, function (err, data) {
            if (err) {
                reject(err);
            } else {
                let crossWindConstraints = JSON.parse(data);
                for (let j = 0; j < crossWindConstraints.length; j++) {
                    if (crossWindConstraints[j]["type"] == aircraftType) {
                        resolve(crossWindConstraints[j]["constraints"]);
                        return;
                    }
                }
                reject("Crosswind threshold not found for the given aircraft type");
            }
        });
    });
}

function crosswindCalculator(windVelocity, windAngle) {
    /*
        Here are 3 crosswind rules-of-thumb:

        If the wind is 30 degrees off the runway, your crosswind component is about 50% of the wind speed.
        If the wind is 45 degrees off the runway, the crosswind component is about 75% of the wind speed.
        And if the wind is 60 degrees or more off the runway, the crosswind component is roughly the same as the total wind.
    */
    //50% set
    let halfSet = ["ENE", "ESE", "WNW", "WSW"];
    //75% set
    let quaterSet = ["NE", "SE", "SW", "NW"];
    //full set
    let fullSet = ["NNE", "NNW", "SSW", "SSE", "E", "W", "N", "S"];

    if (halfSet.includes(windAngle)) {
        console.log("crosswind in HALF SET!!!!");
        return windVelocity * 0.5;
    }

    if (quaterSet.includes(windAngle)) {
        console.log("crosswind in QUater SET!!!!");
        return windVelocity * 0.75;
    }

    if (fullSet.includes(windAngle)) {
        console.log("crosswind in FULL SET!!!!");
        return windVelocity;
    }
}


module.exports = (req, res) => {
    const fs = require("fs");
    const fileName = "saverule.json";

    //const crosswindFs = require("fs");
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
            let headwind = 0.0;

            let runwayDirection = "";
            //let runwayDirection = "RWY30";

            for (let i = 0; i < rules.length; i++) {
                if (rules[i]["topic"] == "aircraft type") {
                    aircraftType = rules[i]["constraints"];
                    console.log(aircraftType);
                } else if (rules[i]["topic"] == "crosswind") {
                    crosswindThreshold = rules[i]["constraints"];
                    console.log(crosswindThreshold);
                } else if (rules[i]["topic"] == "runway direction") {
                    runwayDirection = rules[i]["constraints"];
                    console.log(runwayDirection);
                }
            }

            if (crosswindThreshold == 0.0) {
                console.log("Crosswind threshold is 0, we need try to find the matching one in the file");
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
                        //processWindData();
                    });
            } else {
                // Process windVelocity and windAngle
                processWindData();
            }

            function processWindData() {
                windAngle = inJson["message"]["windDirection"];
                windVelocity = inJson["message"]["windVelocity"];

                //console.log(windVelocity, windAngle, windVelocity && windAngle);

                if (windVelocity && windAngle) {
                    console.log(windAngle, windVelocity);
                    console.log(crosswind, crosswindThreshold);

                    let resultFromCalculator = new Array()

                    resultFromCalculator = drittelmethodeCrosswindCalculator(runwayDirection, windVelocity, windAngle);
                    console.log("result from calculator" + resultFromCalculator);

                    if (resultFromCalculator == "Input Error From Sensor") {
                        //res.write("Sensor Input Error")
                        console.log("Sensor Input Error")
                        res.end();
                    }
                    if (resultFromCalculator == "Error in Calculation") {
                       //
                        console.log("Error in Calculation")
                        res.end();
                    }


                    crosswind = resultFromCalculator[0];
                    headwind = resultFromCalculator[1];
                    //console.log("The crosswind is " + crosswind + "\nThe headwind is " + headwind);


                    if (crosswind >= crosswindThreshold) {
                        console.log("DO NOT LAND!!!");
                        //send warnings back
                        res.write(JSON.stringify(inJson));
                    } else {
                        console.log("current crosswind is " + crosswind + "\nYOU CAN LAND");
                        //res.write("YOU CAN LAND at current crosswind: " +crosswind);
                    }
                } else {
                    console.log("There is not enough information for crosswind");
                    //res.write("There is not enough information for crosswind");
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

function drittelmethodeCrosswindCalculator(runwayDirection, windVelocity, windAngle) {
    const windAngleSet = {
        N: 0,//0
        NNE: 22.5,//1
        NE: 45,//2
        ENE: 67.5,//3
        E: 90,//4
        ESE: 112.5,//5
        SE: 135,//6
        SSE: 157.5,//7
        S: 180,//8
        SSW: 202.5,
        SW: 225,
        WSW: 247.5,
        W: 270,//12
        WNW: 292.5,
        NW: 315,
        NNW: 337.5,
    };

    let result = 0.0
    let runwayAngle = parseInt(runwayDirection.slice(3, runwayDirection.length), 10) * 10

    console.log("Here is the runway angle " + runwayAngle + " ,read from " + runwayDirection)


    let angle = windAngleSet[windAngle];

    //If data from sensor is ERROR/ or Error code 255
    if (windAngle == 255 || windAngle == "ERROR") {
        console.log(windAngle)
        return "Input Error From Sensor"
    }

    if (angle < 180) {
        result = Math.abs(runwayAngle - angle)
        console.log(result)
    } else {
        console.log("angle over 180 " + angle)
        result = Math.abs(360 - angle + runwayAngle)
    }

    while (result > 180) {
        result = result - 180
        console.log("Here is the value of result " + result);
    }


    //let firstSet = 0 <= windDirction < 30
    if ((result >= 0 && result < 30) || (result >= 150 && result < 180)) {
        crosswind = windVelocity / 3;
        headwind = windVelocity;
        console.log("crosswind in the FIRST SET, the CROSSWIND is: " + crosswind + " the HEADWIND is: " + headwind);
        //result =
        return [crosswind, headwind];
    } else if ((result >= 30 && result < 60) || (result >= 120 && result < 150)) {//    let secondSet = 30 <= windDirction < 60
        crosswind = windVelocity * 2 / 3;
        headwind = windVelocity * 2 / 3;
        console.log("crosswind in the SECOND SET, the CROSSWIND is: " + crosswind + " the HEADWIND is: " + headwind);

        return [crosswind, headwind];
    } else if ((result >= 60 && result < 90) || (result >= 90 && result < 120)) {//let thirdSet = 60 <= windDirction <= 90
        headwind = windVelocity / 3;
        crosswind = windVelocity;
        console.log("crosswind in the THIRD SET, the CROSSWIND is: " + crosswind + " the HEADWIND is: " + headwind);

        return [crosswind, headwind];
    } else {
        return "Error in Calculation"
    }

}

module.exports = (req, res) => {
    const fs = require("fs");
    const fileName = "saverule.json"


    fs.readFile(fileName, function (err, data) {
        if (!err) {
            let rules = JSON.parse(data)
            let topicSet = new Array()
            let operatorSet = new Array()
            let constraintsSet = new Array()

            let trueValue = new Array()
            let trueValueTable = true

            let resultFromGeoBroker = JSON.parse(JSON.stringify(req.body));
            let inJson = req.body["message"]

            for (let i = 0; i < rules.length; i++) {
                topicSet[i] = rules[i]["topic"]
                operatorSet[i] = rules[i]["operator"]
                constraintsSet[i] = rules[i]["constraints"]
                trueValue[i] = true

                if (!resultFromGeoBroker["message"][topicSet[i]]) {
                    console.log("no matching event for the given rules topic")
                    res.write("no matching event for the given rules topic")
                    trueValueTable = false
                    break;
                }


                switch (operatorSet[i]) {
                    case "=":
                        if (resultFromGeoBroker["message"][topicSet[i]] == constraintsSet[i]) {
                            console.log("for event " + resultFromGeoBroker["topic"] + " its " + topicSet[i] + "is " + operatorSet[i] + constraintsSet[i] + "\n")
                            break;
                        } else {
                            console.log("NOT MATCHING!!! for event " + resultFromGeoBroker["topic"] + " its " + topicSet[i] + " is not " + operatorSet[i] + constraintsSet[i] + ", it is: " + resultFromGeoBroker["message"][topicSet[i]] + "\n")
                            trueValue[i] = false
                            break;
                        }


                    case ">":
                        if (resultFromGeoBroker["message"][topicSet[i]] > constraintsSet[i]) {
                            console.log("for event " + resultFromGeoBroker["topic"] + " its " + topicSet[i] + "is " + operatorSet[i] + constraintsSet[i] + ", it is: " + resultFromGeoBroker["message"][topicSet[i]] + "\n")
                            break;

                        } else {
                            console.log("NOT MATCHING!!! for event " + resultFromGeoBroker["topic"] + " its " + topicSet[i] + " is not " + operatorSet[i] + constraintsSet[i] + ", it is: " + resultFromGeoBroker["message"][topicSet[i]] + "\n")
                            trueValue[i] = false
                            break;

                        }


                    case "<":
                        if (resultFromGeoBroker["message"][topicSet[i]] < constraintsSet[i]) {
                            console.log("for event " + resultFromGeoBroker["topic"] + " its " + topicSet[i] + "is " + operatorSet[i] + constraintsSet[i] + ", it is: " + resultFromGeoBroker["message"][topicSet[i]] + "\n")
                            // res.write("for event " + resultFromGeoBroker["topic"]+ " its " +topicSet[i]+ "is " +operatorSet[i] + constraintsSet[i]+ ", it is: "+resultFromGeoBroker["message"][topicSet[i]]+ "\n")
                            break;

                        } else {
                            console.log("NOT MATCHING!!! for event " + resultFromGeoBroker["topic"] + " its " + topicSet[i] + " is not " + operatorSet[i] + constraintsSet[i] + ", it is: " + resultFromGeoBroker["message"][topicSet[i]] + "\n")
                            //res.write("NOT MATCHING!!! for event " + resultFromGeoBroker["topic"]+ " its " +topicSet[i]+  " is not " +operatorSet[i] + constraintsSet[i]+ ", it is: "+resultFromGeoBroker["message"][topicSet[i]]+"\n")
                            trueValue[i] = false
                            break;

                        }
                    case ">=":
                        if (resultFromGeoBroker["message"][topicSet[i]] >= constraintsSet[i]) {
                            console.log("for event " + resultFromGeoBroker["topic"] + " its " + topicSet[i] + "is " + operatorSet[i] + constraintsSet[i] + ", it is: " + resultFromGeoBroker["message"][topicSet[i]] + "\n")
                            break;

                        } else {
                            console.log("NOT MATCHING!!! for event " + resultFromGeoBroker["topic"] + " its " + topicSet[i] + " is not " + operatorSet[i] + constraintsSet[i] + ", it is: " + resultFromGeoBroker["message"][topicSet[i]] + "\n")
                            trueValue[i] = false
                            break;

                        }
                    case "<=":
                        if (resultFromGeoBroker["message"][topicSet[i]] <= constraintsSet[i]) {
                            console.log("for event " + resultFromGeoBroker["topic"] + " its " + topicSet[i] + "is " + operatorSet[i] + constraintsSet[i] + ", it is: " + resultFromGeoBroker["message"][topicSet[i]] + "\n")
                            break;

                        } else {
                            console.log("NOT MATCHING!!! for event " + resultFromGeoBroker["topic"] + " its " + topicSet[i] + " is not " + operatorSet[i] + constraintsSet[i] + ", it is: " + resultFromGeoBroker["message"][topicSet[i]] + "\n")
                            trueValue[i] = false
                            break;

                        }
                }
                //intersection
                //check if this rule contains element 'link'; if exists--> use link, if not--> use intersection
                let link = rules[i]["link"]
                if (link == "") {
                    link = "AND";
                }

                switch (link) {
                    case "AND":
                        trueValueTable = trueValueTable && trueValue[i]
                        break;
                    case "OR":
                        trueValueTable = trueValueTable || trueValue[i]
                        break;
                    case "NOT":
                        trueValueTable = trueValueTable && !trueValue[i]
                        break;
                }

            }

            if (trueValueTable) {
                console.log("the matching event is: " + resultFromGeoBroker["topic"] + JSON.stringify(resultFromGeoBroker["location"]) + JSON.stringify(inJson))
                res.write(JSON.stringify(inJson));
                res.end();
                return;

            } else {
                console.log("no matching for existing rules")
                res.end();
                return;
            }
            res.end();

        } else {

            res.end();
            return;
        }

    })

};
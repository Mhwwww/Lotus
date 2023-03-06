module.exports = (req,res)=>{
    const fs = require("fs");

    fs.readFile("rule.json", function (err,data){
        if(!err){
            let rules = JSON.parse(data)
            let topicSet = []
            let operatorSet = []
            let constraintsSet = []
            let trueValue = []
            //intersection, for union set it to false
            let trueValueTable = true


            let resultFromGeoBroker = JSON.parse(JSON.stringify(req.body));

            for(let i = 0; i < rules.length; i++){
                topicSet[i] = rules[i]["topic"]
                operatorSet[i] = rules[i]["operator"]
                constraintsSet[i] = rules[i]["constraints"]
                trueValue[i] = true

               // console.log(topicSet[i])
                //conjunction
                // i want to check, if an evnet doesn't have all needed rule topic, then it won't be a matching event.
                // question: if the event needs to have all the rule topic
                if(!resultFromGeoBroker["message"][topicSet[i]]){
                    console.log("no matching event for the given rules topic")
                    res.write("no matching event for the given rules topic")
                    trueValueTable = false
                    break;
                }

                switch (operatorSet[i]){
                    case "=":
                        if(resultFromGeoBroker["message"][topicSet[i]] === constraintsSet[i]){
                            console.log("for event " + resultFromGeoBroker["topic"]+ " its " +topicSet[i]+ "is " + operatorSet[i] + constraintsSet[i]+ "\n")
                            //res.write("for event " + resultFromGeoBroker["topic"]+ " its " +topicSet[i]+ "is " + operatorSet[i] + constraintsSet[i]+ "\n")
                            break;
                        }else{
                            console.log("NOT MATCHING!!! for event " + resultFromGeoBroker["topic"]+ " its " +topicSet[i]+  " is not " +operatorSet[i] + constraintsSet[i]+ ", it is: "+resultFromGeoBroker["message"][topicSet[i]]+"\n")
                            //res.write("NOT MATCHING!!! for event " + resultFromGeoBroker["topic"]+ " its " +topicSet[i]+  " is not " +operatorSet[i] + constraintsSet[i]+ ", it is: "+resultFromGeoBroker["message"][topicSet[i]]+"\n")
                            trueValue[i] = false
                            break;
                      }


                    case ">":
                        if(resultFromGeoBroker["message"][topicSet[i]] > constraintsSet[i]){
                            console.log("for event " + resultFromGeoBroker["topic"]+ " its " +topicSet[i]+ "is " +operatorSet[i] + constraintsSet[i]+ ", it is: "+resultFromGeoBroker["message"][topicSet[i]]+ "\n")

                            //res.write("for event " + resultFromGeoBroker["topic"]+ " its " +topicSet[i]+ "is " +operatorSet[i] + constraintsSet[i]+ ", it is: "+resultFromGeoBroker["message"][topicSet[i]]+ "\n")
                            break;

                        }else{
                            console.log("NOT MATCHING!!! for event " + resultFromGeoBroker["topic"]+ " its " +topicSet[i]+  " is not " +operatorSet[i] + constraintsSet[i]+ ", it is: "+resultFromGeoBroker["message"][topicSet[i]]+"\n")
                           // res.write("NOT MATCHING!!! for event " + resultFromGeoBroker["topic"]+ " its " +topicSet[i]+  " is not " +operatorSet[i] + constraintsSet[i]+ ", it is: "+resultFromGeoBroker["message"][topicSet[i]]+"\n")
                            trueValue[i] = false
                            break;

                        }


                    case "<":
                        if(resultFromGeoBroker["message"][topicSet[i]] < constraintsSet[i]){
                            console.log("for event " + resultFromGeoBroker["topic"]+ " its " +topicSet[i]+ "is " +operatorSet[i] + constraintsSet[i]+ ", it is: "+resultFromGeoBroker["message"][topicSet[i]]+ "\n")

                           // res.write("for event " + resultFromGeoBroker["topic"]+ " its " +topicSet[i]+ "is " +operatorSet[i] + constraintsSet[i]+ ", it is: "+resultFromGeoBroker["message"][topicSet[i]]+ "\n")
                            break;

                        }else{
                            console.log("NOT MATCHING!!! for event " + resultFromGeoBroker["topic"]+ " its " +topicSet[i]+  " is not " +operatorSet[i] + constraintsSet[i]+ ", it is: "+resultFromGeoBroker["message"][topicSet[i]]+"\n")
                            //res.write("NOT MATCHING!!! for event " + resultFromGeoBroker["topic"]+ " its " +topicSet[i]+  " is not " +operatorSet[i] + constraintsSet[i]+ ", it is: "+resultFromGeoBroker["message"][topicSet[i]]+"\n")
                            trueValue[i] = false
                            break;

                        }
                    case ">=":
                        if(resultFromGeoBroker["message"][topicSet[i]] >= constraintsSet[i]){
                            console.log("for event " + resultFromGeoBroker["topic"]+ " its " +topicSet[i]+ "is " +operatorSet[i] + constraintsSet[i]+ ", it is: "+resultFromGeoBroker["message"][topicSet[i]]+ "\n")
                            //res.write("for event " + resultFromGeoBroker["topic"]+ " its " +topicSet[i]+ "is " +operatorSet[i] + constraintsSet[i]+ ", it is: "+resultFromGeoBroker["message"][topicSet[i]]+ "\n")
                            break;

                        }else{
                            console.log("NOT MATCHING!!! for event " + resultFromGeoBroker["topic"]+ " its " +topicSet[i]+  " is not " +operatorSet[i] + constraintsSet[i]+ ", it is: "+resultFromGeoBroker["message"][topicSet[i]]+"\n")
                            //res.write("NOT MATCHING!!! for event " + resultFromGeoBroker["topic"]+ " its " +topicSet[i]+  " is not " +operatorSet[i] + constraintsSet[i]+ ", it is: "+resultFromGeoBroker["message"][topicSet[i]]+"\n")
                            trueValue[i] = false
                            break;

                        }
                    case "<=":
                        if(resultFromGeoBroker["message"][topicSet[i]] <= constraintsSet[i]){
                            console.log("for event " + resultFromGeoBroker["topic"]+ " its " +topicSet[i]+ "is " +operatorSet[i] + constraintsSet[i]+ ", it is: "+resultFromGeoBroker["message"][topicSet[i]]+ "\n")
                            //res.write("for event " + resultFromGeoBroker["topic"]+ " its " +topicSet[i]+ "is " +operatorSet[i] + constraintsSet[i]+ ", it is: "+resultFromGeoBroker["message"][topicSet[i]]+ "\n")
                            break;

                        }else{
                            console.log("NOT MATCHING!!! for event " + resultFromGeoBroker["topic"]+ " its " +topicSet[i]+  " is not " +operatorSet[i] + constraintsSet[i]+ ", it is: "+resultFromGeoBroker["message"][topicSet[i]]+"\n")
                           // res.write("NOT MATCHING!!! for event " + resultFromGeoBroker["topic"]+ " its " +topicSet[i]+  " is not " +operatorSet[i] + constraintsSet[i]+ ", it is: "+resultFromGeoBroker["message"][topicSet[i]]+"\n")
                            trueValue[i] = false
                            break;

                        }
                }


                //intersection
               trueValueTable = trueValueTable && trueValue[i]
               //console.log(trueValue[i])
               //console.log(trueValueTable)
                //union
                //trueValueTable = trueValueTable || trueValue[i]

      }
            if(trueValueTable){
                console.log("the matching event is: "+ resultFromGeoBroker["topic"]+resultFromGeoBroker["location"]+resultFromGeoBroker["message"])
                res.write(JSON.stringify(resultFromGeoBroker["message"]))
                //res.send("the new topics of the processed events are:")

            }
            else{
                console.log("no matching for existing rules")
                res.write()
            }


        res.end();

        }else{
            res.end();
        }

    })

}
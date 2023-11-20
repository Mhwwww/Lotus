//the function that returns the highest N recent temperatures.

const localState = [""]; // an array to store state
let priority = false; // true is Warning, false is messages.

const constrainsForState =30.0
const topNum = 10

module.exports = (req, res) => {
    let inJson = req.body["message"];
    inJson["priority"] = priority;

    if (inJson["temperature"] > constrainsForState) {
        localState.push(inJson["temperature"]);

        priority = true;
        inJson["priority"] = priority;

        console.log("the tempurature is higher than" + constrainsForState + ", it is " + inJson["temperature"])
        console.log("the priority is: " + inJson["priority"])

        const topTen = findTopN(localState, topNum)
        console.log("!!!!!!!TOP TEN!!!!!!!" + topTen);
        inJson.topN = topTen

        res.write(JSON.stringify(inJson));
        res.end();
        return;
    }

    res.write("");
    res.end();
    return;
};


function findTopN(array, N) {
    // sort the array in descending order and slice the first N elements
    if( typeof N !== 'number' ||!Number.isInteger(N)){
        throw new Error(" Please Input an Integer!!! ")
    }

    //const topN = array.sort((a, b) => b - a).slice(0, N);
    const topN =array.sort((a, b) => b - a).slice(0, N);

    // return the top N values
    return topN

}
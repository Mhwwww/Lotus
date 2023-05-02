module.exports = (req,res)=>{
    let inJson = req.body["message"]
    res.write(JSON.stringify({
        "importantKey": inJson["importantKey"],
        "timeSent": inJson["timeSent"],
    }))
    //console.log(inJson)
/*    console.log(JSON.stringify({
        "importantKey": inJson["importantKey"],
        "timeSent": inJson["timeSent"],
    }))*/
    res.end()

}
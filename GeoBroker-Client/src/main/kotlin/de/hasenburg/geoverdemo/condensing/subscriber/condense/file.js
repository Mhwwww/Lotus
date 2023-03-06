module.exports = (req,res)=>{
    let inJson = req.body["message"]
    res.write(JSON.stringify({
        "importantKey": inJson["importantKey"]
    }))
    res.end()

}
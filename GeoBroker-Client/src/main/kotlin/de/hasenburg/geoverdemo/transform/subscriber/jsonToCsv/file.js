module.exports = (req,res)=>{
    console.time("all")
    let inJson = req.body["message"]["list"]
    // https://stackoverflow.com/questions/8847766/how-to-convert-json-to-csv-format-and-store-in-a-variable
    const replacer = (key, value) => value === null ? '' : value
    const header = Object.keys(inJson[0])
    const csvString = [
        header.join(','), // header row first
        ...inJson.map(row => header.map(fieldName => JSON.stringify(row[fieldName], replacer)).join(','))
    ].join('\r\n')

    let outJson = {"rawCsv": csvString}
    res.write(JSON.stringify(outJson))
    res.end()
    console.timeEnd("all")
}
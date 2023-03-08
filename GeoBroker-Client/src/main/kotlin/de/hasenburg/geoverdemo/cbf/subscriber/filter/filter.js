module.exports = (req, res) => {
  //   res.write(JSON.stringify(req.body));
  //   res.end();
  //   return;
  let inJson = req.body["message"];

  if (inJson["speed"] < 35.0) {
    res.write("");
    res.end();
    return;
  }

  if (inJson["wind"] <= 30.0) {
    res.write("");
    res.end();
    return;
  }

  res.write(JSON.stringify(inJson));
  res.end();
  return;
};

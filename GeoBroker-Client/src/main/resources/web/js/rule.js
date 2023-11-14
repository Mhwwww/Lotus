const ruleTopicInput = document.getElementById('rule-topic');
const ruleOperatorInput = document.getElementById('operator-select');
const ruleConstraintsInput = document.getElementById('rule-constraints');
const ruleLinkInput = document.getElementById('link-select');

const ruleList = document.getElementById('rule-list');
const addRuleButton = document.getElementById('add-rule');
const saveRulesButton = document.getElementById('save-rules');

// let functionSelectionInput = document.getElementById('function-select')


let rules = [];

addRuleButton.addEventListener('click', (event) => {
    event.preventDefault();

    let link = ruleLinkInput.value
    // let functionName = functionSelectionInput.value

    if (link ==="Select a Link") {
        link = '';
    }
    // if(functionName === "Crosswind Warning Generator"|| functionName === ""){
    //     functionName = 'crosswind'
    // }else if(functionName === "Temperature Warning Generator"){
    //     functionName = 'temperature'
    // }

  const rule = {
        topic: ruleTopicInput.value,
        operator: ruleOperatorInput.value,
        constraints: ruleConstraintsInput.value,
        link: link,
        // functionName: functionName,
    };

    rules.push(rule);
    renderRules();

    ruleTopicInput.value = '';
    ruleOperatorInput.value = '';
    ruleConstraintsInput.value = '';
    ruleLinkInput.value = '';
    // functionSelectionInput = '';
});

saveRulesButton.addEventListener('click', () => {
    event.preventDefault();
    // fetch("http://localhost:8081/saveRules", {
    fetch(saveRuleUrl, {
        method: "POST",
        body: JSON.stringify(rules),
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(rules)
    }) .then(response => {
        if (!response.ok) {
            throw new Error('!!!!!!!!!!!!!!!!!!!!!!!!');
        }
    })
        .then(data => {
            console.log('Response received:', data);
            return data.json();
        })
        .catch(error => {
            console.error('Error occurred:', error);
        });

});

function renderRules() {
    //todo: could delete inputed rules
    ruleList.innerHTML = '';
    for (const rule of rules) {
        const listItem = document.createElement('li');
        listItem.innerHTML = `
      <strong>${rule.link} ${rule.topic}</strong> ${rule.operator} ${rule.constraints}
    `;
        ruleList.appendChild(listItem);
    }
}


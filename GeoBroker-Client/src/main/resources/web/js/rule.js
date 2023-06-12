const ruleTopicInput = document.getElementById('rule-topic');
const ruleOperatorInput = document.getElementById('operator-select');
const ruleConstraintsInput = document.getElementById('rule-constraints');
const ruleList = document.getElementById('rule-list');
const addRuleButton = document.getElementById('add-rule');
const saveRulesButton = document.getElementById('save-rules');

let rules = [];

addRuleButton.addEventListener('click', (event) => {
    event.preventDefault();

    const rule = {
        topic: ruleTopicInput.value,
        operator: ruleOperatorInput.value,
        constraints: ruleConstraintsInput.value
    };
    rules.push(rule);
    renderRules();

    ruleTopicInput.value = '';
    ruleOperatorInput.value = '';
    ruleConstraintsInput.value = '';

});

saveRulesButton.addEventListener('click', () => {
    event.preventDefault();
    fetch("http://localhost:8081/saveRules", {
        method: "POST",
        body: JSON.stringify(rules),
        headers: {
            "Content-Type": "application/json",
        },
    }) .then(response => {
        if (!response.ok) {
            throw new Error('!!!!!!!!!!!!!!!!!!!!!!!!');
        }
    })
        .then(data => {
            console.log('Response received:', data);
            return data.json(inputEvent);
        })
        .catch(error => {
            console.error('Error occurred:', error);
        });

});

function renderRules() {
    ruleList.innerHTML = '';
    for (const rule of rules) {
        const listItem = document.createElement('li');
        listItem.innerHTML = `
      <strong>${rule.topic}</strong> ${rule.operator} ${rule.constraints}
    `;
        ruleList.appendChild(listItem);
    }
}


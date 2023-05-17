const eventTopicInput = document.getElementById('event-topic');
const eventRepTopicInput = document.getElementById('repub-topic');
const eventLatitudeInput = document.getElementById('event-lat');
const eventLongitudeInput = document.getElementById('event-lon');
const eventRadiusInput = document.getElementById('event-rad');

const eventList = document.getElementById('event-list');
const applySubscriptionButton = document.getElementById('apply-subscription');
const showWarningButton = document.getElementById('show-warning');
const showInfoButton = document.getElementById('show-information');


let events = [];
let inputEvent = {
    topic: "",
    repubTopic: "",
    lat: "",
    lon: "",
    rad: ""
};

// post subscriptions
applySubscriptionButton.addEventListener('click', (event) => {
    event.preventDefault();
    inputEvent = {
        topic: eventTopicInput.value,
        repubTopic: eventRepTopicInput.value,
        lat: eventLatitudeInput.value,
        lon: eventLongitudeInput.value,
        rad: eventRadiusInput.value
    };

    fetch('http://localhost:8081/test', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(inputEvent)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('!!!!!!!!!!!!!!!!!!!!!!!!');
            }
            return response.json()
        })
        .then(data => {
            console.log('Response received:', data);
            return data.json(inputEvent);
        })
        .catch(error => {

            console.error('Error occurred:', error);
        });

    //pre-show after input
    events.push(inputEvent);
    renderEvents();
    eventTopicInput.value = '';
    eventRepTopicInput.value = '';
    eventLatitudeInput.value = '';
    eventLongitudeInput.value = '';
    eventRadiusInput.value = '';
});

// show the input subscriptions on page
function renderEvents() {
    eventList.innerHTML = '';
    for (const inputEvent of events) {
        const listItem = document.createElement('li');
        listItem.innerHTML = `
      <strong>${inputEvent.topic}</strong>: ${inputEvent.repubTopic}, ${inputEvent.lat}, ${inputEvent.lon}, ${inputEvent.rad}
    `;
        eventList.appendChild(listItem);
    }
}

// redirect to another page
showWarningButton.addEventListener('click',()=>{
    fetch('http://localhost:8081/show', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(inputEvent)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('!!!!!!!!!!!!!!!!!!!!!!!!');
            }
            location.assign(`../warning.html?topic=${inputEvent.topic}&repubTopic=${inputEvent.repubTopic}&lat=${inputEvent.lat}&lon=${inputEvent.lon}&rad=${inputEvent.rad}`);

           // location.assign('../warning.html');
        })
        .then(data => {
            //location.assign('./warning.html');
            console.log('Response received:', data);
            return data.json(inputEvent);
        })
        .catch(error => {
            console.error('Error occurred:', error);
        });
});


// redirect to another page
showInfoButton.addEventListener('click',()=>{
    fetch('http://localhost:8081/showInfo', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(inputEvent)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('!!!!!!!!!!!!!!!!!!!!!!!!');
            }
            //location.assign('./information.html');
            location.assign(`../information.html`);

        })
        .then(data => {
            //location.assign('./warning.html');
            console.log('Response received:', data);
            return data.json(inputEvent);
        })
        .catch(error => {
            console.error('Error occurred:', error);
        });
})


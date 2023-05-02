const eventTopicInput = document.getElementById('event-topic');
const eventGeofenceInput = document.getElementById('event-geofence');
const eventList = document.getElementById('event-list');
const applySubscriptionButton = document.getElementById('apply-subscription');
const showWarningButton = document.getElementById('show-warning');

let events = [];
let inputEvent = {
    topic: "",
    geofence: ""
};

// post subscriptions
applySubscriptionButton.addEventListener('click', (event) => {
    event.preventDefault();
    inputEvent = {
        topic: eventTopicInput.value,
        geofence: eventGeofenceInput.value
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
    eventGeofenceInput.value = '';

});

// show the input subscriptions on page
function renderEvents() {
    eventList.innerHTML = '';

    for (const inputEvent of events) {
        const listItem = document.createElement('li');
        listItem.innerHTML = `
      <strong>${inputEvent.topic}</strong>: ${inputEvent.geofence}
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
            location.assign('./warning.html');
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
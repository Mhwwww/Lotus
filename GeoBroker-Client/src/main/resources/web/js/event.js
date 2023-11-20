document.addEventListener('DOMContentLoaded', function () {
    const eventTopicInput = document.getElementById('event-topic');
    const eventRepTopicInput = document.getElementById('repub-topic');
    // const eventLatitudeInput = document.getElementById('event-lat');
    // const eventLongitudeInput = document.getElementById('event-lon');
    const eventLocationNameInput = document.getElementById('event-location-name');
    const eventRadiusInput = document.getElementById('event-rad');
    //const showWarningButton = document.getElementById('show-warning');

    const showWarningButton = document.querySelector('.event .Connection #show-warnings');

    let events = [];
    let inputEvent = {
        topic: "",
        repubTopic: "",
        locationName: "",
        // lat: "",
        // lon: "",
        rad: ""
    };

    console.log('showWarningButton:', showWarningButton);

// redirect to another page
    showWarningButton.addEventListener('click', (event)=>{
        event.preventDefault();
        inputEvent = {
            topic: eventTopicInput.value,
            repubTopic: eventRepTopicInput.value,
            locationName: eventLocationNameInput.value,
            // lat: eventLatitudeInput.value,
            // lon: eventLongitudeInput.value,
            rad: eventRadiusInput.value
        };


        // fetch('http://localhost:8081/show', {

        fetch(subscriptionInputUrl, {

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
                //location.assign(`../warning.html?subscription=${JSON.stringify(inputEvent)}&ruleinput=${JSON.stringify(rules)}`);
            })
            .then(data => {
                if (data != null){
                    console.log('Response received:', data);
                }else{
                    console.log('no data')
                }

                //return data.json(inputEvent);
                location.assign(`../warning.html?subscription=${JSON.stringify(inputEvent)}&ruleinput=${JSON.stringify(rules)}`);
                return data.json();
            })
            .catch(error => {
                console.error('Error occurred:', error);
            });
    });


});



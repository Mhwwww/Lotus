Vue.component('subscription-info', {
    template: `
        <div>
            <h2>Subscription Information</h2>
            <p>{{ infoText }}</p>
        </div>
    `,
    data() {
        return {
            infoText: ''
        };
    },
    mounted() {
        const urlParams = new URLSearchParams(window.location.search);
        const topic = urlParams.get('topic');
        const repubTopic = urlParams.get('repubTopic');
        const lat = urlParams.get('lat');
        const lon = urlParams.get('lon');
        const rad = urlParams.get('rad');

        this.infoText = `The Input Subscription Is: ${topic}, ${repubTopic}, ${lat}, ${lon}, ${rad}`;
    }
});


new Vue({
    el: '#left-app',
    data: {
        leftContent: 'This is the left content.',
    }
});

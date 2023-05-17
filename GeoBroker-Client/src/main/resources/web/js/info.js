document.addEventListener('DOMContentLoaded', () => {
    const urlParams = new URLSearchParams(window.location.search);
    const topic = urlParams.get('topic');
    const repubTopic = urlParams.get('repubTopic');
    const lat = urlParams.get('lat');
    const lon = urlParams.get('lon');
    const rad = urlParams.get('rad');

    const subscriptionInfo = document.getElementById('subscription-info');
    subscriptionInfo.textContent = `The Input Subscription Is: ${topic}, ${repubTopic}, ${lat}, ${lon}, ${rad}`;
});

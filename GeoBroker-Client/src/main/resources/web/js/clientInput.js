Vue.component('subscription-info', {
    template: `
        <div>
            <h2>Subscription Information</h2>
            <table>
                <thead>
                    <tr>
                        <th>Input</th>
                        <th>Value</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="(value, key) in infoText" :key="key">
                        <td>{{ getFieldLabel(key) }}</td>
                        <td>{{ value }}</td>
                    </tr>
                </tbody>
            </table>
            <br>
            <br>
            <br>
  
            <table>
                <thead>
                    <tr>
                        <th>Rule</th>
                        <th>Content</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="(content, rule) in ruleText" :key="rule">
                        <td>{{ rule + 1 }}</td>
                        <td>
                            <p>{{ getContentValues(content).join(' ') }}</p>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    `,
    data() {
        return {
            infoText: {},
            ruleText: {}
        };
    },
    mounted() {
        const urlParams = new URLSearchParams(window.location.search);
        const input = urlParams.get('subscription');
        const rule = urlParams.get('ruleinput');


        this.infoText = JSON.parse(input);
        this.ruleText = JSON.parse(rule);
    },
    methods: {
        getFieldLabel(key) {
            // Customize the field names here
            const fieldLabels = {
                topic: 'Topic',
                repubTopic: 'Target Topic',
                lat: 'Latitude',
                lon: 'Longitude',
                rad: 'Radius'
            };
            return fieldLabels[key] || key;
        },
        getContentValues(content) {
            return Object.values(content).map(value => value.toString());
        }
    }
});

new Vue({
    el: '#left-app',
    data: {
        leftContent: 'This is the left content.',
    }
});

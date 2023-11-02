Vue.component('warning-list', {
    template: `
    <div>
    <h2> Warnings: </h2>
    <table>
      <thead>
        <tr>
          <th></th>
          <th v-for="key in warningKeys" :key="key">{{ key }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="warning in warnings" :key="warning.id" class="warning-row">
          <td>{{ warning.id }}</td>
          <td v-for="key in warningKeys" :key="key" class="warning-data" >
            <template v-if="key === 'message' || key === 'location'">
              <div v-if="typeof warning[key] === 'object'">
                <div v-for="(subValue, subKey, index) in warning[key]" :key="subKey">
                  <div v-if="index < 2">{{ subKey }}: {{ subValue }}</div>
                  <div v-else v-show="!warning.collapsed">{{ subKey }}:
                    <template v-if="subKey === 'priority'">
                       <a @click="togglePriority(warning)" class="toggle-priority">
                          <template v-if="subValue == false">         
                            <button class="priority-button">Information</button>
                          </template>
                          <template v-else>
                            <button class="priority-button">Warning</button>
                          </template> 
                        </a> 

                    </template>
                    <template v-else>{{ subValue }}</template>
                  </div>
                </div>
                <div v-if="Object.keys(warning[key]).length > 2" class="show-more-row">
                    <a @click="toggleCollapse(warning)" class="collapse-link">
                      <template v-if="warning.collapsed">
                        <button class="show-more-button">show more</button>
                      </template>
                      <template v-else>
                        <button class="show-less-button">show less</button>
                      </template>
                    </a>
              </div>
              </div>
              <template v-else>
                {{ warning[key] }}
              </template>
            </template>
            <template v-else>
              {{ warning[key] }}
            </template>
          </td>
        </tr>
      </tbody>
    </table>
  </div>

  `,
    data() {
        return {
            warnings: [],
            warningKeys: [],
        };
    },
    mounted() {
        this.fetchWarnings();

    },
    methods: {
        async fetchWarnings() {
            try {
                //todo: configure in config.js
                console.log("THE WARNING URL FROM CONFIG FILE IS ",warningMsgUrl)
                const response = await fetch(warningMsgUrl);

                // const response = await fetch(warningMsgUrl);
                const data = await response.json();
                this.warnings = data.map(warning => {
                    return {...warning, collapsed: true};
                });
                // Get keys of the first warning object as table headers
                this.warningKeys = Object.keys(data[0]);
            } catch (error) {
                console.error(error);
            }
        },
        togglePriority(warning) {
            //warning.priority = !warning.priority
            warning["message"]["priority"] = !warning["message"]["priority"];
            //console.log("The current priority is ",warning["message"]["priority"]);
            this.deleteWarning(warning["message"]["timeSent"]);
            this.$forceUpdate();
        },
        async deleteWarning(timeSent) {
            try {
                const warningMsgUrlWithTimeSent = `${warningMsgUrl}/${timeSent}`;
                // const response = await fetch(`http://localhost:8081/warningMessage/${timeSent}`, {
                const response = await fetch(warningMsgUrlWithTimeSent, {

                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({action: 'delete'})
                });
                if (response.ok) {
                    // Deletion successful
                } else {
                    console.error('Failed to delete warning');
                }
            } catch (error) {
                console.error(error);
            }
        },
        toggleCollapse(warning) {
            warning.collapsed = !warning.collapsed;
        },

    }
});
Vue.component('info-list', {
    template: `
    <div>
        <h2> Information: </h2>
        <table>
            <thead>
                <tr>
                    <th></th>
                    <th v-for="key in infoSetKeys" :key="key">{{ key }}</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="info in infoSet" :key="info.id">
                    <td>{{ info.id }}</td>
                    <td v-for="key in infoSetKeys" :key="key">
                        <template v-if="key === 'message' || key === 'location'">
                            <table v-if="typeof info[key] === 'object'">
                                <tr>
                                    <th></th>
                                    <th></th>
                                </tr>
                                <tr v-for="(subValue, subKey) in info[key]">
                                    <td>{{ subKey }}</td>
                                    <td>{{ subValue }}</td>

                                </tr>
                            </table>
                            <template v-else>
                                {{ info[key] }}
                            </template>
                        </template>
                        <template v-else>
                            {{ info[key] }}
                        </template>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
    `,
    data() {
        return {
            infoSet: [],
            infoSetKeys: []
        };
    },
    mounted() {
        this.fetchInfoSet();
    },
    methods: {
        async fetchInfoSet() {
            try {
                // const response = await fetch('http://localhost:8081/infoMessage');
                const response = await fetch(infoMsgUrl);
                const data = await response.json();
                this.infoSet = data;
                this.infoSetKeys = Object.keys(data[0]);

            } catch (error) {
                console.error(error);
            }
        },
    }
});

new Vue({
    el: '#right-app',
    data: {
        currentComponent: 'warning-list',

    },
    methods: {
        reloadPage() {
            location.reload();
        },
        async showInfo() {
            this.currentComponent = 'info-list';
            /* await fetch('http://localhost:8081/showInfo', {
                 method: 'POST',
                 headers: {
                     'Content-Type': 'application/json'
                 },
                 body: JSON.stringify(this.inputEvent)
             })
                 .then(response => {
                     if (!response.ok) {
                         throw new Error('Error occurred');
                     }
                     location.assign('../information.html');

                 })
                 .catch(error => {
                     console.error('Error occurred:', error);
                 });*/


        }
    },
    components: {
        'warning-list': Vue.component('warning-list'),
        'info-list': Vue.component('info-list')
    }
});

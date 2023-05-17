/*
// get posted warnings
Vue.component('warning-list', {
    template: `
    <div>
<h2>Waiting for Result </h2>

      <table>
        <thead>
          <tr>
            <th></th>
            <th v-for="key in warningKeys" :key="key">{{ key }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="warning in warnings" :key="warning.id">
            <td>{{ warning.id }}</td>
            <td v-for="key in warningKeys" :key="key">
              <template v-if="key === 'message' || key === 'location'">
                <table v-if="typeof warning[key] === 'object'">
                  <tr>
                    <th></th>
                    <th></th>
                  </tr>
                  <tr v-for="(subValue, subKey) in warning[key]">
                    <td>{{ subKey }}</td>
                    <td>
                      <template v-if="subKey === 'priority'">
                        <button @click="togglePriority(warning, subValue)">{{ subValue[0] }}</button>
                      </template>
                      <template v-else>
                        {{ subValue }}
                      </template>
                    </td>
                  </tr>
                </table>
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
                const response = await fetch('http://localhost:8081/warningMessage');
                const data = await response.json();
                this.warnings = data;
                this.warningKeys = Object.keys(data[0]); // Get keys of the first warning object as table headers
            } catch (error) {
                console.error(error);
            }
        },
        togglePriority(warning, priority) {
            Vue.set(priority, 0, !priority[0]);
            this.deleteWarning(warning.id); //maybe based on 'timeSent/heartbeat'
        },
        //TODO: delete function
        async deleteWarning(warningId) {
            try {
                const response = await fetch(`http://localhost:8081/warningMessage/${warningId}`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ action: 'delete' })
                });
                if (response.ok) {
                    // Deletion successful, handle as needed
                } else {
                    console.error('Failed to delete warning');
                }
            } catch (error) {
                console.error(error);
            }
        }
    }
});

new Vue({
    el: '#app',
    template: `
    <div>
     <left-panel></left-panel>
      <warning-list></warning-list>
      <button @click="reloadPage()">Reload</button>
    </div>
  `,
    methods: {
        reloadPage() {
            location.reload();
        }
    }
});
*/

Vue.component('warning-list', {
    template: `
    <div>
        <h2>Waiting for Result</h2>
        <table>
            <thead>
                <tr>
                    <th></th>
                    <th v-for="key in warningKeys" :key="key">{{ key }}</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="warning in warnings" :key="warning.id">
                    <td>{{ warning.id }}</td>
                    <td v-for="key in warningKeys" :key="key">
                        <template v-if="key === 'message' || key === 'location'">
                            <table v-if="typeof warning[key] === 'object'">
                                <tr>
                                    <th></th>
                                    <th></th>
                                </tr>
                                <tr v-for="(subValue, subKey) in warning[key]">
                                    <td>{{ subKey }}</td>
                                    <td>
                                        <template v-if="subKey === 'priority'">
                                            <button @click="togglePriority(warning, subValue)">{{ subValue[0] }}</button>
                                        </template>
                                        <template v-else>
                                            {{ subValue }}
                                        </template>
                                    </td>
                                </tr>
                            </table>
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
            warningKeys: []
        };
    },
    mounted() {
        this.fetchWarnings();
    },
    methods: {
        async fetchWarnings() {
            try {
                const response = await fetch('http://localhost:8081/warningMessage');
                const data = await response.json();
                this.warnings = data;
                this.warningKeys = Object.keys(data[0]); // Get keys of the first warning object as table headers
            } catch (error) {
                console.error(error);
            }
        },
        togglePriority(warning, priority) {
            Vue.set(priority, 0, !priority[0]);
            this.deleteWarning(warning.id); // Maybe based on 'timeSent/heartbeat'
        },
        async deleteWarning(warningId) {
            try {
                const response = await fetch(`http://localhost:8081/warningMessage/${warningId}`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ action: 'delete' })
                });
                if (response.ok) {
                    // Deletion successful, handle as needed
                } else {
                    console.error('Failed to delete warning');
                }
            } catch (error) {
                console.error(error);
            }
        }
    }
});

new Vue({
    el: '#right-app',
    methods: {
        reloadPage() {
            location.reload();
        }
    }
});

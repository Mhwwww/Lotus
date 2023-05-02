// get posted warnings
Vue.component('warning-list', {
    template: `
    <div>
      <h2>Waiting for Result </h2>
      <ul>
        <h3>The Warning List is</h3>
        <li v-for="warning in warnings" :key="warning.id">{{ warning.message }}</li>
      </ul>
    </div>
  `,
    data() {
        return {
            warnings: []
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
      <warning-list></warning-list>
      <button @click = "reloadPage()">Reload</button>
    </div>
  `,
    methods: {
    reloadPage() {
        location.reload();
    }
}
});
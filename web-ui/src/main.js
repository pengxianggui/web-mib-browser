import Vue from 'vue'
import ElementUI from 'element-ui'
import FastCrudUI from 'fast-crud-ui'
import App from './App.vue'
import http from "@/http";
import "element-ui/lib/theme-chalk/index.css";
import 'fast-crud-ui/lib/style.css';

Vue.config.productionTip = false
Vue.use(ElementUI)
Vue.use(FastCrudUI, {
    $http: http
})

new Vue({
    render: h => h(App),
}).$mount('#app')

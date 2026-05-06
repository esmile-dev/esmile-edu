import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from '@/common/router'
import { useAuthStore } from '@/common/stores/auth'
import App from './App.vue'
import './style.css'

const app = createApp(App)

const pinia = createPinia()
app.use(pinia)

// Initialize auth state from sessionStorage before router guards run
const authStore = useAuthStore()
authStore.initAuth()

app.use(router)

app.mount('#app')

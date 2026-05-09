import axios, { type AxiosInstance, type AxiosError } from 'axios'

export class ApiError extends Error {
  constructor(
    public code: number,
    message: string
  ) {
    super(message)
    this.name = 'ApiError'
  }
}

// Real API client
class RealApiClient {
  private client: AxiosInstance

  constructor() {
    this.client = axios.create({
      baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
      timeout: 30000,
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
      },
    })

    this.setupInterceptors()
  }

  private setupInterceptors() {
    this.client.interceptors.request.use(
      (config) => {
        const token = sessionStorage.getItem('token')
        if (token) {
          config.headers.Authorization = `Bearer ${token}`
        }
        return config
      },
      (error) => Promise.reject(error)
    )

    this.client.interceptors.response.use(
      (response) => response,
      async (error: AxiosError) => {
        if (error.response?.status === 401) {
          sessionStorage.removeItem('token')
          window.location.href = '/login'
        }
        // Extract error message from response body
        if (error.response?.data) {
          const errorData = error.response.data as { code?: number; message?: string }
          const message = errorData?.message || error.message
          const code = errorData?.code || error.response?.status || 500
          return Promise.reject(new ApiError(code, message))
        }
        return Promise.reject(error)
      }
    )
  }

  async get<T>(url: string, params?: object): Promise<T> {
    const response = await this.client.get<{ code: number; message: string; data: T | null }>(url, { params })
    return this.handleResponse(response)
  }

  async post<T>(url: string, data?: object): Promise<T> {
    const response = await this.client.post<{ code: number; message: string; data: T | null }>(url, data)
    return this.handleResponse(response)
  }

  async put<T>(url: string, data?: object): Promise<T> {
    const response = await this.client.put<{ code: number; message: string; data: T | null }>(url, data)
    return this.handleResponse(response)
  }

  async delete<T>(url: string): Promise<T> {
    const response = await this.client.delete<{ code: number; message: string; data: T | null }>(url)
    return this.handleResponse(response)
  }

  private handleResponse<T>(response: axios.AxiosResponse<{ code: number; message: string; data: T | null }>): T {
    const { code, message, data } = response.data
    if (code >= 200 && code < 300) {
      return data as T
    }
    throw new ApiError(code, message)
  }
}

export const apiClient = new RealApiClient()

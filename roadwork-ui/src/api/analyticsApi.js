import api from './axiosConfig'

export const getAnalytics = () => api.get('/api/admin/analytics')

// PDF is binary, so responseType must be 'blob'
export const downloadSummaryReport = () =>
    api.get('/api/admin/reports/summary-pdf', { responseType: 'blob' })